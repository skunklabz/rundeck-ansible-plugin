package com.batix.rundeck;

import com.dtolabs.rundeck.core.cli.CLIUtils;
import com.dtolabs.rundeck.core.common.INodeEntry;
import com.dtolabs.rundeck.core.common.IRundeckProject;
import com.dtolabs.rundeck.core.common.ProjectManager;
import com.dtolabs.rundeck.core.execution.ExecArgList;
import com.dtolabs.rundeck.core.execution.ExecutionContext;
import com.dtolabs.rundeck.core.execution.service.NodeExecutor;
import com.dtolabs.rundeck.core.execution.service.NodeExecutorResult;
import com.dtolabs.rundeck.core.execution.service.NodeExecutorResultImpl;
import com.dtolabs.rundeck.core.plugins.Plugin;
import com.dtolabs.rundeck.core.plugins.configuration.Describable;
import com.dtolabs.rundeck.core.plugins.configuration.Description;
import com.dtolabs.rundeck.core.plugins.configuration.PropertyUtil;
import com.dtolabs.rundeck.core.utils.Converter;
import com.dtolabs.rundeck.plugins.ServiceNameConstants;
import com.dtolabs.rundeck.plugins.util.DescriptionBuilder;
import com.google.gson.JsonObject;

import java.util.Arrays;

@Plugin(name = AnsibleNodeExecutor.SERVICE_PROVIDER_NAME, service = ServiceNameConstants.NodeExecutor)
public class AnsibleNodeExecutor implements NodeExecutor, Describable {
  public static final String SERVICE_PROVIDER_NAME = "com.batix.rundeck.AnsibleNodeExecutor";

  @Override
  public NodeExecutorResult executeCommand(ExecutionContext context, String[] command, INodeEntry node) {
    String cmdArgs = "";
    ProjectManager projectManager = context.getFramework().getProjectManager();
    IRundeckProject frameworkProject = projectManager.getFrameworkProject(context.getFrameworkProject());
    cmdArgs += "executable=" + frameworkProject.getProperty("executable") + " ";
    final Converter<String, String> quote = CLIUtils.argumentQuoteForOperatingSystem(node.getOsFamily());
    String flatCmd = ExecArgList.joinAndQuote(Arrays.asList(command), quote);
    flatCmd = flatCmd.replaceAll("^'|'$", "");
    cmdArgs += flatCmd;

    AnsibleRunner runner = AnsibleRunner.adHoc("shell", cmdArgs).limit(node.getNodename());
    int result;
    try {
      result = runner.run();
    } catch (Exception e) {
      return NodeExecutorResultImpl.createFailure(AnsibleFailureReason.AnsibleError, e.getMessage(), e, node, runner.getResult());
    }

    JsonObject json = runner.getResults().get(0).results.get(0).json;

    if (json.has("stdout")) {
      String string = json.get("stdout").getAsString();
      if (string != null && string.length() > 0) {
        System.out.println(string);
      }
    }
    if (json.has("stderr")) {
      String string = json.get("stderr").getAsString();
      if (string != null && string.length() > 0) {
        System.err.println(string);
      }
    }

    if (result != 0) {
      return NodeExecutorResultImpl.createFailure(AnsibleFailureReason.AnsibleNonZero, "Ansible exited with non-zero code.", node, result);
    }

    return NodeExecutorResultImpl.createSuccess(node);
  }

  @Override
  public Description getDescription() {
    return DescriptionBuilder.builder()
      .name(SERVICE_PROVIDER_NAME)
      .title("Ansible Ad-Hoc Node Executor")
      .description("Runs Ansible Ad-Hoc commands on the nodes using the shell module.")
      .property(PropertyUtil.freeSelect(
        "executable",
        "Executable",
        "Change the remote shell used to execute the command. Should be an absolute path to the executable.",
        true,
        "/bin/bash",
        Arrays.asList("/bin/sh", "/bin/bash")
      ))
      .build();
  }
}
