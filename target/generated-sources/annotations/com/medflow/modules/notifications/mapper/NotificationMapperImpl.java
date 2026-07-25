package com.medflow.modules.notifications.mapper;

import com.medflow.modules.notifications.api.NotificationCategory;
import com.medflow.modules.notifications.api.NotificationSeverity;
import com.medflow.modules.notifications.api.response.NotificationResponse;
import com.medflow.modules.notifications.domain.entity.Notification;
import java.time.Instant;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-07-25T19:48:22+0530",
    comments = "version: 1.6.3, compiler: Eclipse JDT (IDE) 3.46.100.v20260624-0231, environment: Java 21.0.11 (Eclipse Adoptium)"
)
@Component
public class NotificationMapperImpl implements NotificationMapper {

    @Override
    public NotificationResponse toResponse(Notification notification) {
        if ( notification == null ) {
            return null;
        }

        UUID id = null;
        NotificationCategory category = null;
        NotificationSeverity severity = null;
        String title = null;
        String message = null;
        boolean read = false;
        Instant createdAt = null;

        id = notification.getId();
        category = notification.getCategory();
        severity = notification.getSeverity();
        title = notification.getTitle();
        message = notification.getMessage();
        read = notification.isRead();
        createdAt = notification.getCreatedAt();

        NotificationResponse notificationResponse = new NotificationResponse( id, category, severity, title, message, read, createdAt );

        return notificationResponse;
    }
}
