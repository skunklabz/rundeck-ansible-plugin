package com.batix.rundeck;

import com.dtolabs.rundeck.core.common.INodeSet;
import com.dtolabs.rundeck.core.common.NodeEntryImpl;
import com.dtolabs.rundeck.core.common.NodeSetImpl;
import com.dtolabs.rundeck.core.resources.ResourceModelSource;
import com.dtolabs.rundeck.core.resources.ResourceModelSourceException;
import com.dtolabs.rundeck.core.storage.ResourceMeta;
import org.rundeck.storage.api.Resource;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.ByteArrayOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

public class AnsibleResourceModelSource implements ResourceModelSource {

  final boolean gatherFacts;
  final boolean ignoreErrors;
  final String limit;
  final String extraArgs = "";
  final String ignoreTagPrefix;

  protected String vaultPass;
  protected Boolean debug = false;

  // ansible ssh args
  protected String sshUser;
  protected Boolean sshUsePassword;
  protected String sshPassword;
  protected String sshPrivateKey;
  protected String sshPass;
  protected Integer sshTimeout;

  // ansible become args
  protected Boolean become;
  protected String becomeMethod;
  protected String becomeUser;
  protected String becomePassword;

  public AnsibleResourceModelSource(Properties configuration) {

    gatherFacts = "true".equals(configuration.get("gatherFacts"));
    ignoreErrors = "true".equals(configuration.get("ignoreErrors"));
    limit = (String) configuration.get("limit");
    ignoreTagPrefix = (String) configuration.get("ignoreTagPrefix");

    sshUsePassword = "true".equals( configuration.get("askpass") );
    sshUser = (String) configuration.get("sshUser");
    sshPrivateKey = (String) configuration.get("sshPrivateKey");
    sshPassword = (String) configuration.get("sshPassword");
    sshTimeout =  (Integer)  configuration.get("sshTimeout");
    become = "true".equals( configuration.get("become") );
    becomeMethod = (String) configuration.get("becomeMethod");
    becomeUser = (String) configuration.get("becomeUser");
    becomePassword = (String)  configuration.get("becomePassword");

  }

