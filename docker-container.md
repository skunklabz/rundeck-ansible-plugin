## Docker Container ##

We provide a pre-built image including everything needed to take Rundeck and Ansible for a spin:

- Rundeck 3.0.17
- Ansible 2.7.9
- and this plugin in the respective version

The image can be found on the [Docker Hub](https://hub.docker.com/r/batix/rundeck-ansible/) and is based on the [official Rundeck docker image](https://hub.docker.com/r/rundeck/rundeck).

### Preparations ###

Create a new directory somewhere on your disk and create a new text file in there called `inventory.ini`. This file tells Ansible which hosts (also known as 'nodes') are available. It may also contain groups - read more about the inventory in the [Ansible documentation](https://docs.ansible.com/ansible/latest/user_guide/intro_inventory.html). Fill it with one or more hosts you have access to, for example:

```ini
[test-group]
local-thing      ansible_connection=local      ansible_python_interpreter=/usr/bin/python3
my-first-server  ansible_host=1.2.3.4          ansible_user=root    ansible_ssh_pass=SUPER_SECRET
my-second-server ansible_host=domain.works.too ansible_user=root    ansible_ssh_private_key_file=/home/rundeck/data/my-key.rsa
my-third-server  ansible_host=local-server     ansible_user=lowpriv ansible_ssh_pass=SUPER_SECRET ansible_become=true ansible_become_pass=SUPER_SECRET
```

Depending on your server configuration you can choose between username/password or key authentication. For key-based authentication, copy the private key to your directory and reference it as seen in the second example entry above.

If you don't have any servers readily available, just use the *local-thing* line. With it you can play around with the running docker container.

Using `sudo`, `su`, etc. is also possible, see [Become (Privilege Escalation)](https://docs.ansible.com/ansible/latest/user_guide/become.html) for details (in the example above, the third server will use `sudo` to change to root).

### Running ###

Ensure you have the latest version of the image locally available:

```bash
docker pull batix/rundeck-ansible
```

Then make sure to be in the directory you created above and start the container like this:

```bash
docker run -d --name rundeck-test -p 127.0.0.1:4440:4440 -v `pwd`:/home/rundeck/data batix/rundeck-ansible
```

If you are running Docker on Windows, replace `` `pwd` `` with `%CD%` for cmd or `${PWD}` for PowerShell (you might get a message, telling you that you need to share your drive first in the Docker for Windows settings, so do that beforehand; if the mounting fails - e.g. Rundeck / Ansible can't find the inventory - try un-sharing and re-sharing the drive).

After half a minute or so, you can access the Rundeck web interface at [localhost:4440](http://localhost:4440). You can login with `admin` as both the username and password. Your Rundeck instance won't be accessible externally, because it is bound to 127.0.0.1 / localhost.

Watch the logs with this command (quit with CTRL-C):

```bash
docker logs -f rundeck-test
```

When it says *Grails application running at...* you're good to go.

You can stop or completely remove your container with the following commands:

```bash
docker stop rundeck-test
docker rm -vf rundeck-test
```

#### Custom URL ####

If you want to share your test installation externally, you need to change the `-p` option when starting the container, so it doesn't bind to 127.0.0.1, additionally you need to specifiy the external URL of your install, i.e. what others need to type in the browser to reach your Rundeck install. The environment variable `RUNDECK_GRAILS_URL` can be used to customize the address of the installation:

```bash
docker run -d --name rundeck-test -p 4440:4440 -e RUNDECK_GRAILS_URL=http://my-host:4440 -v `pwd`:/home/rundeck/data batix/rundeck-ansible
```

**Warning:** Don't leave your test install running exposed to the public! Read up on the proper install procedure and configuration in the [Rundeck docs](https://docs.rundeck.com/docs/administration/security/index.html). At least change your password.

### Project ###

A project named "Test-Project" is already created and configured to use the plugin and your inventory file. Just click on it after you logged in.

### Nodes ###

- Click *Nodes* in the left pane and give Rundeck a moment to query your hosts via Ansible (you can watch the logs to see when it's finished)
- Select the *All Nodes* filter and you should be presented with your node(s)
- Expand a node to see the facts Ansible gathered about it (you can use these for filtering!)

### Commands ###

- Click *Commands* in the left pane
- Type `.*` in the *Nodes* textbox and press enter
- Type a simple command (for example `date`) in the first textbox and hit enter
- After some seconds you should see the output of your command, executed on every node

### Jobs ###

Jobs are repeatable lists of steps (actions). You can specify on which nodes a job is run and what is to be executed. Actions are for example simple commands or scripts (inline or a file). There are also some Ansible workflow steps (run a module or playbook). For more details check the [Rundeck docs on jobs](https://docs.rundeck.com/docs/tutorials/jobs.html). Try creating some jobs!

#### Example Job ####

Let's create a very simple job that just executes `date` on all hosts, but let's formulate it as a playbook!

Click on *Jobs* in the left pane. Create a new job and name it "ping all nodes".

Below under *Add a Step*, click on *Ansible Playbook Inline Workflow Node Step* and select *Ansible Playbook*. Put this into the *Playbook* textbox:

```yaml
---
- name: An example playbook
  hosts: all
  tasks:
  - name: ping the node
    ping:
```

Hit *Save* for this step.

For *Nodes* select *Dispatch to Nodes*, type `.*` into the *Node Filter* field and press enter.

Click *Create* at the bottom, you will be taken to your job.

Press *Run Job Now*. Check the *Log Output* tab when it's finished!

Have fun!
