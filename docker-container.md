## Docker Container ##

We provide a pre-built image including Rundeck, Ansible and this plugin. It just contains everything needed to take Rundeck and Ansible for a spin.

The image can be found on the [Docker Hub](https://hub.docker.com/r/batix/rundeck-ansible/) and is based on Alpine Linux, resulting in a size of about 250 MB (compressed 170 MB).

### Preparations ###

Create a new directory somewhere on your disk and create a new text file in there called `inventory.ini`. This file tells Ansible which hosts (also known as 'nodes') are available. It may also contain groups - read more about the inventory in the [Ansible documentation](http://docs.ansible.com/ansible/intro_inventory.html). Fill it with one or more hosts you have access to, for example:

```ini
[test-group]
my-first-server  ansible_host=1.2.3.4          ansible_user=root    ansible_ssh_pass=SUPER_SECRET
my-second-server ansible_host=domain.works.too ansible_user=root    ansible_ssh_private_key_file=/data/my-key.rsa
my-third-server  ansible_host=local-server     ansible_user=lowpriv ansible_ssh_pass=SUPER_SECRET ansible_become=true ansible_become_pass=SUPER_SECRET
```

Depending on your server configuration you can choose between username/password or key authentication. For key-based authentication, copy the private key to your directory and reference it as seen in the second example entry above.

Using `sudo`, `su`, etc. is also possible, see [Become (Privilege Escalation)](http://docs.ansible.com/ansible/become.html) for details (in the example above, the third server will use `sudo` to change to root).

### Running ###

Ensure you have the latest version of the image locally available:

```bash
docker pull batix/rundeck-ansible
```

Then make sure to be in the directory you created above and start the container like this:

```bash
docker run -d --name rundeck-test -p 127.0.0.1:4440:4440 -e RDECK_ADMIN_PASS=SUPER_SECRET -v `pwd`:/data batix/rundeck-ansible
```

If you are running Docker on Windows, replace `` `pwd` `` with `%CD%` for cmd or `${PWD}` for PowerShell (you might get a message, telling you that you need to share your drive first in the Docker for Windows settings, so do that beforehand; if the mounting fails - e.g. Rundeck / Ansible can't find the inventory - try un-sharing and re-sharing the drive).

Specify a password for the admin user via the `RDECK_ADMIN_PASS` environment variable.

After half a minute or so, you can access the Rundeck web interface at [localhost:4440](http://localhost:4440). You can login with `admin` as the username and your password. Your Rundeck instance won't be accessible externally, because it is bound to 127.0.0.1 / localhost.

Watch the logs via (quit with CTRL-C):

```bash
docker logs -f rundeck-test
```

You can stop or completely remove your container with the following commands:

```bash
docker stop rundeck-test
docker rm -vf rundeck-test
```

#### Custom Host / Port ####

The environment variables `RDECK_HOST` and `RDECK_PORT` can be used to customize the address of the installation (note that you need to change the `-p` argument as well):

```bash
docker run -d --name rundeck-test -p 34440:34440 -e RDECK_ADMIN_PASS=SUPER_SECRET -e RDECK_HOST=my.hosted.server -e RDECK_PORT=34440 -v `pwd`:/data batix/rundeck-ansible
```

**Warning:** Don't leave your test install running exposed to the public! Read up on the proper install procedure and configuration in the [Rundeck docs](http://rundeck.org/docs/administration/installation.html). At least set a reasonable difficult password via `RDECK_ADMIN_PASS`.

### Project ###

A project named "Test-Project" is already created and configured to use the plugin and your inventory file. Just click on it after you logged in.

### Nodes ###

- Click *Nodes* in the header and give Rundeck a moment to query your hosts via Ansible (you can watch the logs to see when it's finished)
- Select the *All Nodes* filter and you should be presented with your node(s)
- Expand a node to see the facts Ansible gathered about it (you can use these for filtering!)

### Commands ###

- Click *Commands* in the header
- Type `.*` in the *Nodes* textbox and press enter
- Type a simple command (for example `date`) in the first textbox and hit enter
- After some seconds you should see the output of your command, executed on every node

### Jobs ###

Jobs are repeatable lists of steps (actions). You can specify on which nodes a job is run and what is to be executed. Actions are for example simple commands or scripts (inline or a file). There are also some Ansible workflow steps (run a module or playbook). For more details check the [Rundeck docs on jobs](http://rundeck.org/docs/manual/jobs.html). Try creating some jobs!

#### Example Job ####

Let's create a very simple job that just executes `date` on all hosts, but let's formulate it as a playbook!

First, create a text file called `example-playbook.yaml` in the directory. Fill it with this content:

```yaml
---
- name: An example playbook
  hosts: all
  tasks:
  - name: print the current date and time
    command: date
```

Now (re-)start the container and click on *Jobs* in the header. Create a new job and name it "ping all".

Below under *Add a Step*, click on *Workflow Steps* and select *Ansible Playbook*. Put `/data/example-playbook.yaml` into the first textbox (Path to a playbook) and hit *Save* for this step.

For *Nodes* select *Dispatch to Nodes*, type `.*` into the *Node Filter* and press enter.

Click *Create* at the bottom, you will be taken to your job. Press *Run Job Now*. Check the *Log Output* tab when it's finished!

### Plugins ###

If you want to try out other plugins in your test install (see [here](http://rundeck.org/plugins/index.html) and [here](https://github.com/rundeck-plugins/) for lists of plugins), simply put their .jar files in a directory named `libext` (inside the directory you created under [Preparations](#Preparations).

#### Example: Slack Notifications ####

Here is a quick guide to add Slack notifications to your jobs:
- Go to [https://github.com/rundeck-plugins/slack-incoming-webhook-plugin]()
- Download the latest version from the "releases" tab there (grab the .jar, e.g. slack-incoming-webhook-plugin-1.0.jar)
- Create a directory named "libext" in your directory (where you start the container)
- Put the .jar file there
- (Re)Start the container (see [Running](#Running))
- Add or edit a job and configure Slack under "Send Notification?" (select Yes there)

Have fun!
