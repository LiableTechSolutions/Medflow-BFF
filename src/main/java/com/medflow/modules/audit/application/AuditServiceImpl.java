package com.medflow.modules.audit.application;

import com.medflow.modules.audit.api.AuditService;
import com.medflow.modules.audit.api.response.AuditLogResponse;
import com.medflow.modules.audit.domain.entity.AuditLog;
import com.medflow.modules.audit.domain.repository.AuditLogRepository;
import com.medflow.shared.api.PageResponse;
import com.medflow.shared.security.TenantContext;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
class AuditServiceImpl implements AuditService {

  private static final Logger log = LoggerFactory.getLogger(AuditServiceImpl.class);
  private static final int MAX_PAGE_SIZE = 100;

  private final AuditLogRepository repository;
  private final TenantContext tenantContext;

  AuditServiceImpl(AuditLogRepository repository, TenantContext tenantContext) {
    this.repository = repository;
    this.tenantContext = tenantContext;
  }

  /**
   * Runs in its own transaction: an audit failure must never roll back the business
   * operation that triggered it, and a rolled-back operation should not silently drop
   * the record that it was attempted.
   */
  @Override
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void record(String action, String entityType, Long entityId, String metadataJson) {
    try {
      var user = tenantContext.current().orElse(null);
      if (user == null || user.hospitalId() == null) {
        return;
      }
      repository.save(new AuditLog(user.hospitalId(), user.userId(), action, entityType, entityId,
          metadataJson, clientIp()));
    } catch (RuntimeException exception) {
      log.warn("Failed to write audit entry {} on {} {}", action, entityType, entityId, exception);
    }
  }

  @Override
  @Transactional(readOnly = true)
  public PageResponse<AuditLogResponse> search(Long hospitalId, String entityType, Long userId,
      int page, int size) {
    var pageable = PageRequest.of(Math.max(page, 0), Math.clamp(size, 1, MAX_PAGE_SIZE),
        Sort.by(Sort.Direction.DESC, "createdAt"));
    var normalizedType = (entityType == null || entityType.isBlank()) ? null : entityType.trim();
    return PageResponse.from(repository.search(hospitalId, normalizedType, userId, pageable)
        .map(entry -> new AuditLogResponse(entry.getId(), entry.getUserId(), entry.getAction(),
            entry.getEntityType(), entry.getEntityId(), entry.getMetadataJson(),
            entry.getIpAddress(), entry.getCreatedAt())));
  }

  private String clientIp() {
    var attributes = RequestContextHolder.getRequestAttributes();
    if (attributes instanceof ServletRequestAttributes servletAttributes) {
      HttpServletRequest request = servletAttributes.getRequest();
      var forwarded = request.getHeader("X-Forwarded-For");
      return (forwarded == null || forwarded.isBlank())
          ? request.getRemoteAddr()
          : forwarded.split(",")[0].trim();
    }
    return null;
  }
}
