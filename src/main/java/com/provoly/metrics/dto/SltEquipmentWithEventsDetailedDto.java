package com.provoly.metrics.dto;

public class SltEquipmentWithEventsDetailedDto extends SltEquipmentWithEventsDto {
    private final SltEquipmentByCategoryDto manifestation;
    private final SltEquipmentByCategoryDto outoforder;
    private final SltEquipmentByCategoryDto anomaly;
    private final SltEquipmentByCategoryDto limit;

    public SltEquipmentWithEventsDetailedDto(
            SltEquipmentWithEventsDto equipment,
            SltEquipmentByCategoryDto manifestation,
            SltEquipmentByCategoryDto outoforder,
            SltEquipmentByCategoryDto anomaly,
            SltEquipmentByCategoryDto limit) {
        super(equipment.getNbEquipWithEvent_CA(),
                equipment.getTotalEquipWithEvent_CA(),
                equipment.getNbServiceTodoWithEquip_CA(),
                equipment.getNbServiceCreatedWithEquip_CA(),
                equipment.getNbServiceInProgressWithEquip_CA(),
                equipment.getNbEquipWithEvent_CO(),
                equipment.getTotalEquipWithEvent_CO(),
                equipment.getNbServiceCreatedWithEquip_CO(),
                equipment.getNbServiceTodoWithEquip_CO(),
                equipment.getNbServiceInProgressWithEquip_CO()
                );
        this.manifestation = manifestation;
        this.outoforder = outoforder;
        this.anomaly = anomaly;
        this.limit = limit;
    }

    public SltEquipmentByCategoryDto getManifestation() {
        return manifestation;
    }

    public SltEquipmentByCategoryDto getOutoforder() {
        return outoforder;
    }

    public SltEquipmentByCategoryDto getAnomaly() {
        return anomaly;
    }

    public SltEquipmentByCategoryDto getLimit() {
        return limit;
    }
}
