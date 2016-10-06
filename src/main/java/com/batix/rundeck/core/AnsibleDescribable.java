package com.batix.rundeck.core;

import com.dtolabs.rundeck.core.plugins.configuration.Describable;
import com.dtolabs.rundeck.core.plugins.configuration.Property;
import com.dtolabs.rundeck.core.plugins.configuration.PropertyUtil;
import com.dtolabs.rundeck.core.plugins.configuration.StringRenderingConstants;
import com.dtolabs.rundeck.plugins.util.PropertyBuilder;

import java.util.Arrays;

public interface AnsibleDescribable extends Describable {
	
    public static final String SERVICE_PROVIDER_TYPE = "ansible-service";

    public static final String ANSIBLE_PLAYBOOK = "ansible-playbook";
    public static final String ANSIBLE_INVENTORY = "ansible-inventory";
    public static final String ANSIBLE_MODULE = "ansible-module";
    public static final String ANSIBLE_MODULE_ARGS = "ansible-module-args";
    public static final String ANSIBLE_DEBUG = "ansible-debug";
    public static final String ANSIBLE_EXECUTABLE = "ansible-executable";
    public static final String ANSIBLE_GATHER_FACTS = "ansible-gather-facts";
    public static final String ANSIBLE_IGNORE_ERRORS = "ansible-ignore-errors";
    public static final String ANSIBLE_LIMIT = "ansible-limit";
    public static final String ANSIBLE_IGNORE_TAGS = "ansible-ignore-tags-prefix";
    public static final String ANSIBLE_EXTRA_VARS = "ansible-extra-vars";
    public static final String ANSIBLE_VAULT_PATH = "ansible-vault-path";
    public static final String ANSIBLE_VAULTSTORE_PATH = "ansible-vault-storage-path";

    // ssh configuration
    public static final String ANSIBLE_SSH_PASSWORD = "ansible-ssh-password";
    public static final String ANSIBLE_SSH_PASSWORD_OPTION = "ansible-ssh-password-option";
    public static final String ANSIBLE_SSH_PASSWORD_STORAGE_PATH = "ansible-ssh-password-storage-path";
    public static final String DEFAULT_ANSIBLE_SSH_PASSWORD_OPTION = "ansible-ssh-password";

    public static final String ANSIBLE_SSH_KEYPATH = "ansible-ssh-keypath";
    public static final String ANSIBLE_SSH_KEYPATH_STORAGE_PATH = "ansible-ssh-key-storage-path";

    public static final String ANSIBLE_SSH_TIMEOUT = "ansible-ssh-timeout";
    public static final String ANSIBLE_SSH_USER = "ansible-ssh-user";
    public static final String ANSIBLE_SSH_AUTH_TYPE = "ansible-ssh-auth-type";

    // become configuration
    public static final String ANSIBLE_BECOME = "ansible-become";
    public static final String ANSIBLE_BECOME_USER = "ansible-become-user";
    public static final String ANSIBLE_BECOME_METHOD = "ansible-become-method";
    public static final String ANSIBLE_BECOME_PASSWORD = "ansible-become-password";
    public static final String ANSIBLE_BECOME_PASSWORD_OPTION = "ansible-become-password-option";
    public static final String ANSIBLE_BECOME_PASSWORD_STORAGE_PATH = "ansible-become-password-storage-path";
    public static final String DEFAULT_ANSIBLE_BECOME_PASSWORD_OPTION = "ansible-become-password";

    public static final String PROJ_PROP_PREFIX = "project.";
    public static final String FWK_PROP_PREFIX = "framework.";

    public static Property PLAYBOOK_PROP = PropertyUtil.string(
              ANSIBLE_PLAYBOOK,
              "Playbook",
              "Path to a playbook",
              true,
              null
    );

    public static Property MODULE_PROP = PropertyUtil.string(
              ANSIBLE_MODULE,
              "Module",
              "Module name",
              true,
              null
    );

    public static Property MODULE_ARGS_PROP = PropertyUtil.string(
        ANSIBLE_MODULE_ARGS,
        "Arguments",
        "Arguments to pass to the module (-a/--args flag)",
        false,
        null
    );

    public static Property INVENTORY_PROP = PropertyUtil.string(ANSIBLE_INVENTORY, "ansible inventory File path",
            "File Path to the ansible inventory to use",
            false, null);

    public static Property EXECUTABLE_PROP = PropertyUtil.freeSelect(
              ANSIBLE_EXECUTABLE,
              "Executable",
              "Change the remote shell used to execute the command. Should be an absolute path to the executable.",
              true,
              "/bin/bash",
              Arrays.asList("/bin/sh", "/bin/bash")
    );

