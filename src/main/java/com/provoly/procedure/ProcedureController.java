package com.provoly.procedure;

import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import com.provoly.user.Role;

import io.quarkus.security.Authenticated;

@Path("/procedures")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProcedureController {

    private final ProcedureService procedureService;
    private final ProcedureMapper procedureMapper;

    public ProcedureController(ProcedureService procedureService, ProcedureMapper procedureMapper) {
        this.procedureService = procedureService;
        this.procedureMapper = procedureMapper;
    }

    @Path("/id/{id}")
    @GET
    @RolesAllowed({ Role.STR_EVENT_READ })
    public ProcedureReadDto getProcedureDetails(Integer id) {
        var procedure = procedureService.getProcedureDetails(id);
        return procedureMapper.mapToProcedureReadDetailsDto(procedure);
    }

    @Path("/id/{id}")
    @DELETE
    @RolesAllowed({ Role.STR_EVENT_WRITE })
    public void deleteProcedure(Integer id) {
        procedureService.deleteProcedure(id);
    }

    @Path("/id/{id}/close")
    @PUT
    @RolesAllowed({ Role.STR_EVENT_WRITE })
    public void closeAllProcedureEvents(Integer id) {
        procedureService.closeAllProcedureEvents(id);
    }

    @Path("/id/{id}")
    @PUT
    @RolesAllowed({ Role.STR_EVENT_WRITE, Role.STR_EVENT_PROC_WRITE })
    public void updateProcedure(Integer id, @Valid ProcedureWriteDto dto) {
        procedureService.updateProcedure(id, dto);
    }

}
