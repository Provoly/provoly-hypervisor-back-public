package com.provoly.metrics;

public record EquipmentByEntityDto(
        long CHA_managed,
        long CHA_unmanaged,
        long CH_managed,
        long CH_unmanaged,
        long FAGN_managed,
        long FAGN_unmanaged,
        long SMP_managed,
        long SMP_unmanaged) {
}
