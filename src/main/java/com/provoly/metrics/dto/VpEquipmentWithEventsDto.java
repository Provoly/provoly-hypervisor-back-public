package com.provoly.metrics.dto;

public class VpEquipmentWithEventsDto {
    private final long nbEquipWithEvent_C;
    private final long totalEquipWithEvent_C;
    private final long nbServiceTodoWithEquip_C;
    private final long nbServiceInProgressWithEquip_C;

    public VpEquipmentWithEventsDto(long nbEquipWithEventC, long totalEquipWithEventC, long nbServiceTodoWithEquipC,
            long nbServiceInProgressWithEquipC) {
        nbEquipWithEvent_C = nbEquipWithEventC;
        totalEquipWithEvent_C = totalEquipWithEventC;
        nbServiceTodoWithEquip_C = nbServiceTodoWithEquipC;
        nbServiceInProgressWithEquip_C = nbServiceInProgressWithEquipC;
    }

    public long getNbEquipWithEvent_C() {
        return nbEquipWithEvent_C;
    }

    public long getTotalEquipWithEvent_C() {
        return totalEquipWithEvent_C;
    }

    public long getNbServiceTodoWithEquip_C() {
        return nbServiceTodoWithEquip_C;
    }

    public long getNbServiceInProgressWithEquip_C() {
        return nbServiceInProgressWithEquip_C;
    }

}
