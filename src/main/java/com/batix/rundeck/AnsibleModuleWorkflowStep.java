package com.batix.rundeck;

import com.dtolabs.rundeck.core.execution.workflow.steps.StepException;
import com.dtolabs.rundeck.core.plugins.Plugin;
import com.dtolabs.rundeck.core.plugins.configuration.Describable;
import com.dtolabs.rundeck.core.plugins.configuration.Description;
import com.dtolabs.rundeck.core.plugins.configuration.PropertyUtil;
import com.dtolabs.rundeck.plugins.PluginLogger;
import com.dtolabs.rundeck.plugins.ServiceNameConstants;
import com.dtolabs.rundeck.plugins.step.PluginStepContext;
import com.dtolabs.rundeck.plugins.step.StepPlugin;
import com.dtolabs.rundeck.plugins.util.DescriptionBuilder;
import org.apache.tools.ant.Project;

import java.util.Map;

@Plugin(name = AnsibleModuleWorkflowStep.SERVICE_PROVIDER_NAME, service = ServiceNameConstants.WorkflowStep)
public class AnsibleModuleWorkflowStep implements StepPlugin, Describable {
  public static final String SERVICE_PROVIDER_NAME = "com.batix.rundeck.AnsibleModuleWorkflowStep";

  @Override
  public void executeStep(PluginStepContext context, Map<String, Object> configuration) throws StepException {
    String module = (String) configuration.get("module");
    String args = (String) configuration.get("args");

    AnsibleRunner runner = AnsibleRunner.adHoc(module, args).limit(context.getNodes());
    int result;
    try {
      result = runner.run();
    } catch (Exception e) {
      throw new StepException("Error running Ansible.", e, AnsibleFailureReason.AnsibleError);
    }

    PluginLogger logger = context.getLogger();
    logger.log(Project.MSG_INFO, runner.getOutput());

    if (result != 0) {
      throw new StepException("Ansible exited with non-zero code.", AnsibleFailureReason.AnsibleNonZero);
    }
  }

  @Override
  public Description getDescription() {
    return DescriptionBuilder.builder()
      .name(SERVICE_PROVIDER_NAME)
      .title("Ansible Module")
      .description("Runs an Ansible Module on selected node.")
      .property(PropertyUtil.string(
        "module",
        "Module",
        "Module name",
        true,
        null
      ))
      .property(PropertyUtil.string(
        "args",
        "Arguments",
        "Arguments to pass to the module",
        false,
        null
      ))
      .build();
  }
}
