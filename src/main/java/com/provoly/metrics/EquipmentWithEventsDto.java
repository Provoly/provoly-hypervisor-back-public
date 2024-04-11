package com.provoly.metrics;

public record EquipmentWithEventsDto(
        long nbEquipWithEvent_A,
        long totalEquipWithEvent_A,
        long nbServiceTodoWithEquip_A,
        long nbServiceInProgressWithEquip_A,

        long nbEquipWithEvent_FL,
        long totalEquipWithEvent_FL,
        long nbServiceTodoWithEquip_FL,
        long nbServiceInProgressWithEquip_FL,

        long nbEquipWithEvent_unmanaged,
        long totalEquipWithEvent_unmanaged,
        long nbServiceTodoWithEquip_unmanaged,
        long nbServiceInProgressWithEquip_unmanaged) {
}
