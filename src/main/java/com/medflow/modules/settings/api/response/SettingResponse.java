package com.medflow.modules.settings.api.response;

import java.time.Instant;

public record SettingResponse(String key, String value, Instant updatedAt) {
}