  @Override
  public INodeSet getNodes() throws ResourceModelSourceException {
    NodeSetImpl nodes = new NodeSetImpl();

    Path tempDirectory;
    try {
      tempDirectory = Files.createTempDirectory("ansible-hosts");
    } catch (IOException e) {
        throw new ResourceModelSourceException("Error creating temporary directory.", e);
    }

    try {
      Files.copy(this.getClass().getClassLoader().getResourceAsStream("host-tpl.j2"), tempDirectory.resolve("host-tpl.j2"));
      Files.copy(this.getClass().getClassLoader().getResourceAsStream("gather-hosts.yml"), tempDirectory.resolve("gather-hosts.yml"));
    } catch (IOException e) {
        throw new ResourceModelSourceException("Error copying files.");
    }

    AnsibleRunner runner = AnsibleRunner.playbook("gather-hosts.yml");

    if ("true".equals(System.getProperty("ansible.debug"))) {
      runner.debug();
    }
    runner.tempDirectory(tempDirectory).retainTempDirectory();

    if (limit != null && limit.length() > 0) {
      List<String> limitList = new ArrayList<>();
      limitList.add(limit);
      runner.limit(limitList);
    }

    StringBuilder args = new StringBuilder();
    args.append("facts=").append(gatherFacts ? "True" : "False");
    args.append(" ").append("tmpdir='").append(tempDirectory.toFile().getAbsolutePath()).append("'");
    runner.extraArgs(args.toString());

    System.out.println(sshPassword);

    runner.sshUser(sshUser)
          .sshUsePassword(sshUsePassword)
          .sshPass(sshPassword)
          .sshPrivateKey(sshPrivateKey)
          .sshTimeout(sshTimeout)
          .become(become)
          .becomeMethod(becomeMethod)
          .becomeUser(becomeUser)
          .becomePassword(becomePassword);

    try {
      int status = runner.run();
    } catch (Exception e) {
      throw new ResourceModelSourceException("Error running playbook.", e);
    }

    try {
      if (new File(tempDirectory.toFile(), "data").exists()) {
        DirectoryStream<Path> directoryStream = Files.newDirectoryStream(tempDirectory.resolve("data"));
        for (Path factFile : directoryStream) {
          NodeEntryImpl node = new NodeEntryImpl();

          BufferedReader bufferedReader = Files.newBufferedReader(factFile, Charset.forName("utf-8"));
          JsonElement json = new JsonParser().parse(bufferedReader);
          bufferedReader.close();
          JsonObject root = json.getAsJsonObject();

          String hostname = root.get("inventory_hostname").getAsString();
          if (root.has("ansible_host")) {
            hostname = root.get("ansible_host").getAsString();
          } else if (root.has("ansible_ssh_host")) { // deprecated variable
            hostname = root.get("ansible_ssh_host").getAsString();
          }

          String nodename = root.get("inventory_hostname").getAsString();

          node.setHostname(hostname);
          node.setNodename(nodename);

          String username = System.getProperty("user.name"); // TODO better default?
          if (root.has("ansible_user")) {
            username = root.get("ansible_user").getAsString();
          } else if (root.has("ansible_ssh_user")) { // deprecated variable
            username = root.get("ansible_ssh_user").getAsString();
          } else if (root.has("ansible_user_id")) { // fact
            username = root.get("ansible_user_id").getAsString();
          }
          node.setUsername(username);

          HashSet<String> tags = new HashSet<>();
          for (JsonElement ele : root.getAsJsonArray("group_names")) {
            if (ignoreTagPrefix != null && ignoreTagPrefix.length() > 0 && ele.getAsString().startsWith(ignoreTagPrefix)) continue;
            tags.add(ele.getAsString());
          }
          node.setTags(tags);

          if (root.has("ansible_lsb")) {
            node.setDescription(root.getAsJsonObject("ansible_lsb").get("description").getAsString());
          } else {
            StringBuilder sb = new StringBuilder();

            if (root.has("ansible_distribution")) {
              sb.append(root.get("ansible_distribution").getAsString()).append(" ");
            }
            if (root.has("ansible_distribution_version")) {
              sb.append(root.get("ansible_distribution_version").getAsString()).append(" ");
            }

            if (sb.length() > 0) {
              node.setDescription(sb.toString().trim());
            }
          }

          // ansible_system     = Linux   = osFamily in Rundeck
          // ansible_os_family  = Debian  = osName in Rundeck

          if (root.has("ansible_system")) {
            node.setOsFamily(root.get("ansible_system").getAsString());
          }

          if (root.has("ansible_os_family")) {
            node.setOsName(root.get("ansible_os_family").getAsString());
          }

          if (root.has("ansible_architecture")) {
            node.setOsArch(root.get("ansible_architecture").getAsString());
          }

          if (root.has("ansible_kernel")) {
            node.setOsVersion(root.get("ansible_kernel").getAsString());
          }

          // JSON-Path -> Attribute-Name
          Map<String, String> interestingItems = new HashMap<>();

          interestingItems.put("ansible_form_factor", "form_factor");

          interestingItems.put("ansible_system_vendor", "system_vendor");

          interestingItems.put("ansible_product_name", "product_name");
          interestingItems.put("ansible_product_version", "product_version");
          interestingItems.put("ansible_product_serial", "product_serial");

          interestingItems.put("ansible_bios_version", "bios_version");
          interestingItems.put("ansible_bios_date", "bios_date");

          interestingItems.put("ansible_machine_id", "machine_id");

          interestingItems.put("ansible_virtualization_type", "virtualization_type");
          interestingItems.put("ansible_virtualization_role", "virtualization_role");

          interestingItems.put("ansible_selinux", "selinux");
          interestingItems.put("ansible_fips", "fips");

          interestingItems.put("ansible_service_mgr", "service_mgr");
          interestingItems.put("ansible_pkg_mgr", "pkg_mgr");

          interestingItems.put("ansible_distribution", "distribution");
          interestingItems.put("ansible_distribution_version", "distribution_version");
          interestingItems.put("ansible_distribution_major_version", "distribution_major_version");
          interestingItems.put("ansible_distribution_release", "distribution_release");
          interestingItems.put("ansible_lsb.codename", "lsb_codename");

          interestingItems.put("ansible_domain", "domain");

          interestingItems.put("ansible_date_time.tz", "tz");
          interestingItems.put("ansible_date_time.tz_offset", "tz_offset");

          interestingItems.put("ansible_processor_count", "processor_count");
          interestingItems.put("ansible_processor_cores", "processor_cores");
          interestingItems.put("ansible_processor_vcpus", "processor_vcpus");
          interestingItems.put("ansible_processor_threads_per_core", "processor_threads_per_core");

          interestingItems.put("ansible_userspace_architecture", "userspace_architecture");
          interestingItems.put("ansible_userspace_bits", "userspace_bits");

          interestingItems.put("ansible_memtotal_mb", "memtotal_mb");
          interestingItems.put("ansible_swaptotal_mb", "swaptotal_mb");
          interestingItems.put("ansible_processor.0", "processor0");
          interestingItems.put("ansible_processor.1", "processor1");

          for (Map.Entry<String, String> item : interestingItems.entrySet()) {
            String[] itemParts = item.getKey().split("\\.");

            if (itemParts.length > 1) {
              JsonElement ele = root;
              for (String itemPart : itemParts) {
                if (ele.isJsonArray() && itemPart.matches("^\\d+$") && ele.getAsJsonArray().size() > Integer.parseInt(itemPart)) {
                  ele = ele.getAsJsonArray().get(Integer.parseInt(itemPart));
                } else if (ele.isJsonObject() && ele.getAsJsonObject().has(itemPart)) {
                  ele = ele.getAsJsonObject().get(itemPart);
                } else {
                  ele = null;
                  break;
                }
              }

              if (ele != null && ele.isJsonPrimitive() && ele.getAsString().length() > 0) {
                node.setAttribute(item.getValue(), ele.getAsString());
              }
            } else {
              if (root.has(item.getKey())
                && root.get(item.getKey()).isJsonPrimitive()
                && root.get(item.getKey()).getAsString().length() > 0) {
                node.setAttribute(item.getValue(), root.get(item.getKey()).getAsString());
              }
            }
          }

          nodes.putNode(node);
        }
        directoryStream.close();
      }
    } catch (IOException e) {
      throw new ResourceModelSourceException("Error reading facts.", e);
    }

    try {
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
    } catch (IOException e) {
      throw new ResourceModelSourceException("Error deleting temporary directory.", e);
    }

    return nodes;
  }
}
