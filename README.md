**This is an alpha release!** Use with caution.

Please [report](https://github.com/Batix/rundeck-ansible-plugin/issues) any errors or suggestions!

## Rundeck Ansible Plugin ##

This plugin brings basic Ansible support to Rundeck. It imports hosts from Ansible's inventory, including a bunch of facts, and can run modules and playbooks. There is also a node executor and file copier for your project.

No SSH-Keys need to be shared between Ansible and Rundeck, everything is run through either `ansible` or `ansible-playbook` (even the node import).

The following bits are included:

### Resource Model Source ###

Uses the default configured inventory to scan for nodes. Facts are discovered by default, but you can turn that off (although I highly recommend leaving it on).

Host groups are imported as tags, you can limit the import to just some selected [patterns](http://docs.ansible.com/ansible/intro_patterns.html), if you want.

A bunch of facts are imported as attributes ([sample screenshot](http://batix.de/static/files/rundeck-ansible/node.png)).

### Node Executor ###

This makes it possible to run commands via the "Commands" menu or the default "Command" node step in a job.

The command is passed to Ansible's `shell` module. You can specify which shell to use in the project settings.

### File Copier ###

Enables usage of the default "Copy File" and (in combination with the above) "Script" node steps.

Files are transferred using Ansible's `copy` module.

### Run Ansible Modules ###

Run any Ansible module! You can specify the module name and arguments.

This is available as both a node and workflow step.

Note: The node step runs Ansible for every node, targeting only one node. The workflow step runs Ansible only once with a list of targets, so it should perform a bit better, if you don't need the individuality.

### Run Ansible Playbooks ###

Run a playbook as a node or workflow step (see note above). You specify a path to a file, which must be accessible to Rundeck.

## Requirements ##

- Ansible executable(s) in `PATH` of Rundeck user
- Rundeck user needs to be able to successfully run Ansible commands, that includes access to Ansible's config files and keys

## Installation ##

- [Download the .jar file from GitHub](https://github.com/Batix/rundeck-ansible-plugin/releases) or compile it yourself (using Gradle, either your own the included wrapper)
- Copy the .jar file to your Rundeck plugins directory (`/var/lib/rundeck/libext` if you installed the .deb, for example)
- Create a new project (this assumes you want every node in your project to be controlled via Ansible)
- Choose "Ansible Resource Model Source" as the resource model source
- Choose "Ansible Ad-Hoc Node Executor" as the default node executor
- Choose "Ansible File Copier" as the default node file copier
- Save, it can take a short time to import all the nodes, depending on your fleet
- You're all set! Try running a command

## Notes ##
I'm new to both Rundeck and Ansible so I expect there to be room for improvements. Only basic features have been implemented in this first pass, so I can play around with both tools. Liking it very much so far! :)

Tested on Debian.
