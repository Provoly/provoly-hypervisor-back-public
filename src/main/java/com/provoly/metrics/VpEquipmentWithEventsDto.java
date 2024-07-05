package com.provoly.metrics;

public record VpEquipmentWithEventsDto(
        long nbEquipWithEvent_C,
        long totalEquipWithEvent_C,
        long nbServiceTodoWithEquip_C,
        long nbServiceInProgressWithEquip_C) {
}
