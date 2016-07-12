package com.batix.rundeck;

import java.util.HashMap;
import java.util.Map;

public class AnsibleCommon {
  static Map<String, Object> getRenderParametersForStoragePath() {
    Map<String, Object> renderParameter = new HashMap<>();
    renderParameter.put("selectionAccessor", "STORAGE_PATH");
    return renderParameter;
  }
}
