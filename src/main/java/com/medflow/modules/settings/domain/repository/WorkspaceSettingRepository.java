package com.medflow.modules.settings.domain.repository;

import com.medflow.modules.settings.domain.entity.WorkspaceSetting;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkspaceSettingRepository extends JpaRepository<WorkspaceSetting, Long> {

  List<WorkspaceSetting> findByHospitalIdOrderByKeyAsc(Long hospitalId);

  Optional<WorkspaceSetting> findByHospitalIdAndKey(Long hospitalId, String key);
}
