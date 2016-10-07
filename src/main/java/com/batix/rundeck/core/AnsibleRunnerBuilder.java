/**
 * Ansible Runner Builder.
 *
 * @author Yassine Azzouz <a href="mailto:yassine.azzouz@gmail.com">yassine.azzouz@gmail.com</a>
 */
package com.batix.rundeck.core;

import com.dtolabs.rundeck.core.common.INodeEntry;
import com.dtolabs.rundeck.core.common.Framework;
import com.dtolabs.rundeck.core.dispatcher.DataContextUtils;
import com.dtolabs.rundeck.core.execution.ExecutionContext;
import com.dtolabs.rundeck.core.plugins.configuration.ConfigurationException;
import com.dtolabs.rundeck.core.storage.ResourceMeta;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.rundeck.storage.api.Path;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.rundeck.storage.api.PathUtil;
import org.rundeck.storage.api.StorageException;

public class AnsibleRunnerBuilder {


    public static enum AuthenticationType {
        privateKey,
        password;
    	
    	public static String[] getValues() {
    	    java.util.LinkedList<String> list = new LinkedList<String>();
    	    for (AuthenticationType s : AuthenticationType.values()) {
    	        list.add(s.name());
    	    }
    	    return list.toArray(new String[list.size()]);
    	}
    }

    public static enum BecomeMethodType {
        sudo,
        su;
    	
    	public static String[] getValues() {
    	    java.util.LinkedList<String> list = new LinkedList<String>();
    	    for (BecomeMethodType s : BecomeMethodType.values()) {
    	        list.add(s.name());
    	    }
    	    return list.toArray(new String[list.size()]);
    	}
    }

    private ExecutionContext context;
    private Framework framework;
    private String frameworkProject;
    private Map<String, Object> jobConf;
    private INodeEntry node;

    AnsibleRunnerBuilder(final ExecutionContext context, final Framework framework) {
        this.context = context;
        this.framework = framework;
        this.frameworkProject = context.getFrameworkProject();
        this.jobConf = new HashMap<String, Object>();
        this.node = null;
    }

    public AnsibleRunnerBuilder(final ExecutionContext context, final Framework framework, final Map<String, Object> configuration) {
        this.context = context;
        this.framework = framework;
        this.frameworkProject = context.getFrameworkProject();
        this.jobConf = configuration;
        this.node = null;
    }

    public AnsibleRunnerBuilder(final INodeEntry node,final ExecutionContext context, final Framework framework, final Map<String, Object> configuration) {
        this.context = context;
        this.framework = framework;
        this.frameworkProject = context.getFrameworkProject();
        this.jobConf = configuration;
        this.node = node;
    }

    private String evaluateSecureOption(final String optionName, final ExecutionContext context ) {
        if (null == optionName) {
            return null;
        }
        if (null == context.getPrivateDataContext()) {
            return null;
        }
        final String[] opts = optionName.split("\\.", 2);
        String dataset = null;
        String optname = null;
        if (null != opts && 2 == opts.length) {
            dataset = opts[0];
            optname = opts[1];
        } else if (null != opts && 1 == opts.length) {
            dataset = "option";
            optname = opts[0];
        }
        final Map<String, String> option = context.getPrivateDataContext().get(dataset);
        if (null != option) {
            return option.get(optname);
        }
        return null;
    }

    public static String resolveProperty(
            final String attribute,
            final String defaultValue,
            final String frameworkProject,
            final Framework framework,
            final INodeEntry node,
            final Map<String, Object> jobConf
    )
    {
        // First check if the job Configuration contains the attribute
        // Then check if the project has that attribute
        // Finally check the framework
        if ( jobConf != null && jobConf.containsKey(attribute) ) {
               return (String) jobConf.get(attribute);
        } else if ( node != null && null != node.getAttributes().get(attribute)) {
            return node.getAttributes().get(attribute);
        } else if ( framework.hasProjectProperty(AnsibleDescribable.PROJ_PROP_PREFIX + attribute, frameworkProject)
             && !"".equals(framework.getProjectProperty(frameworkProject, AnsibleDescribable.PROJ_PROP_PREFIX + attribute))
           ) {
               return framework.getProjectProperty(frameworkProject, AnsibleDescribable.PROJ_PROP_PREFIX + attribute);
        } else if (framework.hasProperty(AnsibleDescribable.FWK_PROP_PREFIX + attribute)) {
            return framework.getProperty(AnsibleDescribable.FWK_PROP_PREFIX + attribute);
        } else {
            return defaultValue;
        }
    }

