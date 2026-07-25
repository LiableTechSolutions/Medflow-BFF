package com.medflow.modules.settings.domain.repository;

import com.medflow.modules.settings.domain.entity.WorkspaceSetting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkspaceSettingRepository extends JpaRepository<WorkspaceSetting, String> {
}
