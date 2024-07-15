package com.provoly.metrics;

public class VpEquipmentWithEventsDetailedDto extends VpEquipmentWithEventsDto {
    private final VpEquipmentByCategoryDto manifestation;
    private final VpEquipmentByCategoryDto outoforder;
    private final VpEquipmentByCategoryDto anomaly;
    private final VpEquipmentByCategoryDto limit;

    public VpEquipmentWithEventsDetailedDto(VpEquipmentWithEventsDto equipment,
            VpEquipmentByCategoryDto manifestation,
            VpEquipmentByCategoryDto outoforder,
            VpEquipmentByCategoryDto anomaly,
            VpEquipmentByCategoryDto limit) {
        super(equipment.getNbEquipWithEvent_C(),
                equipment.getTotalEquipWithEvent_C(),
                equipment.getNbServiceTodoWithEquip_C(),
                equipment.getNbServiceInProgressWithEquip_C());
        this.manifestation = manifestation;
        this.outoforder = outoforder;
        this.anomaly = anomaly;
        this.limit = limit;
    }

    public VpEquipmentByCategoryDto getManifestation() {
        return manifestation;
    }

    public VpEquipmentByCategoryDto getOutoforder() {
        return outoforder;
    }

    public VpEquipmentByCategoryDto getAnomaly() {
        return anomaly;
    }

    public VpEquipmentByCategoryDto getLimit() {
        return limit;
    }
}