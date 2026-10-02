package com.medflow.modules.beds.application;

import com.medflow.modules.beds.api.BedService;
import com.medflow.modules.patients.api.PatientDischargedEvent;
import org.springframework.modulith.ApplicationModuleListener;
import org.springframework.stereotype.Component;

/** A patient leaving hospital frees their bed; runs after the discharge has committed. */
@Component
class BedDischargeListener {

  private final BedService beds;

  BedDischargeListener(BedService beds) {
    this.beds = beds;
  }

  @ApplicationModuleListener
  void on(PatientDischargedEvent event) {
    beds.releaseForPatient(event.hospitalId(), event.patientId());
  }
}
