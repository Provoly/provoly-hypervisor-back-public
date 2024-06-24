package com.provoly.procedure;

import java.util.stream.Stream;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import com.provoly.event.Event;
import com.provoly.event.EventService;
import com.provoly.event.dto.AlertEventWriteDto;
import com.provoly.model.ProcedureModel;

import org.jboss.logging.Logger;

@ApplicationScoped
public class ProcedureService {
    private ProcedureDatabaseReader databaseReader;
    private EventService eventService;
    private Logger logger;

    public ProcedureService(ProcedureDatabaseReader databaseReader, EventService eventService,
            Logger logger) {
        this.databaseReader = databaseReader;
        this.eventService = eventService;
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
        events.forEach(event -> eventService.closeEvent(event));
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
        logger.debugf("Update procedure %s and its %s events", String.valueOf(id), dto.events().size());

        for (var event : dto.events()) {
            if (event instanceof AlertEventWriteDto) {
                logger.debugf("Can't update alert event with id %s", dto.id());
                continue;
            }
            eventService.updateEvent(event.getId(), event);
        }
    }

    public Procedure instantiateProcedureWithModelAndEvents(ProcedureModel model, Stream<Event> events) {
        Procedure procedure = new Procedure(model.getName(), model.getDescription());
        logger.debugf("Add events to procedure");
        events.forEach(procedure::addEvent);

        logger.debugf("Add actions to procedure");
        model.getActions().forEach(procedure::addAction);

        databaseReader.saveProcedure(procedure);
        logger.debugf("Procedure %s is instantiated".formatted(procedure.getId()));
        return procedure;
    }
}
