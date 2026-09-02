package com.medflow.modules.settings.api;

import com.medflow.modules.settings.api.request.UpdateRegistrationProfileRequest;
import com.medflow.modules.settings.api.response.RegistrationProfileResponse;
import java.util.Map;

public interface RegistrationProfileService {
  RegistrationProfileResponse get(Long hospitalId);
  RegistrationProfileResponse update(Long hospitalId, Long actorId, UpdateRegistrationProfileRequest request);
  Map<String, RegistrationFieldState> statesFor(Long hospitalId);
}
