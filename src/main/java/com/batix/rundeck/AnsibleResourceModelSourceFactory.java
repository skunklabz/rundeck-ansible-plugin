package com.batix.rundeck;

import com.dtolabs.rundeck.core.plugins.Plugin;
import com.dtolabs.rundeck.core.plugins.configuration.ConfigurationException;
import com.dtolabs.rundeck.core.plugins.configuration.Describable;
import com.dtolabs.rundeck.core.plugins.configuration.Description;
import com.dtolabs.rundeck.core.plugins.configuration.PropertyUtil;
import com.dtolabs.rundeck.core.resources.ResourceModelSource;
import com.dtolabs.rundeck.core.resources.ResourceModelSourceFactory;
import com.dtolabs.rundeck.plugins.ServiceNameConstants;
import com.dtolabs.rundeck.plugins.util.DescriptionBuilder;

import java.util.Properties;

@Plugin(name = AnsibleResourceModelSourceFactory.SERVICE_PROVIDER_NAME, service = ServiceNameConstants.ResourceModelSource)
public class AnsibleResourceModelSourceFactory implements ResourceModelSourceFactory, Describable {
  public static final String SERVICE_PROVIDER_NAME = "com.batix.rundeck.AnsibleResourceModelSourceFactory";

  @Override
  public ResourceModelSource createResourceModelSource(Properties configuration) throws ConfigurationException {
    return new AnsibleResourceModelSource(configuration);
  }

  @Override
  public Description getDescription() {
    return DescriptionBuilder.builder()
      .name(SERVICE_PROVIDER_NAME)
      .title("Ansible Resource Model Source")
      .description("Imports nodes from Ansible's inventory.")
      .property(PropertyUtil.bool(
        "gatherFacts",
        "Gather Facts",
        "Gather fresh facts before importing? (recommended)",
        true,
        "true"
      ))
      .property(PropertyUtil.string(
        "limit",
        "Limit Targets",
        "Select only specified hosts/groups from the Ansible inventory. See http://docs.ansible.com/ansible/intro_patterns.html for syntax help.",
        false,
        ""
      ))
      .property(PropertyUtil.string(
        "extraArgs",
        "Extra Arguments",
        "Extra Arguments for the Ansible process.",
        false,
        null
      ))
      .property(PropertyUtil.string(
        "ignoreTagPrefix",
        "Ignore tags with this prefix",
        "Leave blank to import all groups as tags or specify a prefix string, groups starting with this string won't be added as tags.",
        false,
        ""
      )).property(PropertyUtil.string(
        "envTag",
        "Additional host tag",
        "This tag will be added to all hosts discovered by this source",
        false,
        ""
      ))
      .build();
  }
}
