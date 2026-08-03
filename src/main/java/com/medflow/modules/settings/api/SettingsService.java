package com.medflow.modules.settings.api;

import com.medflow.modules.settings.api.request.UpdateSettingRequest;
import com.medflow.modules.settings.api.response.SettingResponse;
import java.util.List;

/** Public API of the Settings module. */
public interface SettingsService {

  List<SettingResponse> getAll(Long hospitalId);

  SettingResponse get(Long hospitalId, String key);

  /** Creates the key when absent, otherwise overwrites its value. */
  SettingResponse put(Long hospitalId, String key, UpdateSettingRequest request);
}
