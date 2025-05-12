package com.provoly.metrics.dto;

public class SltEquipmentWithEventsDto {

    private final long  nbEquipWithEvent_CA;
    private final long totalEquipWithEvent_CA;
    private final long nbServiceTodoWithEquip_CA;
    private final long nbServiceCreatedWithEquip_CA;
    private final long nbServiceInProgressWithEquip_CA;
    private final long nbEquipWithEvent_CO;
    private final long totalEquipWithEvent_CO;
    private final long nbServiceCreatedWithEquip_CO;
    private final long nbServiceTodoWithEquip_CO;
    private final long nbServiceInProgressWithEquip_CO;


    public SltEquipmentWithEventsDto(long nbEquipWithEventCa, long totalEquipWithEventCa, long nbServiceTodoWithEquipCa, long nbServiceCreatedWithEquipCa, long nbServiceInProgressWithEquipCa, long nbEquipWithEventCo, long totalEquipWithEventCo, long nbServiceCreatedWithEquipCo, long nbServiceTodoWithEquipCo, long nbServiceInProgressWithEquipCo) {
        nbEquipWithEvent_CA = nbEquipWithEventCa;
        totalEquipWithEvent_CA = totalEquipWithEventCa;
        nbServiceTodoWithEquip_CA = nbServiceTodoWithEquipCa;
        nbServiceCreatedWithEquip_CA = nbServiceCreatedWithEquipCa;
        nbServiceInProgressWithEquip_CA = nbServiceInProgressWithEquipCa;
        nbEquipWithEvent_CO = nbEquipWithEventCo;
        totalEquipWithEvent_CO = totalEquipWithEventCo;
        nbServiceCreatedWithEquip_CO = nbServiceCreatedWithEquipCo;
        nbServiceTodoWithEquip_CO = nbServiceTodoWithEquipCo;
        nbServiceInProgressWithEquip_CO = nbServiceInProgressWithEquipCo;
    }

    public long getNbEquipWithEvent_CA() {
        return nbEquipWithEvent_CA;
    }

    public long getTotalEquipWithEvent_CA() {
        return totalEquipWithEvent_CA;
    }

    public long getNbServiceTodoWithEquip_CA() {
        return nbServiceTodoWithEquip_CA;
    }

    public long getNbServiceCreatedWithEquip_CA() {
        return nbServiceCreatedWithEquip_CA;
    }

    public long getNbServiceInProgressWithEquip_CA() {
        return nbServiceInProgressWithEquip_CA;
    }

    public long getNbEquipWithEvent_CO() {
        return nbEquipWithEvent_CO;
    }

    public long getTotalEquipWithEvent_CO() {
        return totalEquipWithEvent_CO;
    }

    public long getNbServiceCreatedWithEquip_CO() {
        return nbServiceCreatedWithEquip_CO;
    }

    public long getNbServiceTodoWithEquip_CO() {
        return nbServiceTodoWithEquip_CO;
    }

    public long getNbServiceInProgressWithEquip_CO() {
        return nbServiceInProgressWithEquip_CO;
    }
}
