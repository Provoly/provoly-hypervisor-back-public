package com.provoly.metrics.dto;

import java.time.Instant;

public record AggregateAnomalyDto(Instant start, String subCategory, long count) {
}
