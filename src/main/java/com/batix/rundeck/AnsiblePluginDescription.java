package com.batix.rundeck;

import com.dtolabs.rundeck.core.plugins.configuration.Description;
import com.dtolabs.rundeck.core.plugins.configuration.PropertyScope;
import com.dtolabs.rundeck.core.plugins.configuration.PropertyUtil;
import com.dtolabs.rundeck.core.plugins.configuration.Property;
import com.dtolabs.rundeck.core.plugins.configuration.PropertyValidator;
import com.dtolabs.rundeck.core.plugins.configuration.ValidationException;

import java.util.*;
import java.io.File;

public class AnsiblePluginDescription implements Description {

    private String name;
    private String title;
    private String description;
    private List<Property> properties;
    private Map<String, String> mapping;
    private Map<String, String> fwkmapping;

    public static class RenderParameterFactory {

      public enum DisplayType {
        SINGLE_LINE("SINGLE_LINE"),
        MULTI_LINE("MULTI_LINE"),
        CODE("CODE"),
        PASSWORD("PASSWORD"),
        STATIC_TEXT("STATIC_TEXT");
    
        final String value;
        DisplayType(String value) {
          this.value = value;
        }
      }

      public enum SelectionAccessorType {
        STORAGE_PATH("STORAGE_PATH");

        final String value;
        SelectionAccessorType(String value) {
          this.value = value;
        }
      }

      public enum ValueConversionFailureType {
        REMOVE("remove");

        final String value;
        ValueConversionFailureType(String value) {
          this.value = value;
        }
      }

      public enum ValueConversionType {
        STORAGE_PATH_AUTOMATIC_READ("STORAGE_PATH_AUTOMATIC_READ"),
        PRIVATE_DATA_CONTEXT("PRIVATE_DATA_CONTEXT");

        final String value;
        ValueConversionType(String value) {
          this.value = value;
        }
      }

      public enum GroupingType {
        SECONDARY("secondary");

        final String value;
        GroupingType(String value) {
          this.value = value;
        }
      }

      public static  Map<String, Object> getRenderParameters(
                                            DisplayType displayType, 
                                            SelectionAccessorType selectionAccessor, 
                                            String storageFileMetaFilter, 
                                            ValueConversionType valueConversion, 
                                            ValueConversionFailureType valueConversionFailure, 
                                            String groupName, 
                                            GroupingType grouping ) {

        Map<String, Object> renderParameter = new HashMap<>();

        if (displayType != null) {
          renderParameter.put("displayType", displayType.value);
        }

        if (selectionAccessor != null) {
          renderParameter.put("selectionAccessor", selectionAccessor.value);
        }

        if (storageFileMetaFilter != null) {
          renderParameter.put("storage-file-meta-filter", storageFileMetaFilter);
        }

        if (valueConversion != null) {
          renderParameter.put("valueConversion", valueConversion.value);
        }

        if (valueConversionFailure != null) {
          renderParameter.put("valueConversionFailure", valueConversionFailure.value);
        }

        if (groupName != null) {
          renderParameter.put("groupName", groupName);
        }

        if (grouping != null) {
          renderParameter.put("grouping", grouping.value);
        }

        return renderParameter;
      }

    }

    public enum AnsiblePluginDescriptionType {
    	PLAYBOOK, MODULE, EXECUTOR, SOURCE
    }

    public static List<String> getBecomeMethodsList() {
      return Arrays.asList("sudo", "su", "pbrun", "pfexec", "runas");
    }

    public static final PropertyValidator FILE_VALIDATOR = new PropertyValidator() {
        public boolean isValid(String value) throws ValidationException {
            return new File(value).isFile();
        }
    };

    public static final PropertyValidator INTEGER_VALIDATOR = new PropertyValidator() {
        public boolean isValid(String value) throws ValidationException {
           try {
               Integer.parseInt(value);
               return true;
           } catch (NumberFormatException ex) {
               return false;
           }
        }
    };

    public static Property PLAYBOOK_PROP = PropertyUtil.string(
              "playbook",
              "Playbook",
              "Path to a playbook",
              true,
              null,
              FILE_VALIDATOR
    );


    public static Property MODULE_PROP = PropertyUtil.string(
              "module",
              "Module",
              "Module name",
              true,
              null
    );

    public static Property ARGS_PROP = PropertyUtil.string(
              "args",
              "Arguments",
              "Arguments to pass to the module (-a/--args flag)",
              false,
              null
    );

