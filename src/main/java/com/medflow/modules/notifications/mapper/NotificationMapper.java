package com.medflow.modules.notifications.mapper;

import com.medflow.modules.notifications.api.response.NotificationResponse;
import com.medflow.modules.notifications.domain.entity.Notification;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

  NotificationResponse toResponse(Notification notification);
}