    public static Property GATHER_FACTS_PROP = PropertyUtil.bool(
              ANSIBLE_GATHER_FACTS,
              "Gather Facts",
              "Gather fresh facts before importing? (recommended)",
              true,
              "true"
    );

    public static Property IGNORE_ERRORS_PROP = PropertyUtil.bool(
              ANSIBLE_IGNORE_ERRORS,
              "Ignore Host Discovery Errors",
              "Still display Successful Hosts even if some other hosts are unreachable ?",
              true,
              "true"
    );

    public static Property LIMIT_PROP = PropertyUtil.string(
              ANSIBLE_LIMIT,
              "Limit Targets",
              "Select only specified hosts/groups from the Ansible inventory. See http://docs.ansible.com/ansible/intro_patterns.html for syntax help.",
              false,
              ""
    );

    public static Property IGNORE_TAGS_PREFIX_PROP = PropertyUtil.string(
              ANSIBLE_IGNORE_TAGS,
              "Ignore tags with this prefix",
              "Leave blank to import all groups as tags or specify a prefix string, groups starting with this string won't be added as tags.",
              false,
              ""
    );

    public static Property EXTRA_VARS_PROP = PropertyUtil.string(
          ANSIBLE_EXTRA_VARS,
          "Extra Arguments",
          "Extra Arguments for the Ansible process",
          false,
          null
    );

    static final Property VAULT_KEY_FILE_PROP = PropertyUtil.string(ANSIBLE_VAULT_PATH, "VAULT Key File path",
            "File Path to the ansible vault Key to use",
            false, null);

    static final Property VAULT_KEY_STORAGE_PROP = PropertyBuilder.builder()
            .string(ANSIBLE_VAULTSTORE_PATH)
            .required(false)
            .title("vaultPass")
            .description("Vault Password used to decrypt group variables")
            .renderingOption(StringRenderingConstants.SELECTION_ACCESSOR_KEY,
                    StringRenderingConstants.SelectionAccessor.STORAGE_PATH)
            .renderingOption(StringRenderingConstants.STORAGE_PATH_ROOT_KEY, "keys")
            .renderingOption(StringRenderingConstants.STORAGE_FILE_META_FILTER_KEY, "Rundeck-data-type=password")
            .build();


    static final Property SSH_KEY_FILE_PROP = PropertyBuilder.builder()
            .string(ANSIBLE_SSH_KEYPATH)
            .required(false)
            .title("SSH Key File path")
            .description("File Path to the SSH Key to use")
            .renderingOption(StringRenderingConstants.GROUPING,"SECONDARY")
            .renderingOption(StringRenderingConstants.GROUP_NAME,"SSH Connection")
            .build();

    
    static final Property SSH_KEY_STORAGE_PROP = PropertyBuilder.builder()
            .string(ANSIBLE_SSH_KEYPATH_STORAGE_PATH)
            .required(false)
            .title("SSH Key Storage Path")
            .description("Path to the SSH Key to use within Rundeck Storage. E.g. \"keys/path/key1.pem\"")
            .renderingOption(StringRenderingConstants.SELECTION_ACCESSOR_KEY,
                    StringRenderingConstants.SelectionAccessor.STORAGE_PATH)
            .renderingOption(StringRenderingConstants.STORAGE_PATH_ROOT_KEY, "keys")
            .renderingOption(StringRenderingConstants.STORAGE_FILE_META_FILTER_KEY, "Rundeck-key-type=private")
            .renderingOption(StringRenderingConstants.GROUPING,"SECONDARY")
            .renderingOption(StringRenderingConstants.GROUP_NAME,"SSH Connection")
            .build();

    static final Property SSH_PASSWORD_STORAGE_PROP = PropertyBuilder.builder()
            .string(ANSIBLE_SSH_PASSWORD_STORAGE_PATH)
            .required(false)
            .title("SSH Password Storage Path")
            .description("Path to the ssh Password to use within Rundeck Storage.")
            .renderingOption(StringRenderingConstants.SELECTION_ACCESSOR_KEY,
                    StringRenderingConstants.SelectionAccessor.STORAGE_PATH)
            .renderingOption(StringRenderingConstants.STORAGE_PATH_ROOT_KEY, "keys")
            .renderingOption(StringRenderingConstants.STORAGE_FILE_META_FILTER_KEY, "Rundeck-data-type=password")
            .renderingOption(StringRenderingConstants.GROUPING,"SECONDARY")
            .renderingOption(StringRenderingConstants.GROUP_NAME,"SSH Connection")
            .build();

