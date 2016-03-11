package com.batix.rundeck;

import com.dtolabs.rundeck.core.plugins.configuration.PropertyValidator;
import com.dtolabs.rundeck.core.plugins.configuration.ValidationException;

import java.io.File;

public class AnsiblePlaybookPropertyValidator implements PropertyValidator {
  @Override
  public boolean isValid(String value) throws ValidationException {
    File file = new File(value);
    if (!file.exists()) {
      throw new ValidationException("File not found.");
    }
    return true;
  }
}
