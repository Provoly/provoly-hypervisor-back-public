package com.provoly.procedure;

import java.util.Collection;

import jakarta.enterprise.context.ApplicationScoped;

import com.provoly.action.ActionMapper;
import com.provoly.event.Event;
import com.provoly.event.EventMapper;

@ApplicationScoped
public class ProcedureMapper {

    private EventMapper eventMapper;
    private ActionMapper actionMapper;

    public ProcedureMapper(EventMapper eventMapper, ActionMapper actionMapper) {
        this.eventMapper = eventMapper;
        this.actionMapper = actionMapper;
    }

    public ProcedureReadDto mapToProcedureReadDetailsDto(Procedure procedure, Collection<Event> events) {
        return new ProcedureReadDto(
                procedure.getId(),
                procedure.getName(),
                procedure.getCreationDate(),
                actionMapper.mapToActionReadDto(procedure.getActions()),
                eventMapper.mapToEventReadDto(events),
                procedure.getProcedureProgress());
    }

}
