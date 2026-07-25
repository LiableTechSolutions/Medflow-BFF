package com.medflow;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
class ModularityTest {
  @Test void verifiesModuleBoundaries() { ApplicationModules.of(MedflowApplication.class).verify(); }
}
