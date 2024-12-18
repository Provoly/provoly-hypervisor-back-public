package com.provoly.equipment;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import com.provoly.user.Role;

import org.jboss.resteasy.reactive.RestQuery;

@Path("/equipments")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EquipmentController {
    private EquipmentService equipmentService;

    private EquipmentMapper equipmentMapper;

    public EquipmentController(EquipmentService equipmentService, EquipmentMapper equipmentMapper) {
        this.equipmentService = equipmentService;
        this.equipmentMapper = equipmentMapper;
    }

    @POST
    @RolesAllowed({ Role.STR_EQUIPMENT_WRITE })
    public void saveOrUpdateEquipments(@Valid Collection<EquipmentWriteDto> equipments) {
        equipmentService.saveOrUpdateEquipments(equipments);
    }

    @GET
    @RolesAllowed({ Role.STR_EQUIPMENT_READ })
    public Collection<EquipmentReadDto> getEquipments(@RestQuery List<String> entity,
            @RestQuery List<String> family,
            @RestQuery String search,
            @DefaultValue("1") @Positive @RestQuery int page,
            @DefaultValue("20") @Positive @RestQuery int pageSize) {
        var equipments = equipmentService.getEquipments(entity, family, search, page, pageSize);
        return equipmentMapper.mapToEquipmentReadDto(equipments);

    }

    @GET
    @RolesAllowed({ Role.STR_EQUIPMENT_READ })
    @Path("/id/{id}")
    public EquipmentReadDto getEquipmentDetails(UUID id) {
        var equipment = equipmentService.getEquipmentById(id);
        return equipmentMapper.mapToEquipmentReadDto(equipment);

    }

    @GET
    @RolesAllowed({ Role.STR_EQUIPMENT_READ })
    @Path("/{source}/id/{id}")
    public EquipmentReadDto getEquipmentByExternalId(String source, String id) {
        var equipment = equipmentService.getEquipmentByIdExternalId(source, id);
        return equipmentMapper.mapToEquipmentReadDto(equipment);

    }

    @GET
    @RolesAllowed({ Role.STR_EQUIPMENT_READ })
    @Path("/name/{name}")
    public EquipmentReadDto getEquipmentDetails(String name) {
        var equipment = equipmentService.getEquipmentByName(name);
        return equipmentMapper.mapToEquipmentReadDto(equipment);

    }

}
