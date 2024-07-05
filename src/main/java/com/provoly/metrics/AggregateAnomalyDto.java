package com.provoly.metrics;

import java.time.Instant;

public record AggregateAnomalyDto(Instant start, String subCategory, long count) {
}
