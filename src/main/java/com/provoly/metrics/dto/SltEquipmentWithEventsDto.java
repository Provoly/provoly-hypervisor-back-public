package com.provoly.metrics.dto;

public class SltEquipmentWithEventsDto {
    private final long nbEquipWithEvent_CF;
    private final long totalEquipWithEvent_CF;
    private final long nbServiceTodoWithEquip_CF;
    private final long nbServiceInProgressWithEquip_CF;

    private final long nbEquipWithEvent_unmanaged;
    private final long totalEquipWithEvent_unmanaged;
    private final long nbServiceTodoWithEquip_unmanaged;
    private final long nbServiceInProgressWithEquip_unmanaged;

    public SltEquipmentWithEventsDto(long nbEquipWithEventCF, long totalEquipWithEventCF, long nbServiceTodoWithEquipCF,
            long nbServiceInProgressWithEquipCF, long nbEquipWithEventUnmanaged,
            long totalEquipWithEventUnmanaged, long nbServiceTodoWithEquipUnmanaged,
            long nbServiceInProgressWithEquipUnmanaged) {
        nbEquipWithEvent_CF = nbEquipWithEventCF;
        totalEquipWithEvent_CF = totalEquipWithEventCF;
        nbServiceTodoWithEquip_CF = nbServiceTodoWithEquipCF;
        nbServiceInProgressWithEquip_CF = nbServiceInProgressWithEquipCF;
        nbEquipWithEvent_unmanaged = nbEquipWithEventUnmanaged;
        totalEquipWithEvent_unmanaged = totalEquipWithEventUnmanaged;
        nbServiceTodoWithEquip_unmanaged = nbServiceTodoWithEquipUnmanaged;
        nbServiceInProgressWithEquip_unmanaged = nbServiceInProgressWithEquipUnmanaged;
    }

    public long getNbEquipWithEvent_CF() {
        return nbEquipWithEvent_CF;
    }

    public long getTotalEquipWithEvent_CF() {
        return totalEquipWithEvent_CF;
    }

    public long getNbServiceTodoWithEquip_CF() {
        return nbServiceTodoWithEquip_CF;
    }

    public long getNbServiceInProgressWithEquip_CF() {
        return nbServiceInProgressWithEquip_CF;
    }

    public long getNbEquipWithEvent_unmanaged() {
        return nbEquipWithEvent_unmanaged;
    }

    public long getTotalEquipWithEvent_unmanaged() {
        return totalEquipWithEvent_unmanaged;
    }

    public long getNbServiceTodoWithEquip_unmanaged() {
        return nbServiceTodoWithEquip_unmanaged;
    }

    public long getNbServiceInProgressWithEquip_unmanaged() {
        return nbServiceInProgressWithEquip_unmanaged;
    }
}
