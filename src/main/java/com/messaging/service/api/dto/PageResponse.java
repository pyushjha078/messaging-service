package com.messaging.service.api.dto;

import java.util.List;

public record PageResponse<T>(List<T> items, String nextCursor) {
}
