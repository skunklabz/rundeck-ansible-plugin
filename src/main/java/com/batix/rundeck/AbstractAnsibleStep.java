package com.batix.rundeck;

import com.dtolabs.rundeck.core.execution.workflow.steps.StepException;
import com.dtolabs.rundeck.core.common.INodeEntry;
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

public abstract class AbstractAnsibleStep {

    protected AnsibleRunner runner;

    protected String playbook;
    protected String module;
    protected String args;
    protected List<String> limits = new ArrayList<>();
    protected String extraArgs;
    protected String vaultPass;
    protected Boolean debug = false;

    // ansible ssh args
    protected String sshUser;
    protected Boolean sshUsePassword;
    protected String sshPrivateKey;
    protected String sshPass;
    protected Integer sshTimeout;

    // ansible become args
    protected Boolean become;
    protected String becomeMethod;
    protected String becomeUser;
    protected String becomePassword;

    // depending on the implementation this could have defferent Implementations
    abstract protected AnsibleRunner getRunner();

    abstract protected AnsiblePluginType getPluginType();

    protected AnsibleRunner createRunner(PluginStepContext context, Map<String, Object> configuration) throws IOException,AnsibleStepException {
         this.parseRunnerArgs(context,configuration);
         runner = getRunner();
         runner.limit(context.getNodes().getNodeNames());
         this.setRunnerArgs();
         return runner;
    }

    protected AnsibleRunner createRunner(PluginStepContext context, Map<String, Object> configuration, INodeEntry entry) throws IOException,AnsibleStepException {
         parseRunnerArgs(context,configuration);
         runner = getRunner();
         runner.limit(entry.getNodename());
         this.setRunnerArgs();
         return runner;
    }

    protected void setRunnerArgs() {
         runner.extraArgs(extraArgs)
                  .debug(debug)
                  .vaultPass(vaultPass)
                  .sshUser(sshUser)
                  .sshUsePassword(sshUsePassword)
                  .sshPass(sshPass)
                  .sshPrivateKey(sshPrivateKey)
                  .sshTimeout(sshTimeout)
                  .become(become)
                  .becomeMethod(becomeMethod)
                  .becomeUser(becomeUser)
                  .becomePassword(becomePassword);
    }

    protected void parseRunnerArgs(PluginStepContext context, Map<String, Object> configuration) throws IOException {

      final PluginLogger logger = context.getLogger();
      
      Map<String, Map<String, String>> privateDataContext = context.getExecutionContext().getPrivateDataContext();
      Map<java.lang.String,java.lang.String> jobConfig = context.getDataContext().get("job");

      playbook = (String) configuration.get("playbook");

      module = (String) configuration.get("module");
      args = (String) configuration.get("args");

      extraArgs = (String) configuration.get("extraArgs");
      vaultPass = (String) configuration.get("vaultPass");

      sshUsePassword = Boolean.valueOf( (String) configuration.get("askpass") );
      sshUser = (String) configuration.get("sshUser");
      sshPrivateKey = (String) configuration.get("sshPrivateKey");
      sshPass = (String) configuration.get("sshpassword");
      
      if( configuration.containsKey("sshTimeout") ) {
        sshTimeout = Integer.valueOf( (String) configuration.get("sshTimeout") );
      }

      if( configuration.containsKey("become") ) {
        become = Boolean.valueOf( (String) configuration.get("become") );
      }

      becomeMethod = (String) configuration.get("becomeMethod");
      becomeUser = (String) configuration.get("becomeUser");
      becomePassword = (String) configuration.get("becomePassword");

      if (vaultPass != null && vaultPass.length() > 0) {
          Resource<ResourceMeta> resource  = context.getExecutionContext().getStorageTree().getResource(vaultPass);
          ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
          resource.getContents().writeContent(byteArrayOutputStream);
          vaultPass = new String(byteArrayOutputStream.toByteArray());
      } else {
          vaultPass = "";
      }

      if (jobConfig.get("loglevel").equals("DEBUG")) {
        debug = true;
      } else {
        debug = false;
      }

    }

}