    private byte[] loadStoragePathData(final String passwordStoragePath) throws IOException {
        if (null == passwordStoragePath) {
            return null;
        }
        ResourceMeta contents = context.getStorageTree().getResource(passwordStoragePath).getContents();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        contents.writeContent(byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    public String getPrivateKeyfilePath() {
        String path = resolveProperty(
                AnsibleDescribable.ANSIBLE_SSH_KEYPATH,
                null,
                getFrameworkProject(),
                getFramework(),
                getNode(),
                getjobConf()
                );

        //expand properties in path
        if (path != null && path.contains("${")) {
            path = DataContextUtils.replaceDataReferences(path, context.getDataContext());
        }
        return path;
    }

    public String getPrivateKeyStoragePath() {
        String path = resolveProperty(
        		AnsibleDescribable.ANSIBLE_SSH_KEYPATH_STORAGE_PATH,
                null,
                getFrameworkProject(),
                getFramework(),
                getNode(),
                getjobConf()
                );
        //expand properties in path
        if (path != null && path.contains("${")) {
            path = DataContextUtils.replaceDataReferences(path, context.getDataContext());
        }
        return path;
    }

    public InputStream getPrivateKeyStorageData() throws IOException {
        String privateKeyResourcePath = getPrivateKeyStoragePath();
        if (null == privateKeyResourcePath) {
            return null;
        }
        return context
                .getStorageTree()
                .getResource(privateKeyResourcePath)
                .getContents()
                .getInputStream();
    }


    public String getPasswordStoragePath() {

        String path = resolveProperty(
        		AnsibleDescribable.ANSIBLE_SSH_PASSWORD_STORAGE_PATH,
                null,
                getFrameworkProject(),
                getFramework(),
                getNode(),
                getjobConf()
                );

        //expand properties in path
        if (path != null && path.contains("${")) {
            path = DataContextUtils.replaceDataReferences(path, context.getDataContext());
        }
         return path;
    }

    public String getSshPrivateKey()  throws ConfigurationException{
        //look for storage option
        String storagePath = resolveProperty(
        	AnsibleDescribable.ANSIBLE_SSH_KEYPATH_STORAGE_PATH,
                null,
                getFrameworkProject(),
                getFramework(),
                getNode(),
                getjobConf()
                );

        if(null!=storagePath){
            //look up storage value
            if (storagePath.contains("${")) {
                storagePath = DataContextUtils.replaceDataReferences(
                        storagePath,
                        context.getDataContext()
                );
            }
            Path path = PathUtil.asPath(storagePath);
            try {
                ResourceMeta contents = context.getStorageTree().getResource(path)
                        .getContents();
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                contents.writeContent(byteArrayOutputStream);
                return new String(byteArrayOutputStream.toByteArray());
            } catch (StorageException e) {
                throw new ConfigurationException("Failed to read the ssh private key for " +
                        "storage path: " + storagePath + ": " + e.getMessage());
            } catch (IOException e) {
                throw new ConfigurationException("Failed to read the ssh private key for " +
                        "storage path: " + storagePath + ": " + e.getMessage());
            }
        } else {
            //else look up option value
            final String path = getPrivateKeyfilePath();
            if (path != null) {
                try {
                    return new String(Files.readAllBytes(Paths.get(path)));
                } catch (IOException e) {
                    throw new ConfigurationException("Failed to read the ssh private key from path " +
                                                  path + ": " + e.getMessage());
                }
            } else {
                return null;
            }
        }
    }

    public String getSshPassword()  throws ConfigurationException{
        
        //look for option values first
        //typically jobs use secure options to dynamically setup the ssh password
        final String passwordOption = resolveProperty(
                    AnsibleDescribable.ANSIBLE_SSH_PASSWORD_OPTION,
                    AnsibleDescribable.DEFAULT_ANSIBLE_SSH_PASSWORD_OPTION,
                    getFrameworkProject(),
                    getFramework(),
                    getNode(),
                    getjobConf()
                    );
        String sshPassword = evaluateSecureOption(passwordOption, getContext());
        
        if(null!=sshPassword){
            // is true if there is an ssh option defined in the private data context
            return sshPassword;
        } else {
            //look for storage option
            String storagePath = resolveProperty(
                AnsibleDescribable.ANSIBLE_SSH_PASSWORD_STORAGE_PATH,
                null,
                getFrameworkProject(),
                getFramework(),
                getNode(),
                getjobConf()
                );

            if(null!=storagePath){
                //look up storage value
                if (storagePath.contains("${")) {
                    storagePath = DataContextUtils.replaceDataReferences(
                            storagePath,
                            context.getDataContext()
                    );
                }
                Path path = PathUtil.asPath(storagePath);
                try {
                    ResourceMeta contents = context.getStorageTree().getResource(path)
                            .getContents();
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    contents.writeContent(byteArrayOutputStream);
                    return new String(byteArrayOutputStream.toByteArray());
                } catch (StorageException e) {
                    throw new ConfigurationException("Failed to read the shh password for " +
                            "storage path: " + storagePath + ": " + e.getMessage());
                } catch (IOException e) {
                    throw new ConfigurationException("Failed to read the ssh password for " +
                            "storage path: " + storagePath + ": " + e.getMessage());
                }

            } else {
                return null;
            }
        }
    }

    public Integer getSSHTimeout() throws ConfigurationException {
    	Integer timeout = null;
        final String stimeout = resolveProperty(
        		    AnsibleDescribable.ANSIBLE_SSH_TIMEOUT,
                    null,
                    getFrameworkProject(), 
                    getFramework(),
                    getNode(),
                    getjobConf()
                    );
        if (null != stimeout) {
            try {
            	timeout = Integer.parseInt(stimeout);
            } catch (NumberFormatException e) {
                throw new ConfigurationException("Can't parse timeout value" +
                		timeout + ": " + e.getMessage());
            }
        }
        return timeout;
    }

    public String getSshUser() {
        final String user;
        user = resolveProperty(
                  AnsibleDescribable.ANSIBLE_SSH_USER,
                  null,
                  getFrameworkProject(),
                  getFramework(),
                  getNode(),
                  getjobConf()
                  );

        if (null != user && user.contains("${")) {
            return DataContextUtils.replaceDataReferences(user, getContext().getDataContext());
        }
        return user;
    }


    public AuthenticationType getSshAuthenticationType() {
        String authType = resolveProperty(
                  AnsibleDescribable.ANSIBLE_SSH_AUTH_TYPE,
                  null,
                  getFrameworkProject(),
                  getFramework(),
                  getNode(),
                  getjobConf()
                  );

        if (null != authType) {
             return AuthenticationType.valueOf(authType);
        }
        return AuthenticationType.privateKey;
    }

    public String getBecomeUser() {
        final String user;
        user = resolveProperty(
                   AnsibleDescribable.ANSIBLE_BECOME_USER,
                   null,
                   getFrameworkProject(),
                   getFramework(),getNode(),
                   getjobConf()
                   );
        
        if (null != user && user.contains("${")) {
            return DataContextUtils.replaceDataReferences(user, getContext().getDataContext());
        }
        return user;
    }

    public Boolean getBecome() {
        Boolean become = null;
        String sbecome = resolveProperty(
                   AnsibleDescribable.ANSIBLE_BECOME,
                   null,
                   getFrameworkProject(),
                   getFramework(),
                   getNode(),
                   getjobConf()
                   );

        if (null != sbecome) {
        	become = Boolean.parseBoolean(sbecome);
        }
        return become;
    }

    public BecomeMethodType getBecomeMethod() {
        String becomeMethod = resolveProperty(
                   AnsibleDescribable.ANSIBLE_BECOME_METHOD,
                   null,
                   getFrameworkProject(),
                   getFramework(),
                   getNode(),
                   getjobConf()
                   );

        if (null != becomeMethod) {
             return BecomeMethodType.valueOf(becomeMethod);
        }
        return null;
    }


    public byte[] getPasswordStorageData() throws IOException{
        return loadStoragePathData(getPasswordStoragePath());
    }


    public String getBecomePasswordStoragePath() { 
        String path = resolveProperty(
        		AnsibleDescribable.ANSIBLE_BECOME_PASSWORD_STORAGE_PATH,
                null,
                getFrameworkProject(),
                getFramework(),
                getNode(),
                getjobConf()
                );
        //expand properties in path
        if (path != null && path.contains("${")) {
            path = DataContextUtils.replaceDataReferences(path, context.getDataContext());
        }
        return path;
    }

    public byte[] getBecomePasswordStorageData() throws IOException{
        return loadStoragePathData(getBecomePasswordStoragePath());
    }


    public String getBecomePassword(String prefix) {
        final String passwordOption = resolveProperty(
                    AnsibleDescribable.ANSIBLE_BECOME_PASSWORD_OPTION,
                    AnsibleDescribable.DEFAULT_ANSIBLE_BECOME_PASSWORD_OPTION,
                    getFrameworkProject(), 
                    getFramework(),
                    getNode(),
                    getjobConf()
                    );

        return evaluateSecureOption(passwordOption, getContext());
    }

    public String getBecomePassword()  throws ConfigurationException{
        
        //look for option values first
        //typically jobs use secure options to dynamically setup the become password
        String passwordOption = resolveProperty(
                    AnsibleDescribable.ANSIBLE_BECOME_PASSWORD_OPTION,
                    AnsibleDescribable.DEFAULT_ANSIBLE_BECOME_PASSWORD_OPTION,
                    getFrameworkProject(),
                    getFramework(),
                    getNode(),
                    getjobConf()
                    );
        String becomePassword = evaluateSecureOption(passwordOption, getContext());
        
        if(null!=becomePassword){
            // is true if there is a become option defined in the private data context
            return becomePassword;
        } else {
            //look for storage option
            String storagePath = resolveProperty(
                AnsibleDescribable.ANSIBLE_BECOME_PASSWORD_STORAGE_PATH,
                null,
                getFrameworkProject(),
                getFramework(),
                getNode(),
                getjobConf()
                );

            if(null!=storagePath){
                //look up storage value
                if (storagePath.contains("${")) {
                    storagePath = DataContextUtils.replaceDataReferences(
                            storagePath,
                            context.getDataContext()
                    );
                }
                Path path = PathUtil.asPath(storagePath);
                try {
                    ResourceMeta contents = context.getStorageTree().getResource(path)
                            .getContents();
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    contents.writeContent(byteArrayOutputStream);
                    return new String(byteArrayOutputStream.toByteArray());
                } catch (StorageException e) {
                    throw new ConfigurationException("Failed to read the become password for " +
                            "storage path: " + storagePath + ": " + e.getMessage());
                } catch (IOException e) {
                    throw new ConfigurationException("Failed to read the become password for " +
                            "storage path: " + storagePath + ": " + e.getMessage());
                }

            } else {
                return null;
            }
        }
    }

    public String getVaultKey()  throws ConfigurationException{
        //look for storage option
        String storagePath = resolveProperty(
        		AnsibleDescribable.ANSIBLE_VAULTSTORE_PATH,
                null,
                getFrameworkProject(),
                getFramework(),
                getNode(),
                getjobConf()
                );

        if(null!=storagePath){
            //look up storage value
            if (storagePath.contains("${")) {
                storagePath = DataContextUtils.replaceDataReferences(
                        storagePath,
                        context.getDataContext()
                );
            }
            Path path = PathUtil.asPath(storagePath);
            try {
                ResourceMeta contents = context.getStorageTree().getResource(path)
                        .getContents();
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                contents.writeContent(byteArrayOutputStream);
                return new String(byteArrayOutputStream.toByteArray());
            } catch (StorageException e) {
                throw new ConfigurationException("Failed to read the vault key for " +
                        "storage path: " + storagePath + ": " + e.getMessage());
            } catch (IOException e) {
                throw new ConfigurationException("Failed to read the vault key for " +
                        "storage path: " + storagePath + ": " + e.getMessage());
            }
        } else {

            String path = resolveProperty(
            	AnsibleDescribable.ANSIBLE_VAULT_PATH,
                null,
                getFrameworkProject(),
                getFramework(),
                getNode(),
                getjobConf()
                );

            //expand properties in path
            if (path != null && path.contains("${")) {
                path = DataContextUtils.replaceDataReferences(path, context.getDataContext());
            }

            if (path != null) {
              try {
				return new String(Files.readAllBytes(Paths.get(path)));
			} catch (IOException e) {
                throw new ConfigurationException("Failed to read the ssh private key from path " +
                		path + ": " + e.getMessage());
			}
            } else {
              return null;
            }
        }
    }

    public String getPlaybook() {
        String playbook = null;
        if ( getjobConf().containsKey(AnsibleDescribable.ANSIBLE_PLAYBOOK) ) {
        	playbook = (String) jobConf.get(AnsibleDescribable.ANSIBLE_PLAYBOOK);
        }

        if (null != playbook && playbook.contains("${")) {
            return DataContextUtils.replaceDataReferences(playbook, getContext().getDataContext());
        }
        return playbook;
    }

    public String getModule() {
        String module = null;
        if ( getjobConf().containsKey(AnsibleDescribable.ANSIBLE_MODULE) ) {
        	module = (String) jobConf.get(AnsibleDescribable.ANSIBLE_MODULE);
        }
        
        if (null != module && module.contains("${")) {
            return DataContextUtils.replaceDataReferences(module, getContext().getDataContext());
        }
        return module;
    }

    public String getModuleArgs() {
        String args = null;
        if ( getjobConf().containsKey(AnsibleDescribable.ANSIBLE_MODULE_ARGS) ) {
        	args = (String) jobConf.get(AnsibleDescribable.ANSIBLE_MODULE_ARGS);
        }

        if (null != args && args.contains("${")) {
            return DataContextUtils.replaceDataReferences(args, getContext().getDataContext());
        }
        return args;
    }

    public String getExecutable() {
        final String executable;
        executable = resolveProperty(
                  AnsibleDescribable.ANSIBLE_EXECUTABLE,
                  null,
                  getFrameworkProject(),
                  getFramework(),
                  getNode(),
                  getjobConf()
                  );

        if (null != executable && executable.contains("${")) {
            return DataContextUtils.replaceDataReferences(executable, getContext().getDataContext());
        }
        return executable;
    }

    public Boolean getDebug() {
        Boolean debug = Boolean.FALSE;
        String sdebug = resolveProperty(
                  AnsibleDescribable.ANSIBLE_DEBUG,
                  null,
                  getFrameworkProject(),
                  getFramework(),
                  getNode(),
                  getjobConf()
                  );

        if (null != sdebug) {
            debug = Boolean.parseBoolean(sdebug);
        }
        return debug;
    }

    public Boolean gatherFacts() {
        Boolean gatherFacts = null;
        String sgatherFacts = resolveProperty(
                  AnsibleDescribable.ANSIBLE_GATHER_FACTS,
                  null,
                  getFrameworkProject(),
                  getFramework(),
                  getNode(),
                  getjobConf()
                  );

        if (null != sgatherFacts) {
        	gatherFacts = Boolean.parseBoolean(sgatherFacts);
        }
        return gatherFacts;
    }

    public Boolean ignoreErrors() {
        Boolean ignoreErrors = null;
        String signoreErrors = resolveProperty(
                   AnsibleDescribable.ANSIBLE_IGNORE_ERRORS,
                   null,
                   getFrameworkProject(),
                   getFramework(),
                   getNode(),
                   getjobConf()
                   );

        if (null != signoreErrors) {
        	ignoreErrors = Boolean.parseBoolean(signoreErrors);
        }
        return ignoreErrors;
    }

    public String getIgnoreTagsPrefix() {
        final String ignoreTagsPrefix;
        ignoreTagsPrefix = resolveProperty(
                   AnsibleDescribable.ANSIBLE_IGNORE_TAGS,
                   null,
                   getFrameworkProject(),
                   getFramework(),
                   getNode(),
                   getjobConf()
                   );

        if (null != ignoreTagsPrefix && ignoreTagsPrefix.contains("${")) {
            return DataContextUtils.replaceDataReferences(ignoreTagsPrefix, getContext().getDataContext());
        }
        return ignoreTagsPrefix;
    }

    public String getExtraVars() {
        final String extraVars;
        extraVars = resolveProperty(
                    AnsibleDescribable.ANSIBLE_EXTRA_VARS,
                    null,
                    getFrameworkProject(),
                    getFramework(),
                    getNode(),
                    getjobConf()
                    );

        if (null != extraVars && extraVars.contains("${")) {
            return DataContextUtils.replaceDataReferences(extraVars, getContext().getDataContext());
        }
        return extraVars;
    }

    public String getInventory() {
        final String inventory;
        inventory = resolveProperty(
                     AnsibleDescribable.ANSIBLE_INVENTORY,
                     null,
                     getFrameworkProject(),
                     getFramework(),
                     getNode(),
                     getjobConf()
                     );

        if (null != inventory && inventory.contains("${")) {
            return DataContextUtils.replaceDataReferences(inventory, getContext().getDataContext());
        }
        return inventory;
    }

    public AnsibleRunner buildAnsibleRunner() throws ConfigurationException{

        AnsibleRunner runner = null;

        String playbook = getPlaybook();
        String module = getModule();
        if (playbook != null) {
            runner = AnsibleRunner.playbook(playbook);
        } else if (module != null) {
            runner = AnsibleRunner.adHoc(module, getModuleArgs());
        } else {
            throw new ConfigurationException("Missing module or playbook job arguments");          
        }

        final AuthenticationType authType = getSshAuthenticationType();
        if (AuthenticationType.privateKey == authType) {
             final String privateKey = getSshPrivateKey();
             if (privateKey != null) {
                runner = runner.sshPrivateKey(privateKey);
             }
        } else if (AuthenticationType.password == authType) {
            final String password = getSshPassword();
            if (password != null) {
                runner = runner.sshUsePassword(Boolean.TRUE).sshPass(password);
            }
        }

        String inventory = getInventory();
        if (inventory != null) {
            runner = runner.setInventory(inventory);
        }

        Boolean debug = getDebug();
        if (debug != null) {
            if (debug == Boolean.TRUE) {
               runner = runner.debug(Boolean.TRUE);
            } else {
               runner = runner.debug(Boolean.FALSE);
            }
        }

        String extraVars = getExtraVars();
        if (extraVars != null) {
            runner = runner.extraArgs(extraVars);
        }
        
        String user = getSshUser();
        if (user != null) {
            runner = runner.sshUser(user);
        }

        String vault = getVaultKey();
        if (vault != null) {
            runner = runner.vaultPass(vault);
        }

        Integer timeout = getSSHTimeout();
        if (timeout != null) {
            runner = runner.sshTimeout(timeout);
        }

        Boolean become = getBecome();
        if (become != null) {
            runner = runner.become(become);
        }

        String become_user = getBecomeUser();
        if (become_user != null) {
            runner = runner.becomeUser(become_user);
        }

        BecomeMethodType become_method = getBecomeMethod();
        if (become_method != null) {
            runner = runner.becomeMethod(become_method.name());
        }

        String become_password = getBecomePassword();
        if (become_password != null) {
            runner = runner.becomePassword(become_password);
        }

        return runner;
    }

    public ExecutionContext getContext() {
        return context;
    }

    public Framework getFramework() {
        return framework;
    }

    public INodeEntry getNode() {
        return node;
    }

    public String getFrameworkProject() {
        return frameworkProject;
    }

    public Map<String,Object> getjobConf() {
        return jobConf;
    }
}
