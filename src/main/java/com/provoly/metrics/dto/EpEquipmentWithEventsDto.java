package com.provoly.metrics.dto;

public class EpEquipmentWithEventsDto {
    private final long nbEquipWithEvent_A;
    private final long totalEquipWithEvent_A;
    private final long nbServiceTodoWithEquip_A;
    private final long nbServiceInProgressWithEquip_A;

    private final long nbEquipWithEvent_FL;
    private final long totalEquipWithEvent_FL;
    private final long nbServiceTodoWithEquip_FL;
    private final long nbServiceInProgressWithEquip_FL;

    private final long nbEquipWithEvent_unmanaged;
    private final long totalEquipWithEvent_unmanaged;
    private final long nbServiceTodoWithEquip_unmanaged;
    private final long nbServiceInProgressWithEquip_unmanaged;

    public EpEquipmentWithEventsDto(long nbEquipWithEventA, long totalEquipWithEventA, long nbServiceTodoWithEquipA,
            long nbServiceInProgressWithEquipA, long nbEquipWithEventFl, long totalEquipWithEventFl,
            long nbServiceTodoWithEquipFl, long nbServiceInProgressWithEquipFl, long nbEquipWithEventUnmanaged,
            long totalEquipWithEventUnmanaged, long nbServiceTodoWithEquipUnmanaged,
            long nbServiceInProgressWithEquipUnmanaged) {
        nbEquipWithEvent_A = nbEquipWithEventA;
        totalEquipWithEvent_A = totalEquipWithEventA;
        nbServiceTodoWithEquip_A = nbServiceTodoWithEquipA;
        nbServiceInProgressWithEquip_A = nbServiceInProgressWithEquipA;
        nbEquipWithEvent_FL = nbEquipWithEventFl;
        totalEquipWithEvent_FL = totalEquipWithEventFl;
        nbServiceTodoWithEquip_FL = nbServiceTodoWithEquipFl;
        nbServiceInProgressWithEquip_FL = nbServiceInProgressWithEquipFl;
        nbEquipWithEvent_unmanaged = nbEquipWithEventUnmanaged;
        totalEquipWithEvent_unmanaged = totalEquipWithEventUnmanaged;
        nbServiceTodoWithEquip_unmanaged = nbServiceTodoWithEquipUnmanaged;
        nbServiceInProgressWithEquip_unmanaged = nbServiceInProgressWithEquipUnmanaged;
    }

    public long getNbEquipWithEvent_A() {
        return nbEquipWithEvent_A;
    }

    public long getTotalEquipWithEvent_A() {
        return totalEquipWithEvent_A;
    }

    public long getNbServiceTodoWithEquip_A() {
        return nbServiceTodoWithEquip_A;
    }

    public long getNbServiceInProgressWithEquip_A() {
        return nbServiceInProgressWithEquip_A;
    }

    public long getNbEquipWithEvent_FL() {
        return nbEquipWithEvent_FL;
    }

    public long getTotalEquipWithEvent_FL() {
        return totalEquipWithEvent_FL;
    }

    public long getNbServiceTodoWithEquip_FL() {
        return nbServiceTodoWithEquip_FL;
    }

    public long getNbServiceInProgressWithEquip_FL() {
        return nbServiceInProgressWithEquip_FL;
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
