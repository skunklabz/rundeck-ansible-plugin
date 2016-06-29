package com.batix.rundeck;

import com.dtolabs.rundeck.core.execution.workflow.steps.FailureReason;

public enum AnsibleFailureReason implements FailureReason {
  AnsibleNonZero, // Ansible process exited with non-zero value
  AnsibleError, // Ansible not found etc.
  StorageTierAccessError
}
