package com.batix.rundeck;

import com.dtolabs.rundeck.core.execution.workflow.steps.StepException;
import com.dtolabs.rundeck.core.plugins.Plugin;
import com.dtolabs.rundeck.core.plugins.configuration.Describable;
import com.dtolabs.rundeck.core.plugins.configuration.Description;
import com.dtolabs.rundeck.core.plugins.configuration.PropertyScope;
import com.dtolabs.rundeck.core.plugins.configuration.PropertyUtil;
import com.dtolabs.rundeck.core.storage.ResourceMeta;
import com.dtolabs.rundeck.plugins.PluginLogger;
import com.dtolabs.rundeck.plugins.ServiceNameConstants;
import com.dtolabs.rundeck.plugins.step.PluginStepContext;
import com.dtolabs.rundeck.plugins.step.StepPlugin;
import com.dtolabs.rundeck.plugins.util.DescriptionBuilder;
import org.apache.tools.ant.Project;
import org.rundeck.storage.api.Resource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

@Plugin(name = AnsiblePlaybookWorkflowStep.SERVICE_PROVIDER_NAME, service = ServiceNameConstants.WorkflowStep)
public class AnsiblePlaybookWorkflowStep implements StepPlugin, Describable {
  public static final String SERVICE_PROVIDER_NAME = "com.batix.rundeck.AnsiblePlaybookWorkflowStep";

  @Override
  public void executeStep(PluginStepContext context, Map<String, Object> configuration) throws StepException {
    String playbook = (String) configuration.get("playbook");
    String extraArgs = (String) configuration.get("extraArgs");
    String vaultPass = (String) configuration.get("vaultPass");
    String sshPass = (String) configuration.get("sshPassword");
    final PluginLogger logger = context.getLogger();
    Map<java.lang.String,java.lang.String> jobConfig = context.getDataContext().get("job");

    if (vaultPass != null && vaultPass.length() > 0) {
        Resource<ResourceMeta> resource  = context.getExecutionContext().getStorageTree().getResource(vaultPass);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
          resource.getContents().writeContent(byteArrayOutputStream);
        } catch (IOException e) {
           throw new StepException("Error reading vault password from storage Tier.", e, AnsibleFailureReason.StorageTierAccessError);
        }
        vaultPass = new String(byteArrayOutputStream.toByteArray());
    } else {
        vaultPass = "";
    }

    AnsibleRunner runner = AnsibleRunner.playbook(playbook).limit(context.getNodes()).extraArgs(extraArgs).vaultPass(vaultPass).sshPass(sshPass).stream();

    if (jobConfig.get("loglevel").equals("DEBUG")) {
      runner.debug();
    }

    runner.listener(new AnsibleRunner.Listener() {
      @Override
      public void output(String line) {
        logger.log(Project.MSG_INFO, line);
      }
    });

    int result;
    try {
      result = runner.run();
    } catch (Exception e) {
      throw new StepException("Error running Ansible.", e, AnsibleFailureReason.AnsibleError);
    }

    if (result != 0) {
      throw new StepException("Ansible exited with non-zero code.", AnsibleFailureReason.AnsibleNonZero);
    }
  }

  @Override
  public Description getDescription() {
    return DescriptionBuilder.builder()
      .name(SERVICE_PROVIDER_NAME)
      .title("Ansible Playbook")
      .description("Runs an Ansible Playbook on selected nodes.")
      .property(PropertyUtil.string(
        "playbook",
        "Playbook",
        "Path to a playbook",
        true,
        null,
        new AnsiblePlaybookPropertyValidator()
      ))
      .property(PropertyUtil.string(
        "extraArgs",
        "Extra Arguments",
        "Extra Arguments for the Ansible process",
        false,
        null
      ))
      .property(PropertyUtil.string(
        "vaultPass",
        "Vault Password",
        "Vault Password used to decrypt group variables",
        false,
        null,
        null,
        PropertyScope.Unspecified,
        AnsibleCommon.getRenderParametersForStoragePath()
      ))
      .property(PropertyUtil.string(
        "sshPassword",
        "SSH Password",
        "ssh password passed to ansible job using Private data context.",
        false,
        "option.sshpassword",
        null,
        PropertyScope.Unspecified,
        AnsibleCommon.getRenderParametersForSshPassword()
      ))
      .build();
  }
}
