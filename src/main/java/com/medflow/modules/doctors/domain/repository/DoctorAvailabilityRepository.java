package com.medflow.modules.doctors.domain.repository;

import com.medflow.modules.doctors.domain.entity.DoctorAvailability;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DoctorAvailabilityRepository extends JpaRepository<DoctorAvailability, Long> {

  List<DoctorAvailability> findByDoctorIdOrderByDayOfWeekAscStartTimeAsc(Long doctorId);

  Optional<DoctorAvailability> findByIdAndDoctorId(Long id, Long doctorId);
}
