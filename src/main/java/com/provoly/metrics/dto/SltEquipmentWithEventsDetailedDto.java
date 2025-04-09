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
        super(equipment.getNbEquipWithEvent_CF(),
                equipment.getTotalEquipWithEvent_CF(),
                equipment.getNbServiceTodoWithEquip_CF(),
                equipment.getNbServiceInProgressWithEquip_CF(),
                equipment.getNbEquipWithEvent_unmanaged(),
                equipment.getTotalEquipWithEvent_unmanaged(),
                equipment.getNbServiceTodoWithEquip_unmanaged(),
                equipment.getNbServiceInProgressWithEquip_unmanaged());
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
