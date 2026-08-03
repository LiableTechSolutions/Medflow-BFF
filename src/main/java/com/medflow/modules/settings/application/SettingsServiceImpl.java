package com.medflow.modules.settings.application;

import com.medflow.modules.settings.api.SettingsService;
import com.medflow.modules.settings.api.request.UpdateSettingRequest;
import com.medflow.modules.settings.api.response.SettingResponse;
import com.medflow.modules.settings.domain.entity.WorkspaceSetting;
import com.medflow.modules.settings.domain.repository.WorkspaceSettingRepository;
import com.medflow.shared.exception.ResourceNotFoundException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class SettingsServiceImpl implements SettingsService {

  private final WorkspaceSettingRepository repository;

  SettingsServiceImpl(WorkspaceSettingRepository repository) {
    this.repository = repository;
  }

  @Override
  @Transactional(readOnly = true)
  public List<SettingResponse> getAll(Long hospitalId) {
    return repository.findByHospitalIdOrderByKeyAsc(hospitalId).stream()
        .map(this::toResponse)
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public SettingResponse get(Long hospitalId, String key) {
    return toResponse(repository.findByHospitalIdAndKey(hospitalId, key)
        .orElseThrow(() -> new ResourceNotFoundException("Setting not found: " + key)));
  }

  @Override
  @Transactional
  public SettingResponse put(Long hospitalId, String key, UpdateSettingRequest request) {
    var setting = repository.findByHospitalIdAndKey(hospitalId, key)
        .map(existing -> {
          existing.changeValue(request.value());
          return existing;
        })
        .orElseGet(() -> repository.save(new WorkspaceSetting(hospitalId, key, request.value())));
    return toResponse(setting);
  }

  private SettingResponse toResponse(WorkspaceSetting setting) {
    return new SettingResponse(setting.getKey(), setting.getValue(), setting.getUpdatedAt());
  }
}
