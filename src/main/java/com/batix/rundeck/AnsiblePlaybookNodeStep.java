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

import java.util.Map;
import java.util.List;
import java.util.ArrayList;

@Plugin(name = AnsiblePlaybookNodeStep.SERVICE_PROVIDER_NAME, service = ServiceNameConstants.WorkflowNodeStep)
public class AnsiblePlaybookNodeStep extends AbstractAnsibleStep implements NodeStepPlugin, Describable {

  public static final String SERVICE_PROVIDER_NAME = "com.batix.rundeck.AnsiblePlaybookNodeStep";

  @Override
  public void executeNodeStep(PluginStepContext context, Map<String, Object> configuration, INodeEntry entry) throws NodeStepException {

    try {
      this.createRunner(context,configuration,entry);
    } catch (Exception e) {
      throw new NodeStepException("Error parsing module arguments.", e, AnsibleFailureReason.ParseArgumentsError, entry.getNodename());
    }

    try {
        this.runner.run();
    } catch (AnsibleStepException e) {
        throw new NodeStepException("Error running Ansible Node Step.", e, e.getFailureReason(), entry.getNodename());
    } catch (Exception e) {
        throw new NodeStepException(e.getMessage(), e, AnsibleFailureReason.AnsibleError, entry.getNodename());
    }
  }

  @Override
  public AnsiblePluginType getPluginType() {
      return AnsiblePluginType.PLAYBOOK;
  }

  @Override
  public AnsibleRunner getRunner() {
      return AnsibleRunner.playbook(playbook);
  }


  @Override
  public Description getDescription() {
     return AnsiblePluginDescription.getAnsiblePluginPlaybookDesc(
                    SERVICE_PROVIDER_NAME,
                    "Ansible Playbook",
                    "Runs an Ansible Playbook on selected nodes.",
                    AnsiblePluginType.PLAYBOOK);
  }
}
