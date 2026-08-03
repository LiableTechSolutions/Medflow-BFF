package com.medflow.modules.audit.api;

import com.medflow.modules.audit.api.response.AuditLogResponse;
import com.medflow.shared.api.PageResponse;

/** Public API of the Audit module. */
public interface AuditService {

  /**
   * Appends an entry for the acting user, taken from the current request. Auditing must
   * never break the operation being audited, so failures here are logged, not rethrown.
   */
  void record(String action, String entityType, Long entityId, String metadataJson);

  PageResponse<AuditLogResponse> search(Long hospitalId, String entityType, Long userId,
      int page, int size);
}
