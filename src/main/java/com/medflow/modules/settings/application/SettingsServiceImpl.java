package com.medflow.modules.settings.application;

import com.medflow.modules.settings.api.SettingsService;
import com.medflow.modules.settings.api.request.UpdateSettingRequest;
import com.medflow.modules.settings.api.response.SettingResponse;
import com.medflow.modules.settings.domain.entity.WorkspaceSetting;
import com.medflow.modules.settings.domain.repository.WorkspaceSettingRepository;
import com.medflow.modules.settings.mapper.WorkspaceSettingMapper;
import com.medflow.shared.exception.ResourceNotFoundException;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class SettingsServiceImpl implements SettingsService {

  private final WorkspaceSettingRepository repository;
  private final WorkspaceSettingMapper mapper;

  SettingsServiceImpl(WorkspaceSettingRepository repository, WorkspaceSettingMapper mapper) {
    this.repository = repository;
    this.mapper = mapper;
  }

  @Override
  @Transactional(readOnly = true)
  public List<SettingResponse> getAll() {
    return repository.findAll().stream()
        .sorted(Comparator.comparing(WorkspaceSetting::getKey))
        .map(mapper::toResponse)
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public SettingResponse get(String key) {
    return mapper.toResponse(load(key));
  }

  @Override
  @Transactional
  public SettingResponse put(String key, UpdateSettingRequest request) {
    var setting = repository.findById(key)
        .map(existing -> {
          existing.changeValue(request.value());
          return existing;
        })
        .orElseGet(() -> repository.save(new WorkspaceSetting(key, request.value())));
    return mapper.toResponse(setting);
  }

  private WorkspaceSetting load(String key) {
    return repository.findById(key)
        .orElseThrow(() -> new ResourceNotFoundException("Setting not found: " + key));
  }
}