    public static Property EXECUTABLE_PROP = PropertyUtil.freeSelect(
              "executable",
              "Executable",
              "Change the remote shell used to execute the command. Should be an absolute path to the executable.",
              true,
              "/bin/bash",
              Arrays.asList("/bin/sh", "/bin/bash")
    );

    public static Property GATHER_FACTS_PROP = PropertyUtil.bool(
              "gatherFacts",
              "Gather Facts",
              "Gather fresh facts before importing? (recommended)",
              true,
              "true"
    );

    public static Property IGNORE_ERRORS_PROP = PropertyUtil.bool(
              "ignoreErrors",
              "Ignore Host Discovery Errors",
              "Still display Successful Hosts even if some other hosts are unreachable ?",
              true,
              "true"
    );

    public static Property LIMIT_PROP = PropertyUtil.string(
              "limit",
              "Limit Targets",
              "Select only specified hosts/groups from the Ansible inventory. See http://docs.ansible.com/ansible/intro_patterns.html for syntax help.",
              false,
              ""
    );

    public static Property IGNORE_TAGS_PREFIX_PROP = PropertyUtil.string(
              "ignoreTagPrefix",
              "Ignore tags with this prefix",
              "Leave blank to import all groups as tags or specify a prefix string, groups starting with this string won't be added as tags.",
              false,
              ""
    );

    public static Property EXTRA_ARGS_PROP = PropertyUtil.string(
          "extraArgs",
          "Extra Arguments",
          "Extra Arguments for the Ansible process",
          false,
          null
    );

    public static Property VAULT_PASS_PROP = PropertyUtil.string(
          "vaultPass",
          "Vault Password",
          "Vault Password used to decrypt group variables",
          false,
          null,
          null,
          PropertyScope.Unspecified,
          RenderParameterFactory.getRenderParameters( null, 
                                                      RenderParameterFactory.SelectionAccessorType.STORAGE_PATH,
                                                      null,
                                                      null,
                                                      null,
                                                      null,
                                                      null)
    );

    public static Property ASK_PASS_PROP = PropertyUtil.bool(
          "askpass",
          "Use SSH password authentication.",
          null,
          false,
          null,
          PropertyScope.Unspecified,
          RenderParameterFactory.getRenderParameters( null,
                                                      null,
                                                      null,
                                                      null,
                                                      null,
                                                      "SSH Connection",
                                                      RenderParameterFactory.GroupingType.SECONDARY)
    );

    public static Property SSH_USER_PROP = PropertyUtil.string(
          "sshUser",
          "SSH User",
          "SSH User to authenticate as (default=rundeck).",
          false,
          null,
          null,
          PropertyScope.Unspecified,
          RenderParameterFactory.getRenderParameters( null,
                                                      null,
                                                      null,
                                                      null,
                                                      null,
                                                      "SSH Connection",
                                                      RenderParameterFactory.GroupingType.SECONDARY)
    );

    public static Property SSH_PRIVATE_KEY_PROP = PropertyUtil.string(
          "sshPrivateKey",
          "SSH Private Key File Path",
          "SSH private key file path to authenticate the connection.",
          false,
          null,
          null,
          PropertyScope.Unspecified,
          RenderParameterFactory.getRenderParameters( null,
                                                      null,
                                                      null,
                                                      null,
                                                      null,
                                                      "SSH Connection",
                                                      RenderParameterFactory.GroupingType.SECONDARY)
    );

    public static Property SSH_PASS_PROP = PropertyUtil.string(
          "sshPassword",
          "SSH Password",
          "SSH password passed to ansible job.",
          false,
          null,
          null,
          PropertyScope.Unspecified,
          RenderParameterFactory.getRenderParameters(RenderParameterFactory.DisplayType.PASSWORD,
                                                     null, 
                                                     null,
                                                     null,
                                                     null,
                                                     "SSH Connection",
                                                     RenderParameterFactory.GroupingType.SECONDARY)
    );

    public static Property SSH_TIMEOUT_PROP = PropertyUtil.integer(
          "sshTimeout",
          "SSH Timeout",
          "SSH timeout, override the SSH timeout in seconds (default=10).",
          false,
          null,
          INTEGER_VALIDATOR,
          PropertyScope.Unspecified,
          RenderParameterFactory.getRenderParameters( RenderParameterFactory.DisplayType.SINGLE_LINE,
                                                      null,
                                                      null,
                                                      null,
                                                      null, 
                                                      "SSH Connection",
                                                      RenderParameterFactory.GroupingType.SECONDARY)
    );

