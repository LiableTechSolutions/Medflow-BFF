package com.medflow.modules.settings.mapper;

import com.medflow.modules.settings.api.response.SettingResponse;
import com.medflow.modules.settings.domain.entity.WorkspaceSetting;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface WorkspaceSettingMapper {

  SettingResponse toResponse(WorkspaceSetting setting);
}
