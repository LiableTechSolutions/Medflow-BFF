/**
 * Laboratory module: lab order intake, processing workflow and result capture.
 * Publishes {@code LabOrderCompletedEvent} so the notifications module can surface
 * "results ready" alerts without coupling.
 */
@org.springframework.modulith.ApplicationModule(displayName = "Laboratory")
package com.medflow.modules.laboratory;
