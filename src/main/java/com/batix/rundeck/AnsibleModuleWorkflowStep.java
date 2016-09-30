package com.batix.rundeck;

import com.dtolabs.rundeck.core.common.IRundeckProject;
import com.dtolabs.rundeck.core.execution.workflow.steps.StepException;
import com.dtolabs.rundeck.core.plugins.Plugin;
import com.dtolabs.rundeck.core.plugins.configuration.Describable;
import com.dtolabs.rundeck.core.plugins.configuration.Description;
import com.dtolabs.rundeck.core.plugins.configuration.PropertyUtil;
import com.dtolabs.rundeck.core.plugins.configuration.PropertyScope;
import com.dtolabs.rundeck.plugins.PluginLogger;
import com.dtolabs.rundeck.plugins.ServiceNameConstants;
import com.dtolabs.rundeck.plugins.step.PluginStepContext;
import com.dtolabs.rundeck.plugins.step.StepPlugin;
import com.dtolabs.rundeck.plugins.util.DescriptionBuilder;
import org.apache.tools.ant.Project;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;

@Plugin(name = AnsibleModuleWorkflowStep.SERVICE_PROVIDER_NAME, service = ServiceNameConstants.WorkflowStep)
public class AnsibleModuleWorkflowStep extends AbstractAnsibleStep implements StepPlugin, Describable {

  public static final String SERVICE_PROVIDER_NAME = "com.batix.rundeck.AnsibleModuleWorkflowStep";

  @Override
  public void executeStep(PluginStepContext context, Map<String, Object> configuration) throws StepException {

    try {
      this.createRunner(context, configuration);
    } catch (Exception e) {
        throw new StepException("Error parsing module arguments.", e, AnsibleFailureReason.ParseArgumentsError);
    }

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
      return AnsiblePluginType.MODULE;
  }

  @Override
  public AnsibleRunner getRunner() {
      return AnsibleRunner.adHoc(module, args);
  }

  @Override
  public Description getDescription() {
     return AnsiblePluginDescription.getAnsiblePluginPlaybookDesc(
                    SERVICE_PROVIDER_NAME,
                    "Ansible Module",
                    "Runs an Ansible Module on selected nodes.",
                    AnsiblePluginType.MODULE);
  }
}
