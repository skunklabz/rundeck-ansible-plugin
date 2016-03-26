package com.batix.rundeck;

import com.dtolabs.rundeck.core.common.INodeEntry;
import com.dtolabs.rundeck.core.common.IRundeckProject;
import com.dtolabs.rundeck.core.common.ProjectManager;
import com.dtolabs.rundeck.core.execution.ExecutionContext;
import com.dtolabs.rundeck.core.execution.impl.jsch.JschScpFileCopier;
import com.dtolabs.rundeck.core.execution.service.DestinationFileCopier;
import com.dtolabs.rundeck.core.execution.service.FileCopierException;
import com.dtolabs.rundeck.core.plugins.Plugin;
import com.dtolabs.rundeck.core.plugins.configuration.Describable;
import com.dtolabs.rundeck.core.plugins.configuration.Description;
import com.dtolabs.rundeck.core.plugins.configuration.PropertyUtil;
import com.dtolabs.rundeck.plugins.ServiceNameConstants;
import com.dtolabs.rundeck.plugins.util.DescriptionBuilder;

import java.io.File;
import java.io.InputStream;

@Plugin(name = AnsibleFileCopier.SERVICE_PROVIDER_NAME, service = ServiceNameConstants.FileCopier)
public class AnsibleFileCopier implements DestinationFileCopier, Describable {
  public static final String SERVICE_PROVIDER_NAME = "com.batix.rundeck.AnsibleFileCopier";

  @Override
  public String copyFileStream(ExecutionContext context, InputStream input, INodeEntry node, String destination) throws FileCopierException {
    return doFileCopy(context, null, input, null, node, destination);
  }

  @Override
  public String copyFile(ExecutionContext context, File file, INodeEntry node, String destination) throws FileCopierException {
    return doFileCopy(context, file, null, null, node, destination);
  }

  @Override
  public String copyScriptContent(ExecutionContext context, String script, INodeEntry node, String destination) throws FileCopierException {
    return doFileCopy(context, null, null, script, node, destination);
  }

  @Override
  public String copyFileStream(ExecutionContext context, InputStream input, INodeEntry node) throws FileCopierException {
    return doFileCopy(context, null, input, null, node, null);
  }

  @Override
  public String copyFile(ExecutionContext context, File file, INodeEntry node) throws FileCopierException {
    return doFileCopy(context, file, null, null, node, null);
  }

  @Override
  public String copyScriptContent(ExecutionContext context, String script, INodeEntry node) throws FileCopierException {
    return doFileCopy(context, null, null, script, node, null);
  }

  private String doFileCopy(
    final ExecutionContext context,
    final File scriptFile,
    final InputStream input,
    final String script,
    final INodeEntry node,
    String destinationPath
  ) throws FileCopierException {
    if (destinationPath == null) {
      String identity = (context.getDataContext() != null && context.getDataContext().get("job") != null) ?
        context.getDataContext().get("job").get("execid") : null;
      destinationPath = JschScpFileCopier.generateRemoteFilepathForNode(
        node,
        context.getFramework().getFrameworkProjectMgr().getFrameworkProject(context.getFrameworkProject()),
        context.getFramework(),
        scriptFile != null ? scriptFile.getName() : "dispatch-script",
        null,
        identity
      );
    }

    ProjectManager projectManager = context.getFramework().getProjectManager();
    IRundeckProject frameworkProject = projectManager.getFrameworkProject(context.getFrameworkProject());
    String extraArgs = frameworkProject.hasProperty("extraArgs") ? frameworkProject.getProperty("extraArgs") : null;

    File localTempFile = scriptFile != null ?
      scriptFile : JschScpFileCopier.writeTempFile(context, null, input, script);

    String cmdArgs = "src='" + localTempFile.getAbsolutePath() + "' dest='" + destinationPath + "'";

    AnsibleRunner runner = AnsibleRunner.adHoc("copy", cmdArgs).limit(node.getNodename()).extraArgs(extraArgs);
    int result;
    try {
      result = runner.run();
    } catch (Exception e) {
      throw new FileCopierException("Error running Ansible.", AnsibleFailureReason.AnsibleError, e);
    }

    if (result != 0) {
      throw new FileCopierException("Ansible exited with non-zero code.", AnsibleFailureReason.AnsibleNonZero);
    }

    return destinationPath;
  }

  @Override
  public Description getDescription() {
    return DescriptionBuilder.builder()
      .name(SERVICE_PROVIDER_NAME)
      .title("Ansible File Copier")
      .description("Sends a file to a node via the copy module.")
      .property(PropertyUtil.string(
        "extraArgs",
        "Extra Arguments",
        "Extra Arguments for the Ansible process",
        false,
        null
      ))
      .build();
  }
}
