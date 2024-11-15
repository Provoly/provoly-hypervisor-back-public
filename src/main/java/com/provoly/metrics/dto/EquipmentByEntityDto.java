package com.provoly.metrics.dto;

public record EquipmentByEntityDto(
        long aggloManaged,
        long aggloUnmanaged,
        long chManaged,
        long chUnmanaged,
        long fagnManaged,
        long fagnUnmanaged,
        long smpManaged,
        long smpUnmanaged) {
}
