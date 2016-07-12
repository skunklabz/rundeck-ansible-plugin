package com.batix.rundeck;

import com.batix.rundeck.ext.ArgumentTokenizer;
import com.dtolabs.rundeck.core.common.INodeSet;
import com.dtolabs.rundeck.plugins.PluginLogger;

import com.dtolabs.utils.Streams;
import java.io.*;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class AnsibleRunner {

  enum AnsibleCommand {
    AdHoc("ansible"),
    Playbook("ansible-playbook");

    final String command;
    AnsibleCommand(String command) {
      this.command = command;
    }
  }

  public static AnsibleRunner adHoc(String module, String arg) {
    AnsibleRunner ar = new AnsibleRunner(AnsibleCommand.AdHoc);
    ar.module = module;
    ar.arg = arg;
    return ar;
  }

  public static AnsibleRunner playbook(String playbook) {
    AnsibleRunner ar = new AnsibleRunner(AnsibleCommand.Playbook);
    ar.playbook = playbook;
    return ar;
  }

  /**
   * Splits up a command and its arguments inf form of a string into a list of strings.
   * @param commandline  String with a possibly complex command and arguments
   * @return a list of arguments
   */
  public static List<String> tokenizeCommand(String commandline) {
    List<String> tokens = ArgumentTokenizer.tokenize(commandline, true);
    List<String> args = new ArrayList<>();
    for (String token : tokens) {
      args.add(token.replaceAll("\\\\", "\\\\").replaceAll("^\"|\"$", ""));
    }
    return args;
  }

  private boolean done = false;
  private String output;

  private final AnsibleCommand type;
  private String module;
  private String arg;
  private String extraArgs;
  private String vaultPass;
  private String playbook;
  private boolean debug;
  private Path tempDirectory;
  private boolean retainTempDirectory;
  private final List<String> limits = new ArrayList<>();
  private int result;
  private boolean doParseOutput;
  private PluginLogger logger = null;

  private AnsibleRunner(AnsibleCommand type) {
    this.type = type;
  }

  public AnsibleRunner limit(String host) {
    limits.add(host);
    return this;
  }

  public AnsibleRunner limit(INodeSet nodes) {
    limits.addAll(nodes.getNodeNames());
    return this;
  }

  /**
   * Additional arguments to pass to the process
   * @param args  extra commandline which gets appended to the base command and arguments
   */
  public AnsibleRunner extraArgs(String args) {
    if (args != null && args.length() > 0) {
      extraArgs = args;
    }
    return this;
  }

  /**
   * Vault Password
   * @param pass  vault password to be used to decrypt group variables
   */
  public AnsibleRunner vaultPass(String pass) {
    if (pass != null && pass.length() > 0) {
      vaultPass = pass;
    }
    return this;
  }

  /**
   * Run Ansible with -vvvv and print the command and output to the console / log
   */
  public AnsibleRunner debug() {
    return debug(true);
  }

  /**
   * Run Ansible with -vvvv and print the command and output to the console / log
   */
  public AnsibleRunner debug(boolean debug) {
    this.debug = debug;
    return this;
  }

  /**
   * Keep the temp dir around, dont' delete it.
   */
  public AnsibleRunner retainTempDirectory() {
    return retainTempDirectory(true);
  }

  public AnsibleRunner setLogger(PluginLogger logger) {
    this.logger = logger;
    return this;
  }

  /**
   * Keep the temp dir around, dont' delete it.
   */
  public AnsibleRunner retainTempDirectory(boolean retainTempDirectory) {
    this.retainTempDirectory = retainTempDirectory;
    return this;
  }

  /**
   * Specify in which directory Ansible is run.
   * If none is specified, a temporary directory will be created automatically.
   */
  public AnsibleRunner tempDirectory(Path dir) {
    if (dir != null) {
      this.tempDirectory = dir;
    }
    return this;
  }

  public void deleteTempDirectory(Path tempDirectory) throws IOException {
      Files.walkFileTree(tempDirectory, new SimpleFileVisitor<Path>() {
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
          Files.delete(file);
          return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
          Files.delete(dir);
          return FileVisitResult.CONTINUE;
        }
      });
  }

  public int run() throws Exception {
    if (done) {
      throw new IllegalStateException("already done");
    }
    done = true;

    if (tempDirectory == null) {
      tempDirectory = Files.createTempDirectory("ansible-rundeck");
    }

    File tempFile = null;
    File tempVaultFile = null;

    List<String> procArgs = new ArrayList<>();
    procArgs.add(type.command);

    // parse arguments
    if (type == AnsibleCommand.AdHoc) {
      procArgs.add("all");

      procArgs.add("-m");
      procArgs.add(module);

      if (arg != null && arg.length() > 0) {
        procArgs.add("-a");
        procArgs.add(arg);
      }
      procArgs.add("-t");
      procArgs.add(tempDirectory.toFile().getAbsolutePath());
    } else if (type == AnsibleCommand.Playbook) {
      procArgs.add(playbook);
    }

    if (limits.size() == 1) {
      procArgs.add("-l");
      procArgs.add(limits.get(0));
    } else if (limits.size() > 1) {
      tempFile = File.createTempFile("ansible-runner", "targets");
      StringBuilder sb = new StringBuilder();
      for (String limit : limits) {
        sb.append(limit).append("\n");
      }
      Files.write(tempFile.toPath(), sb.toString().getBytes());

      procArgs.add("-l");
      procArgs.add("@" + tempFile.getAbsolutePath());
    }

    if (debug) {
      procArgs.add("-vvvv");
    } else {
      procArgs.add("-v");
    }

    if (extraArgs != null && extraArgs.length() > 0) {
      if (debug) {
        System.out.println("extraArgs: " + extraArgs);
        System.out.println("tokenized: " + tokenizeCommand(extraArgs));
      }
      procArgs.addAll(tokenizeCommand(extraArgs));
    }

    if (vaultPass != null && vaultPass.length() > 0) {
      tempVaultFile = File.createTempFile("ansible-runner", "vault");
      Files.write(tempVaultFile.toPath(), vaultPass.toString().getBytes());
      procArgs.add("--vault-password-file" + "=" + tempVaultFile.getAbsolutePath());
    }

    if (logger != null) {
      logger.log(3, "[ansible-exec]: executing command: " + procArgs);
    }

    // execute the ansible process
    ProcessBuilder processBuilder = new ProcessBuilder()
      .command(procArgs)
      .directory(tempDirectory.toFile()); // set cwd
    Process proc = null;

    try {
      proc = processBuilder.start();
      proc.getOutputStream().close();
      Thread outthread = Streams.copyStreamThread(proc.getInputStream(), System.out);
      outthread.start();
      result = proc.waitFor();
      System.out.flush();
      outthread.join();

      if (result != 0) {
        // fetch the error message returned by ansible from stdout
        StringBuilder inputStringBuilder = new StringBuilder();
        BufferedReader bufferedReader = 
        		new BufferedReader(new InputStreamReader(proc.getErrorStream(), "UTF-8"));
        String line = bufferedReader.readLine();
        while(line != null){
            inputStringBuilder.append(line);
            inputStringBuilder.append('\n');
            line = bufferedReader.readLine();
        }
        String msg = inputStringBuilder.toString();
        // if stdout does noy return any message just print
        // a generic error message.
        if (msg.length() < 1){
           msg = "ERROR: Ansible execution returned with non zero code.";
        }
        throw new AnsibleStepException(msg,AnsibleFailureReason.AnsibleNonZero);
      }
    } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AnsibleStepException("ERROR: Ansible Execution Interrupted.", e, AnsibleFailureReason.Interrupted);
    } catch (IOException e) {
            throw new AnsibleStepException("ERROR: Ansible IO failure.", e, AnsibleFailureReason.IOFailure);
    } catch (AnsibleStepException e) {
            throw e;
    } catch (Exception e) {
            throw new AnsibleStepException("ERROR: Ansible execution returned with non zero code.", e, AnsibleFailureReason.Unknown);
    } finally {
        // Make sure to always cleanup on failure and success
        proc.getErrorStream().close();
        proc.getInputStream().close();
        if (tempFile != null && !tempFile.delete()) {
          tempFile.deleteOnExit();
        }
        if (tempVaultFile != null && !tempVaultFile.delete()) {
          tempVaultFile.deleteOnExit();
        }
        if (tempDirectory != null && !retainTempDirectory) {
          deleteTempDirectory(tempDirectory);
        }

        if (logger != null) {
          boolean success = 0 == result;
          logger.log(3, "[ansible-exec]: result code: " + result + ", success: " + success);
        }
    }

    return result;
  }

  public int getResult() {
    return result;
  }

}
