package com.medflow.modules.patients.domain.repository;

import com.medflow.modules.patients.domain.entity.RegistrationProfile;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegistrationProfileRepository extends JpaRepository<RegistrationProfile, Long> {

  Optional<RegistrationProfile> findByHospitalId(Long hospitalId);
}
