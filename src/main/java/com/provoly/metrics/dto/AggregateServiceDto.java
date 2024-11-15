package com.provoly.metrics.dto;

import java.time.Instant;

public record AggregateServiceDto(Instant start, long count) {
}
