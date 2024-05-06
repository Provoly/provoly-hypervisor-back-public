package com.provoly;

import java.time.Instant;
import java.util.UUID;

import com.provoly.event.Status;

public record CondensedService(UUID id, Status status, Instant closeDate) {
}
