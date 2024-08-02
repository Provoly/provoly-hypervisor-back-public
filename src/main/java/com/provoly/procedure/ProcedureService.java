package com.provoly.procedure;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import com.provoly.action.Action;
import com.provoly.action.ActionMapper;
import com.provoly.action.ActionService;
import com.provoly.action.dto.ActionWriteDto;
import com.provoly.event.Event;
import com.provoly.event.EventService;
import com.provoly.model.ProcedureModel;

import org.jboss.logging.Logger;

@ApplicationScoped
public class ProcedureService {
    private final ProcedureDatabaseReader databaseReader;
    private final EventService eventService;
    private final ActionService actionService;
    private final ActionMapper actionMapper;
    private final Logger logger;

    public ProcedureService(ProcedureDatabaseReader databaseReader, EventService eventService, ActionService actionService,
            ActionMapper actionMapper,
            Logger logger) {
        this.databaseReader = databaseReader;
        this.eventService = eventService;
        this.actionService = actionService;
        this.actionMapper = actionMapper;
        this.logger = logger;
    }

    @Transactional
    public Procedure getProcedureDetails(Integer id) {
        logger.debugf("Get procedure detail with id %s", id);
        return databaseReader.getProcedureById(id);
    }

    @Transactional
    public void closeAllProcedureEvents(Integer id) {
        logger.debugf("Close all procedure events with id %s", id);
        var events = getProcedureDetails(id).getEvents();
        events.forEach(eventService::closeEvent);
    }

    @Transactional
    public long getLinkedEventCountByProcedure(Procedure procedure) {
        if (procedure == null) {
            logger.debug("Procedure is null, count is 0");
            return 0;
        }
        return databaseReader.getLinkedEventCountForProcedure(procedure.getId());
    }

    @Transactional
    public void updateProcedure(Integer id, ProcedureWriteDto dto) {
        logger.infof("Update procedure %s with its %s actions and %s events", String.valueOf(id), dto.actions().size(),
                dto.events().size());

        var procedure = databaseReader.getProcedureById(id);
        // if role event_write sinon forbidden exception
        logger.debug("Update events");
        for (var event : dto.events()) {
            eventService.updateEvent(event.getId(), event);
        }

        logger.debug("Update actions");
        var currentActionIds = procedure.getActions().stream().map(Action::getId).collect(Collectors.toSet());
        var newActionIds = dto.actions().stream().map(ActionWriteDto::getId).collect(Collectors.toSet());

        if (currentActionIds.equals(newActionIds)) {
            //if(&& pas event_proc_write){
            //  throw new ForbiddenException("");
            //}
        }

        currentActionIds.removeAll(newActionIds);
        if (!currentActionIds.isEmpty()) {
            logger.debugf("delete actions with id %s", currentActionIds);
            for (var actionId : currentActionIds) {
                procedure.getAction(actionId).ifPresent(procedure::removeAction);
            }
        }

        int index = 0;
        for (var actionDto : dto.actions()) {
            actionService.saveActionForInstance(actionDto, procedure, index++);
        }

        logger.debugf("Procedure %s is updated".formatted(procedure.getId()));

    }

    @Transactional
    public Procedure instantiateProcedureWithModelAndEvents(ProcedureModel model, Stream<Event> events) {
        Procedure procedure = new Procedure(model.getName(), model.getDescription());
        logger.debugf("Add events to procedure");
        events.forEach(procedure::addEvent);

        logger.debugf("Add actions to procedure");
        model.getActions().forEach(action -> procedure.addAction(actionMapper.duplicateAction(action)));

        databaseReader.saveProcedure(procedure);
        logger.debugf("Procedure %s is instantiated".formatted(procedure.getId()));
        return procedure;
    }

    @Transactional
    public void deleteProcedure(Integer id) {
        var procedure = databaseReader.getProcedureById(id);
        procedure.dissociateEvents();
        databaseReader.removeProcedure(procedure);
    }
}
