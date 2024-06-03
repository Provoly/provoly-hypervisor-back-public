package com.provoly.metrics;

import java.time.Instant;

public record AggregateServiceDto(Instant start, long count) {
}
