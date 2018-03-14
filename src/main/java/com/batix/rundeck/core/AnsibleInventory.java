package com.batix.rundeck.core;

import java.util.HashMap;
import java.util.Map;

public class AnsibleInventory {
  
  public class AnsibleInventoryHosts {
    
    protected Map<String, Map<String, String>> hosts = new HashMap<String, Map<String, String>>();
    protected Map<String, AnsibleInventoryHosts> children = new HashMap<String, AnsibleInventoryHosts>();
  
    public AnsibleInventoryHosts addHost(String nodeName) {
      hosts.put(nodeName, new HashMap<String, String>());
      return this;
    }

    public AnsibleInventoryHosts addHost(String nodeName, String host, Map<String, String> attributes) {
      attributes.put("ansible_host", host);
      hosts.put(nodeName, attributes);
      return this;
    }

    public AnsibleInventoryHosts getOrAddChildHostGroup(String groupName) {
      children.putIfAbsent(groupName, new AnsibleInventoryHosts());
      return children.get(groupName);
    }
  }

  protected AnsibleInventoryHosts all = new AnsibleInventoryHosts();

  public AnsibleInventory addHost(String nodeName, String host, Map<String, String> attributes) {
    // Remove attributes that are reserved in Ansible
    String[] reserved = { "hostvars", "group_names", "groups", "environment" };
    for (String r: reserved){
      attributes.remove(r);
    }
    all.addHost(nodeName, host, attributes);
    // Create Ansible groups by attribute
    // Group by osFamily is needed for windows hosts setup
    String[] attributeGroups = { "osFamily" };
    for (String g: attributeGroups) {
      if (attributes.containsKey(g)) {
        String groupName = attributes.get(g).toLowerCase();
        all.getOrAddChildHostGroup(groupName).addHost(nodeName);
      }
    }
    return this;
  }
}