    static final Property SSH_PASSWORD_PROP = PropertyBuilder.builder()
            .string(ANSIBLE_SSH_PASSWORD)
            .required(false)
            .title("SSH Password")
            .description("Ansible SSH password.")
            .renderingOption(StringRenderingConstants.DISPLAY_TYPE_KEY,
                    StringRenderingConstants.DisplayType.PASSWORD)
            .renderingOption(StringRenderingConstants.GROUPING,"SECONDARY")
            .renderingOption(StringRenderingConstants.GROUP_NAME,"SSH Connection")
            .build();

    static final Property SSH_AUTH_TYPE_PROP = PropertyBuilder.builder()
            .select(ANSIBLE_SSH_AUTH_TYPE)
            .required(false)
            .title("SSH Authentication")
            .description("Type of SSH Authentication to use.")
            .values(Arrays.asList(AnsibleRunnerBuilder.AuthenticationType.getValues()))
            .renderingOption(StringRenderingConstants.GROUPING,"SECONDARY")
            .renderingOption(StringRenderingConstants.GROUP_NAME,"SSH Connection")
            .build();

    static final Property SSH_USER_PROP = PropertyBuilder.builder()
            .string(ANSIBLE_SSH_USER)
            .required(false)
            .title("SSH User")
            .description("SSH User to authenticate as (default=rundeck).")
            .renderingOption(StringRenderingConstants.GROUPING,"SECONDARY")
            .renderingOption(StringRenderingConstants.GROUP_NAME,"SSH Connection")
            .build();

    static final Property SSH_TIMEOUT_PROP = PropertyBuilder.builder()
            .integer(ANSIBLE_SSH_TIMEOUT)
            .required(false)
            .title("SSH Timeout")
            .description("SSH timeout, override the SSH timeout in seconds (default=10).")
            .renderingOption(StringRenderingConstants.GROUPING,"SECONDARY")
            .renderingOption(StringRenderingConstants.GROUP_NAME,"SSH Connection")
            .build();


    static final Property BECOME_PROP = PropertyBuilder.builder()
            .booleanType(ANSIBLE_BECOME)
            .required(false)
            .title("Use become previlege escalation.")
            .description("Run operations with become (nopasswd implied).")
            .renderingOption(StringRenderingConstants.GROUPING,"SECONDARY")
            .renderingOption(StringRenderingConstants.GROUP_NAME,"Privilege Escalation")
            .build();

    static final Property BECOME_USER_PROP = PropertyBuilder.builder()
            .string(ANSIBLE_BECOME_USER)
            .required(false)
            .title("Privilege escalation user")
            .description("run operations as this user (default=root).")
            .renderingOption(StringRenderingConstants.GROUPING,"SECONDARY")
            .renderingOption(StringRenderingConstants.GROUP_NAME,"Privilege Escalation")
            .build();


    static final Property BECOME_PASSWORD_STORAGE_PROP = PropertyBuilder.builder()
            .string(ANSIBLE_BECOME_PASSWORD_STORAGE_PATH)
            .required(false)
            .title("Privilege escalation Password Storage Path.")
            .description("Become password used for previlege escalation.")
            .renderingOption(StringRenderingConstants.SELECTION_ACCESSOR_KEY,
                    StringRenderingConstants.SelectionAccessor.STORAGE_PATH)
            .renderingOption(StringRenderingConstants.STORAGE_PATH_ROOT_KEY, "keys")
            .renderingOption(StringRenderingConstants.STORAGE_FILE_META_FILTER_KEY, "Rundeck-data-type=password")
            .renderingOption(StringRenderingConstants.GROUPING,"SECONDARY")
            .renderingOption(StringRenderingConstants.GROUP_NAME,"Privilege Escalation")
            .build();

    static final Property BECOME_PASSWORD_PROP = PropertyBuilder.builder()
            .string(ANSIBLE_BECOME_PASSWORD)
            .required(false)
            .title("Privilege escalation Password")
            .description("Become password used for previlege escalation.")
            .renderingOption(StringRenderingConstants.DISPLAY_TYPE_KEY,
                    StringRenderingConstants.DisplayType.PASSWORD)
            .renderingOption(StringRenderingConstants.GROUPING,"SECONDARY")
            .renderingOption(StringRenderingConstants.GROUP_NAME,"Privilege Escalation")
            .build();

    static final Property BECOME_AUTH_TYPE_PROP = PropertyBuilder.builder()
            .select(ANSIBLE_BECOME_METHOD)
            .required(false)
            .title("Privilege escalation method.")
            .description("Privilege escalation method to use (default=sudo).")
            .values(Arrays.asList(AnsibleRunnerBuilder.BecomeMethodType.getValues()))
            .renderingOption(StringRenderingConstants.GROUPING,"SECONDARY")
            .renderingOption(StringRenderingConstants.GROUP_NAME,"Privilege Escalation")
            .build();

}