    public static Property BECOME_PROP = PropertyUtil.bool(
          "become",
          "Run operations with become (nopasswd implied).",
          null,
          false,
          null,
          PropertyScope.Unspecified,
          RenderParameterFactory.getRenderParameters( null,
                                                      null,
                                                      null,
                                                      null,
                                                      null,
                                                      "Privilege Escalation",
                                                      RenderParameterFactory.GroupingType.SECONDARY)
    );

    public static Property BECOME_USER_PROP = PropertyUtil.string(
          "becomeUser",
          "Privilege escalation user",
          "run operations as this user (default=root).",
          false,
          null,
          null,
          PropertyScope.Unspecified,
          RenderParameterFactory.getRenderParameters( null,
                                                      null,
                                                      null,
                                                      null,
                                                      null,
                                                      "Privilege Escalation",
                                                      RenderParameterFactory.GroupingType.SECONDARY)
    );

    public static Property BECOME_PASS_PROP = PropertyUtil.string(
          "becomePassword",
          "Privilege escalation Password",
          "Become password used for previlege escalation.",
          false,
          null,
          null,
          PropertyScope.Unspecified,
          RenderParameterFactory.getRenderParameters( RenderParameterFactory.DisplayType.PASSWORD,
                                                      null,
                                                      null,
                                                      null,
                                                      null,
                                                      "Privilege Escalation",
                                                      RenderParameterFactory.GroupingType.SECONDARY)
    );

    public static Property BECOME_METHOD_PROP = PropertyUtil.select(
          "becomeMethod",
          "Privilege escalation method",
          "privilege escalation method to use (default=sudo).",
          false,
          null,
          getBecomeMethodsList(),
          PropertyScope.Unspecified,
          RenderParameterFactory.getRenderParameters( null,
                                                      null,
                                                      null,
                                                      null,
                                                      null,
                                                      "Privilege Escalation",
                                                      RenderParameterFactory.GroupingType.SECONDARY)
    );

    public static AnsiblePluginDescription  getAnsiblePluginPlaybookDesc(String name, String title, String description, AnsiblePluginType type) {
    	AnsiblePluginDescription pluginDesc = new AnsiblePluginDescription(name, title, description);
        
        if (type == AnsiblePluginType.PLAYBOOK) {
          pluginDesc.getProperties().add(PLAYBOOK_PROP);
          pluginDesc.getProperties().add(EXTRA_ARGS_PROP);
          pluginDesc.getProperties().add(VAULT_PASS_PROP);
        }

        if (type == AnsiblePluginType.MODULE) {
          pluginDesc.getProperties().add(MODULE_PROP);
          pluginDesc.getProperties().add(ARGS_PROP);
        }

        if (type == AnsiblePluginType.EXECUTOR) {
          pluginDesc.getProperties().add(EXECUTABLE_PROP);
        }

        if (type == AnsiblePluginType.SOURCE) {
          pluginDesc.getProperties().add(GATHER_FACTS_PROP);
          pluginDesc.getProperties().add(IGNORE_ERRORS_PROP);
          pluginDesc.getProperties().add(LIMIT_PROP);
          pluginDesc.getProperties().add(IGNORE_TAGS_PREFIX_PROP);
        }

        pluginDesc.getProperties().add(ASK_PASS_PROP);
        pluginDesc.getProperties().add(SSH_USER_PROP);
        pluginDesc.getProperties().add(SSH_PRIVATE_KEY_PROP);
        pluginDesc.getProperties().add(SSH_PASS_PROP);
        pluginDesc.getProperties().add(SSH_TIMEOUT_PROP);
        pluginDesc.getProperties().add(BECOME_PROP);
        pluginDesc.getProperties().add(BECOME_USER_PROP);
        pluginDesc.getProperties().add(BECOME_PASS_PROP);
        pluginDesc.getProperties().add(BECOME_METHOD_PROP);
        return pluginDesc;
    }

    private AnsiblePluginDescription(String name, String title, String description) {
        this.name = name;
        this.title = title;
        this.description = description;

        properties = new ArrayList<Property>();
        mapping = new HashMap<String, String>();
        fwkmapping = new HashMap<String, String>();
        
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public List<Property> getProperties() {
        return properties;
    }

    @Override
    public Map<String, String> getPropertiesMapping() {
        return mapping;
    }

    @Override
    public Map<String, String> getFwkPropertiesMapping() {
        return fwkmapping;
    }

    @Override
    public String toString() {
        return "PropertyDescription{" +
                    "name = " + getName() + ", " +
                    "title = " + getTitle() + ", " +
                    "description = " + getDescription() + ", " +
                    "properties = " + getProperties() + ", " +
                    "mapping = " + getPropertiesMapping() +
                "}";
    }
	
}
