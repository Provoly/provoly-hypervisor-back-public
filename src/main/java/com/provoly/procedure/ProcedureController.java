package com.provoly.procedure;

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
    public ProcedureReadDto getProcedureDetails(Integer id) {
        var procedure = procedureService.getProcedureDetails(id);
        return procedureMapper.mapToProcedureReadDetailsDto(procedure);
    }

    @Path("/id/{id}/close")
    @PUT
    @Authenticated
    public void closeAllProcedureEvents(Integer id) {
        procedureService.closeAllProcedureEvents(id);
    }

    @Path("/id/{id}")
    @PUT
    @Authenticated
    public void updateProcedure(Integer id, ProcedureWriteDto dto) {
        procedureService.updateProcedure(id, dto);
    }

}
