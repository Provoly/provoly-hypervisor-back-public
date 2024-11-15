package com.provoly.metrics.dto;

public class EpEquipmentWithEventsDetailedDto extends EpEquipmentWithEventsDto {
    private final EpEquipmentByCategoryDto manifestation;
    private final EpEquipmentByCategoryDto outoforder;
    private final EpEquipmentByCategoryDto anomaly;
    private final EpEquipmentByCategoryDto limit;

    public EpEquipmentWithEventsDetailedDto(
            EpEquipmentWithEventsDto equipment,
            EpEquipmentByCategoryDto manifestation,
            EpEquipmentByCategoryDto outoforder,
            EpEquipmentByCategoryDto anomaly,
            EpEquipmentByCategoryDto limit) {
        super(equipment.getNbEquipWithEvent_A(),
                equipment.getTotalEquipWithEvent_A(),
                equipment.getNbServiceTodoWithEquip_A(),
                equipment.getNbServiceInProgressWithEquip_A(),
                equipment.getNbEquipWithEvent_FL(),
                equipment.getTotalEquipWithEvent_FL(),
                equipment.getNbServiceTodoWithEquip_FL(),
                equipment.getNbServiceInProgressWithEquip_FL(),
                equipment.getNbEquipWithEvent_unmanaged(),
                equipment.getTotalEquipWithEvent_unmanaged(),
                equipment.getNbServiceTodoWithEquip_unmanaged(),
                equipment.getNbServiceInProgressWithEquip_unmanaged());
        this.manifestation = manifestation;
        this.outoforder = outoforder;
        this.anomaly = anomaly;
        this.limit = limit;
    }

    public EpEquipmentByCategoryDto getManifestation() {
        return manifestation;
    }

    public EpEquipmentByCategoryDto getOutoforder() {
        return outoforder;
    }

    public EpEquipmentByCategoryDto getAnomaly() {
        return anomaly;
    }

    public EpEquipmentByCategoryDto getLimit() {
        return limit;
    }
}
