package com.provoly.equipment;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

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

    @GET
    public Collection<EquipmentReadDto> getEquipments(@RestQuery String entity) {
        var equipments = equipmentService.getEquipments(entity);
        return equipmentMapper.mapToEquipmentReadDto(equipments);

    }

    @GET
    @Path("/id/{id}")
    public EquipmentReadDto getEquipmentDetails(UUID id) {
        var equipment = equipmentService.getEquipmentById(id);
        return equipmentMapper.mapToEquipmentReadDto(equipment);

    }

    @GET
    @Path("/name/{name}")
    public EquipmentReadDto getEquipmentDetails(String name) {
        var equipment = equipmentService.getEquipmentByName(name);
        return equipmentMapper.mapToEquipmentReadDto(equipment);

    }

    @GET
    @Path("/entities")
    public List<String> getEquipmentEntitiesName() {
        return equipmentService.getEquipmentEntitiesName();
    }

}
