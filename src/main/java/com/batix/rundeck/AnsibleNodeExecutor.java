package com.batix.rundeck;

import com.dtolabs.rundeck.core.common.INodeEntry;
import com.dtolabs.rundeck.core.common.IRundeckProject;
import com.dtolabs.rundeck.core.common.ProjectManager;
import com.dtolabs.rundeck.core.execution.ExecutionContext;
import com.dtolabs.rundeck.plugins.PluginLogger;
import com.dtolabs.rundeck.core.execution.service.NodeExecutor;
import com.dtolabs.rundeck.core.execution.service.NodeExecutorResult;
import com.dtolabs.rundeck.core.execution.service.NodeExecutorResultImpl;
import com.dtolabs.rundeck.core.plugins.Plugin;
import com.dtolabs.rundeck.core.plugins.configuration.Describable;
import com.dtolabs.rundeck.core.plugins.configuration.Description;
import com.dtolabs.rundeck.core.plugins.configuration.PropertyUtil;
import com.dtolabs.rundeck.plugins.ServiceNameConstants;
import com.dtolabs.rundeck.plugins.util.DescriptionBuilder;
import com.google.gson.JsonObject;
import com.dtolabs.rundeck.core.storage.ResourceMeta;
import org.rundeck.storage.api.Resource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

@Plugin(name = AnsibleNodeExecutor.SERVICE_PROVIDER_NAME, service = ServiceNameConstants.NodeExecutor)
public class AnsibleNodeExecutor implements NodeExecutor, Describable {
  public static final String SERVICE_PROVIDER_NAME = "com.batix.rundeck.AnsibleNodeExecutor";

  @Override
  public NodeExecutorResult executeCommand(ExecutionContext context, String[] command, INodeEntry node) {

      StringBuilder cmdArgs = new StringBuilder();
      ProjectManager projectManager = context.getFramework().getProjectManager();
      IRundeckProject project = projectManager.getFrameworkProject(context.getFrameworkProject());
      cmdArgs.append("executable=").append(project.getProperty("executable"));

      AnsibleRunner runner = null;

      for (String cmd : command) {
        cmdArgs.append(" '").append(cmd).append("'");
      }

      try {
        Boolean sshUsePassword = project.hasProperty("askpass") ? Boolean.valueOf( project.getProperty("askpass") ) : false;
        String sshUser = project.hasProperty("sshUser") ? project.getProperty("sshUser") : null;
        String sshPrivateKey = project.hasProperty("sshPrivateKey") ? project.getProperty("sshPrivateKey") : null;
        String sshPassword = project.hasProperty("sshpassword") ? project.getProperty("sshpassword") : null;
        Integer sshTimeout = project.hasProperty("sshTimeout") ? Integer.valueOf( project.getProperty("sshTimeout") ) : null;
        Boolean become = project.hasProperty("become") ? Boolean.valueOf( project.getProperty("become") ) : false;
        String becomeMethod = project.hasProperty("becomeMethod") ? project.getProperty("becomeMethod") : null;
        String becomeUser = project.hasProperty("becomeUser") ? project.getProperty("becomeUser") : null;
        String becomePassword = project.hasProperty("becomePassword") ? project.getProperty("becomePassword") : null;


        runner = AnsibleRunner.adHoc("shell", cmdArgs.toString())
                              .limit(node.getNodename())
                              .sshUser(sshUser)
                              .sshUsePassword(sshUsePassword)
                              .sshPass(sshPassword)
                              .sshPrivateKey(sshPrivateKey)
                              .sshTimeout(sshTimeout)
                              .become(become)
                              .becomeMethod(becomeMethod)
                              .becomeUser(becomeUser)
                              .becomePassword(becomePassword);
      } catch (Exception e) {
         return NodeExecutorResultImpl.createFailure(AnsibleFailureReason.AnsibleError, "Error Parsing project arguments, " + e.getMessage(), e, node, runner.getResult());
      }

      try {
        runner.run();
      } catch (Exception e) {
        return NodeExecutorResultImpl.createFailure(AnsibleFailureReason.AnsibleError, "Error Executing Ansible command, " + e.getMessage(), e, node, runner.getResult());
      }
      return NodeExecutorResultImpl.createSuccess(node);
  }

  @Override
  public Description getDescription() {
    return AnsiblePluginDescription.getAnsiblePluginPlaybookDesc( 
                                    SERVICE_PROVIDER_NAME, 
                                    "Ansible Ad-Hoc Node Executor", 
                                    "Runs Ansible Ad-Hoc commands on the nodes using the shell module.", 
                                    AnsiblePluginType.EXECUTOR );
  }

}
