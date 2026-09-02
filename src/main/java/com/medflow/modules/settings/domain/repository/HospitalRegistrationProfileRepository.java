package com.medflow.modules.settings.domain.repository;

import com.medflow.modules.settings.domain.entity.HospitalRegistrationProfile;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HospitalRegistrationProfileRepository
    extends JpaRepository<HospitalRegistrationProfile, Long> {
  Optional<HospitalRegistrationProfile> findByHospitalId(Long hospitalId);
}
