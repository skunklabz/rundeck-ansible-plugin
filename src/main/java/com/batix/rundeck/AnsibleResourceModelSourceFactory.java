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

    return AnsiblePluginDescription.getAnsiblePluginPlaybookDesc(
                                    SERVICE_PROVIDER_NAME,
                                    "Ansible Resource Model Source",
                                    "Imports nodes from Ansible's inventory.",
                                    AnsiblePluginType.SOURCE );
  }

}
