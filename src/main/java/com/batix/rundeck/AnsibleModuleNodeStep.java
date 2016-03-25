package com.batix.rundeck;

import com.dtolabs.rundeck.core.common.INodeEntry;
import com.dtolabs.rundeck.core.execution.workflow.steps.node.NodeStepException;
import com.dtolabs.rundeck.core.plugins.Plugin;
import com.dtolabs.rundeck.core.plugins.configuration.Describable;
import com.dtolabs.rundeck.core.plugins.configuration.Description;
import com.dtolabs.rundeck.core.plugins.configuration.PropertyUtil;
import com.dtolabs.rundeck.plugins.PluginLogger;
import com.dtolabs.rundeck.plugins.ServiceNameConstants;
import com.dtolabs.rundeck.plugins.step.NodeStepPlugin;
import com.dtolabs.rundeck.plugins.step.PluginStepContext;
import com.dtolabs.rundeck.plugins.util.DescriptionBuilder;
import org.apache.tools.ant.Project;

import java.util.Map;

@Plugin(name = AnsibleModuleNodeStep.SERVICE_PROVIDER_NAME, service = ServiceNameConstants.WorkflowNodeStep)
public class AnsibleModuleNodeStep implements NodeStepPlugin, Describable {
  public static final String SERVICE_PROVIDER_NAME = "com.batix.rundeck.AnsibleModuleNodeStep";

  @Override
  public void executeNodeStep(PluginStepContext context, Map<String, Object> configuration, INodeEntry entry) throws NodeStepException {
    String module = (String) configuration.get("module");
    String args = (String) configuration.get("args");
    String options = (String) configuration.get("options");

    AnsibleRunner runner = AnsibleRunner.adHoc(module, args, options).limit(entry.getNodename());
    int result;
    try {
      result = runner.run();
    } catch (Exception e) {
      throw new NodeStepException("Error running Ansible.", e, AnsibleFailureReason.AnsibleError, entry.getNodename());
    }

    PluginLogger logger = context.getLogger();
    logger.log(Project.MSG_INFO, runner.getOutput());

    if (result != 0) {
      throw new NodeStepException("Ansible exited with non-zero code.", AnsibleFailureReason.AnsibleNonZero, entry.getNodename());
    }
  }

  @Override
  public Description getDescription() {
    return DescriptionBuilder.builder()
      .name(SERVICE_PROVIDER_NAME)
      .title("Ansible Module")
      .description("Runs an Ansible Module on a single node.")
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
      .property(PropertyUtil.string(
        "options",
        "Options",
        "Options to pass",
        false,
        null
      ))
      .build();
  }
}
