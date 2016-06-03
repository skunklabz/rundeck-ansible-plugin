package com.batix.rundeck;

import com.dtolabs.rundeck.core.common.IRundeckProject;
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

@Plugin(name = AnsiblePlaybookWorkflowStep.SERVICE_PROVIDER_NAME, service = ServiceNameConstants.WorkflowStep)
public class AnsiblePlaybookWorkflowStep implements StepPlugin, Describable {
  public static final String SERVICE_PROVIDER_NAME = "com.batix.rundeck.AnsiblePlaybookWorkflowStep";

  @Override
  public void executeStep(PluginStepContext context, Map<String, Object> configuration) throws StepException {
    String playbook = (String) configuration.get("playbook");
    String extraArgs = (String) configuration.get("extraArgs");

    AnsibleRunner runner = AnsibleRunner.playbook(playbook).limit(context.getNodes()).extraArgs(extraArgs);
    if ("true".equals(System.getProperty("ansible.debug"))) {
      runner.debug();
    }
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
      .build();
  }
}
