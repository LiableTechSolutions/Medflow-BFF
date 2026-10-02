package com.medflow.modules.prescriptions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.medflow.modules.prescriptions.domain.entity.Prescription;
import com.medflow.shared.exception.BusinessRuleViolationException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class PrescriptionEditWindowTest {

  private Prescription issuedToday() {
    return new Prescription(1L, null, 2L, 3L, "Cold", "[]", true, null);
  }

  @Test
  void canBeEditedOnTheDayItWasIssuedAndAddsAFollowUp() {
    var prescription = issuedToday();
    var followUp = LocalDate.now().plusDays(15);

    prescription.update("Cold and cough", "[]", true, followUp);

    assertThat(prescription.getDiagnosis()).isEqualTo("Cold and cough");
    assertThat(prescription.getFollowUpDate()).isEqualTo(followUp);
  }

  @Test
  void isLockedOnceTheIssueDayHasPassed() {
    var prescription = issuedToday();
    ReflectionTestUtils.setField(prescription, "createdAt", Instant.now().minus(1, ChronoUnit.DAYS)
        .minus(1, ChronoUnit.HOURS));

    assertThat(prescription.isEditable()).isFalse();
    assertThatThrownBy(() -> prescription.update("Changed", "[]", true, null))
        .isInstanceOf(BusinessRuleViolationException.class);
  }

  @Test
  void cancelledPrescriptionsCannotBeEdited() {
    var prescription = issuedToday();
    prescription.cancel();

    assertThat(prescription.isEditable()).isFalse();
    assertThatThrownBy(() -> prescription.update("Changed", "[]", true, null))
        .isInstanceOf(BusinessRuleViolationException.class);
  }
}
