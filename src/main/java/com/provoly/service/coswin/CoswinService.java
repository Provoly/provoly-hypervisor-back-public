package com.provoly.service.coswin;

import java.io.IOException;

import jakarta.enterprise.context.ApplicationScoped;

import com.provoly.equipment.Equipment;
import com.provoly.equipment.EquipmentService;
import com.provoly.service.ExternalServiceWriteDto;
import com.provoly.service.Priority;
import com.provoly.user.UserService;

@ApplicationScoped
public class CoswinService {
    public static final String DEFAULT_CITY = "CHALONS";
    public static final String DEFAULT_DOMAIN = "EP";
    private final EquipmentService equipmentService;
    private final UserService userService;
    private final CoswinClient coswinClient;
    private final ServiceTypeService typeService;

    public CoswinService(EquipmentService equipmentService,
            UserService userService,
            CoswinClient coswinClient,
            ServiceTypeService typeService) {
        this.equipmentService = equipmentService;
        this.userService = userService;
        this.coswinClient = coswinClient;
        this.typeService = typeService;
    }

    public CoswinServiceWriteDto mapToCoswinService(ExternalServiceWriteDto dto) {
        Equipment equipment = null;
        if (dto.equipment() != null) {
            equipment = equipmentService.getEquipmentByName(dto.equipment());
        }

        if (!Priority.priorityExists(dto.priority())) {
            throw new IllegalArgumentException("Priority %s invalid".formatted(dto.priority()));
        }

        var serviceType = typeService.getServiceType(dto.type());

        return new CoswinServiceWriteDto(
                "MDEBURE", // TODO: use current user
                dto.priority(),
                dto.name(),
                dto.type(),
                dto.description(),
                serviceType.getGti(),
                serviceType.getGtr(),
                serviceType.getGtrp(),
                equipment != null ? equipment.getCode() : null,
                equipment != null ? equipment.getDomain().getCode() : DEFAULT_DOMAIN,
                equipment != null ? equipment.getDistrict().getCode() : DEFAULT_CITY);
    }

    public String sendExternalService(ExternalServiceWriteDto dto) throws IOException {
        var coswinService = mapToCoswinService(dto);
        return coswinClient.sendExternalService(coswinService);
    }

}
