package com.provoly.model;

import java.util.Collection;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import com.provoly.procedure.ProcedureMapper;
import com.provoly.procedure.ProcedureReadDto;

import io.quarkus.security.Authenticated;

import org.jboss.resteasy.reactive.RestQuery;

@Path("/procedures/model")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProcedureModelController {

    private final ProcedureModelService procedureModelService;
    private final ProcedureModelMapper procedureModelMapper;
    private final ProcedureMapper procedureMapper;

    public ProcedureModelController(ProcedureModelService procedureModelService, ProcedureModelMapper procedureModelMapper,
            ProcedureMapper procedureMapper) {
        this.procedureModelService = procedureModelService;
        this.procedureModelMapper = procedureModelMapper;
        this.procedureMapper = procedureMapper;
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
    public ProcedureModelReadDto saveProcedureModel(@Valid ProcedureModelWriteDto dto) {
        var model = procedureModelService.saveProcedureModel(dto);
        return procedureModelMapper.mapToProcedureReadDetailsDto(model);
    }

    @Path("/id/{id}")
    @GET
    @Authenticated
    public ProcedureModelReadDto getProcedureModelDetails(Integer id) {
        var model = procedureModelService.getProcedureModelDetails(id);
        return procedureModelMapper.mapToProcedureReadDetailsDto(model);
    }

    @Path("/id/{id}")
    @PUT
    @Authenticated
    public void updateProcedureModel(Integer id, @Valid ProcedureModelWriteDto dto) {
        procedureModelService.updateProcedureModel(id, dto);
    }

    @Path("/id/{id}/associate")
    @PUT
    @Authenticated
    public ProcedureReadDto associateProcedureModelToEvents(Integer id, List<Integer> eventIds) {
        var procedure = procedureModelService.associateProcedureModelToEvents(id, eventIds);
        return procedureMapper.mapToProcedureReadDetailsDto(procedure);
    }

}
