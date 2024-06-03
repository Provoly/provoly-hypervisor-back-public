package com.provoly.equipment;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import io.quarkus.security.Authenticated;

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
    @Authenticated
    public void saveOrUpdateEquipments(@Valid Collection<EquipmentWriteDto> equipments) {
        equipmentService.saveOrUpdateEquipments(equipments);
    }

    @GET
    @Authenticated
    public Collection<EquipmentReadDto> getEquipments(@RestQuery List<String> entity) {
        var equipments = equipmentService.getEquipments(entity);
        return equipmentMapper.mapToEquipmentReadDto(equipments);

    }

    @GET
    @Authenticated
    @Path("/id/{id}")
    public EquipmentReadDto getEquipmentDetails(UUID id) {
        var equipment = equipmentService.getEquipmentById(id);
        return equipmentMapper.mapToEquipmentReadDto(equipment);

    }

    @GET
    @Authenticated
    @Path("/name/{name}")
    public EquipmentReadDto getEquipmentDetails(String name) {
        var equipment = equipmentService.getEquipmentByName(name);
        return equipmentMapper.mapToEquipmentReadDto(equipment);

    }

    @GET
    @Authenticated
    @Path("/entities")
    public Collection<String> getEquipmentEntitiesName() {
        return equipmentService.getEquipmentEntitiesName();
    }

}
