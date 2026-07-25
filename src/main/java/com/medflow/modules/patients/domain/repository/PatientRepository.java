package com.medflow.modules.patients.domain.repository;
import com.medflow.modules.patients.domain.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface PatientRepository extends JpaRepository<Patient, UUID> { boolean existsByEmail(String email); }
