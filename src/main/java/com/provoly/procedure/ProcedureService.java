package com.provoly.procedure;

import java.util.stream.Stream;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import com.provoly.action.ActionMapper;
import com.provoly.action.ActionService;
import com.provoly.event.Event;
import com.provoly.event.EventService;
import com.provoly.event.dto.AlertEventWriteDto;
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
        logger.debug("Update events");
        for (var event : dto.events()) {
            if (event instanceof AlertEventWriteDto) {
                logger.debugf("Can't update alert event with id %s", dto.id());
                continue;
            }
            eventService.updateEvent(event.getId(), event);
        }

        logger.debugf("Remove %s actions", procedure.getActions().size());
        procedure.removeAllActions();

        logger.debug("Update actions");
        for (var action : dto.actions()) {
            actionService.saveActionForInstance(action, procedure);
        }

    }

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
}
