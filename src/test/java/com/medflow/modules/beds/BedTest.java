package com.medflow.modules.beds;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.medflow.modules.beds.api.BedStatus;
import com.medflow.modules.beds.domain.entity.Bed;
import com.medflow.shared.exception.BusinessRuleViolationException;
import org.junit.jupiter.api.Test;

class BedTest {

  private Bed freeBed() {
    return new Bed(1L, 1L, 1);
  }

  @Test
  void aNewBedIsAvailable() {
    var bed = freeBed();
    assertThat(bed.getStatus()).isEqualTo(BedStatus.AVAILABLE);
    assertThat(bed.getPatientId()).isNull();
  }

  @Test
  void assigningAndReleasingRoundTrips() {
    var bed = freeBed();

    bed.assign(7L);
    assertThat(bed.getStatus()).isEqualTo(BedStatus.OCCUPIED);
    assertThat(bed.getPatientId()).isEqualTo(7L);
    assertThat(bed.getOccupiedAt()).isNotNull();

    bed.release();
    assertThat(bed.getStatus()).isEqualTo(BedStatus.AVAILABLE);
    assertThat(bed.getPatientId()).isNull();
    assertThat(bed.getOccupiedAt()).isNull();
  }

  @Test
  void maintenanceCanBeToggledOnAFreeBed() {
    var bed = freeBed();

    bed.setMaintenance(true);
    assertThat(bed.getStatus()).isEqualTo(BedStatus.MAINTENANCE);

    bed.setMaintenance(false);
    assertThat(bed.getStatus()).isEqualTo(BedStatus.AVAILABLE);
  }

  @Test
  void anOccupiedBedCannotBeAssignedAgain() {
    var bed = freeBed();
    bed.assign(7L);

    assertThatThrownBy(() -> bed.assign(8L)).isInstanceOf(BusinessRuleViolationException.class);
    assertThat(bed.getPatientId()).isEqualTo(7L);
  }

  @Test
  void aBedUnderMaintenanceCannotBeAssigned() {
    var bed = freeBed();
    bed.setMaintenance(true);

    assertThatThrownBy(() -> bed.assign(7L)).isInstanceOf(BusinessRuleViolationException.class);
  }

  @Test
  void aFreeBedCannotBeReleased() {
    assertThatThrownBy(() -> freeBed().release()).isInstanceOf(BusinessRuleViolationException.class);
  }

  @Test
  void anOccupiedBedCannotGoIntoMaintenance() {
    var bed = freeBed();
    bed.assign(7L);

    assertThatThrownBy(() -> bed.setMaintenance(true))
        .isInstanceOf(BusinessRuleViolationException.class);
    assertThat(bed.getStatus()).isEqualTo(BedStatus.OCCUPIED);
  }
}
