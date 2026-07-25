package com.medflow.modules.settings.mapper;

import com.medflow.modules.settings.api.response.SettingResponse;
import com.medflow.modules.settings.domain.entity.WorkspaceSetting;
import java.time.Instant;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-07-25T20:09:13+0530",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.7 (Oracle Corporation)"
)
@Component
public class WorkspaceSettingMapperImpl implements WorkspaceSettingMapper {

    @Override
    public SettingResponse toResponse(WorkspaceSetting setting) {
        if ( setting == null ) {
            return null;
        }

        String key = null;
        String value = null;
        Instant updatedAt = null;

        key = setting.getKey();
        value = setting.getValue();
        updatedAt = setting.getUpdatedAt();

        SettingResponse settingResponse = new SettingResponse( key, value, updatedAt );

        return settingResponse;
    }
}
