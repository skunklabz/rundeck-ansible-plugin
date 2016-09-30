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

import java.util.Map;
import java.util.List;
import java.util.ArrayList;

@Plugin(name = AnsiblePlaybookWorkflowStep.SERVICE_PROVIDER_NAME, service = ServiceNameConstants.WorkflowStep)
public class AnsiblePlaybookWorkflowStep extends AbstractAnsibleStep implements StepPlugin, Describable {

  public static final String SERVICE_PROVIDER_NAME = "com.batix.rundeck.AnsiblePlaybookWorkflowStep";

  @Override
  public void executeStep(PluginStepContext context, Map<String, Object> configuration) throws StepException {

    try {
      this.createRunner(context,configuration);
    } catch (Exception e) {
        throw new StepException("Error parsing module arguments.", e, AnsibleFailureReason.ParseArgumentsError);
    }

    // ansible runner will take care of handling exceptions, here handle only jobs specific stuff
    try {
        runner.run();
    } catch (AnsibleStepException e) {
        throw new StepException(e.getMessage(), e, e.getFailureReason());
    } catch (Exception e) {
        throw new StepException(e.getMessage(),e,AnsibleFailureReason.AnsibleError);

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
                    "Runs an Ansible Playbook.",
                    AnsiblePluginType.PLAYBOOK);
  }

}
