package com.provoly.event.dto;

import java.util.List;

public record EventsSummariesByStatusDto(Long count, List<EventSummaryDto> events) {
}
