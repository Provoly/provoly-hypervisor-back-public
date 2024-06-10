package com.provoly.procedure;

import java.util.Comparator;

import jakarta.enterprise.context.ApplicationScoped;

import com.provoly.action.ActionMapper;
import com.provoly.event.Event;
import com.provoly.event.EventMapper;

@ApplicationScoped
public class ProcedureMapper {

    private final EventMapper eventMapper;
    private final ActionMapper actionMapper;

    public ProcedureMapper(EventMapper eventMapper, ActionMapper actionMapper) {
        this.eventMapper = eventMapper;
        this.actionMapper = actionMapper;
    }

    public ProcedureReadDto mapToProcedureReadDetailsDto(Procedure procedure) {
        return new ProcedureReadDto(
                procedure.getId(),
                procedure.getName(),
                procedure.getCreationDate(),
                actionMapper.mapToActionReadDto(procedure.getActions()),
                eventMapper.mapToEventReadDto(procedure.getEvents()
                        .stream()
                        .sorted(Comparator.comparing(Event::getCreationDate).reversed())),
                procedure.getProcedureProgress());
    }

}
