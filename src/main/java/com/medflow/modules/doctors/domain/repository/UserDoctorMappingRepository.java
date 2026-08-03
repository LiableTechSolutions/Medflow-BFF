package com.medflow.modules.doctors.domain.repository;

import com.medflow.modules.doctors.domain.entity.UserDoctorMapping;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserDoctorMappingRepository extends JpaRepository<UserDoctorMapping, Long> {

  List<UserDoctorMapping> findByDoctorId(Long doctorId);

  boolean existsByUserIdAndDoctorId(Long userId, Long doctorId);
}
