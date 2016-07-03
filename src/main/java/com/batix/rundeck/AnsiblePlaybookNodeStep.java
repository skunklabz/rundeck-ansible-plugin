package com.batix.rundeck;

import com.dtolabs.rundeck.core.common.INodeEntry;
import com.dtolabs.rundeck.core.execution.workflow.steps.node.NodeStepException;
import com.dtolabs.rundeck.core.plugins.Plugin;
import com.dtolabs.rundeck.core.plugins.configuration.Describable;
import com.dtolabs.rundeck.core.plugins.configuration.Description;
import com.dtolabs.rundeck.core.plugins.configuration.PropertyScope;
import com.dtolabs.rundeck.core.plugins.configuration.PropertyUtil;
import com.dtolabs.rundeck.core.storage.ResourceMeta;
import com.dtolabs.rundeck.plugins.PluginLogger;
import com.dtolabs.rundeck.plugins.ServiceNameConstants;
import com.dtolabs.rundeck.plugins.step.NodeStepPlugin;
import com.dtolabs.rundeck.plugins.step.PluginStepContext;
import com.dtolabs.rundeck.plugins.util.DescriptionBuilder;
import org.apache.tools.ant.Project;
import org.rundeck.storage.api.Resource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;


@Plugin(name = AnsiblePlaybookNodeStep.SERVICE_PROVIDER_NAME, service = ServiceNameConstants.WorkflowNodeStep)
public class AnsiblePlaybookNodeStep implements NodeStepPlugin, Describable {
  public static final String SERVICE_PROVIDER_NAME = "com.batix.rundeck.AnsiblePlaybookNodeStep";

  @Override
  public void executeNodeStep(PluginStepContext context, Map<String, Object> configuration, INodeEntry entry) throws NodeStepException {
    String playbook = (String) configuration.get("playbook");
    String extraArgs = (String) configuration.get("extraArgs");
    String vaultPass = (String) configuration.get("vaultPass");

    final PluginLogger logger = context.getLogger();
    Map<java.lang.String,java.lang.String> jobConfig = context.getDataContext().get("job");

    if (vaultPass != null && vaultPass.length() > 0) {
        Resource<ResourceMeta> resource  = context.getExecutionContext().getStorageTree().getResource(vaultPass);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
          resource.getContents().writeContent(byteArrayOutputStream);
        } catch (IOException e) {
           throw new NodeStepException("Error reading vault password from storage Tier.", e, AnsibleFailureReason.StorageTierAccessError, entry.getNodename());
        }
        vaultPass = new String(byteArrayOutputStream.toByteArray());
    } else {
        vaultPass = "";
    }

    AnsibleRunner runner = AnsibleRunner.playbook(playbook).limit(entry.getNodename()).extraArgs(extraArgs).vaultPass(vaultPass).stream();

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
      throw new NodeStepException("Error running Ansible.", e, AnsibleFailureReason.AnsibleError, entry.getNodename());
    }

    if (result != 0) {
      throw new NodeStepException("Ansible exited with non-zero code.", AnsibleFailureReason.AnsibleNonZero, entry.getNodename());
    }
  }

  @Override
  public Description getDescription() {
    return DescriptionBuilder.builder()
      .name(SERVICE_PROVIDER_NAME)
      .title("Ansible Playbook")
      .description("Runs an Ansible Playbook on a single node.")
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
      .build();
  }
}
