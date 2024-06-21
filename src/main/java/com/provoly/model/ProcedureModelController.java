package com.provoly.model;

import java.util.Collection;
import java.util.List;

import jakarta.validation.constraints.Positive;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import io.quarkus.security.Authenticated;

import org.jboss.resteasy.reactive.RestQuery;

@Path("/procedures/model")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProcedureModelController {

    private final ProcedureModelService procedureModelService;
    private final ProcedureModelMapper procedureModelMapper;

    public ProcedureModelController(ProcedureModelService procedureModelService, ProcedureModelMapper procedureModelMapper) {
        this.procedureModelService = procedureModelService;
        this.procedureModelMapper = procedureModelMapper;
    }

    @GET
    @Authenticated
    public Collection<ProcedureModelReadDto> getProceduresModel(
            @DefaultValue("1") @Positive @RestQuery int page,
            @DefaultValue("20") @Positive @RestQuery int pageSize,
            @RestQuery String sort,
            @DefaultValue("ASC") @RestQuery String order,
            @RestQuery List<String> domain,
            @RestQuery String search) {
        var models = procedureModelService.getProceduresModel(page, pageSize, sort, order, domain, search);
        return models.stream()
                .map(procedureModelMapper::mapToProcedureReadDetailsDto)
                .toList();
    }

    @POST
    @Authenticated
    public ProcedureModelReadDto saveProcedureModel(ProcedureModelWriteDto dto) {
        return procedureModelMapper.mapToProcedureReadDetailsDto(procedureModelService.saveProcedureModel(dto));
    }

    @Path("/id/{id}")
    @GET
    @Authenticated
    public ProcedureModelReadDto getProcedureModelDetails(Integer id) {
        return procedureModelMapper.mapToProcedureReadDetailsDto(procedureModelService.getProcedureModelDetails(id));
    }

    @Path("/id/{id}")
    @PUT
    @Authenticated
    public void updateProcedureModel(Integer id, ProcedureModelWriteDto dto) {
        procedureModelService.updateProcedureModel(id, dto);
    }

}
