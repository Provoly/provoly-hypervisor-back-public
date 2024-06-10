package com.provoly.procedure;

import java.util.UUID;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import io.quarkus.security.Authenticated;

@Path("/procedures")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProcedureController {

    private ProcedureService procedureService;
    private ProcedureMapper procedureMapper;

    public ProcedureController(ProcedureService procedureService, ProcedureMapper procedureMapper) {
        this.procedureService = procedureService;
        this.procedureMapper = procedureMapper;
    }

    @Path("/id/{id}")
    @GET
    @Authenticated
    public ProcedureReadDto getProcedureDetail(UUID id) {
        var procedure = procedureService.getProcedureDetails(id);
        return procedureMapper.mapToProcedureReadDetailsDto(procedure);
    }

    @Path("/id/{id}/close")
    @PUT
    @Authenticated
    public void closeAllProcedureEvents(UUID id) {
        procedureService.closeAllProcedureEvents(id);
    }

    @POST
    @Authenticated
    public void updateProcedure(ProcedureWriteDto dto) {
        procedureService.updateProcedure(dto);
    }

}
