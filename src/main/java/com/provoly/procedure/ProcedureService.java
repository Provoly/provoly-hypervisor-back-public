package com.provoly.procedure;

import java.util.Collection;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import com.provoly.DatabaseReader;
import com.provoly.event.Event;
import com.provoly.event.EventService;
import com.provoly.event.dto.AlertEventWriteDto;
import com.provoly.event.dto.OperatorEventWriteDto;
import com.provoly.event.dto.ReportEventWriteDto;

import org.jboss.logging.Logger;

@ApplicationScoped
public class ProcedureService {
    private DatabaseReader databaseReader;
    private EventService eventService;
    private Logger logger;

    public ProcedureService(DatabaseReader databaseReader, EventService eventService, Logger logger) {
        this.databaseReader = databaseReader;
        this.eventService = eventService;
        this.logger = logger;
    }

    @Transactional
    public Procedure getProcedureDetails(UUID id) {
        logger.debugf("Get procedure detail with id %s", id);
        return databaseReader.getProcedureById(id);
    }

    @Transactional
    public void closeAllProcedureEvents(UUID id) {
        logger.debugf("Close all procedure events with id %s", id);
        var events = databaseReader.getEventsByProcedureId(id);
        events.forEach(event -> eventService.closeEvent(event));
    }

    @Transactional
    public Collection<Event> getEventsByProcedureId(UUID id) {
        logger.debugf("Get events linked to procedure with id %s", id);
        return databaseReader.getEventsByProcedureId(id);
    }

    @Transactional
    public long getLinkedEventCountByProcedure(Procedure procedure) {
        if (procedure == null) {
            logger.debug("Procedure is null, count is 0");
            return 0;
        }
        return databaseReader.getLinkedEventCountByProcedure(procedure.getId());
    }

    @Transactional
    public void updateProcedure(ProcedureWriteDto dto) {
        logger.debugf("Update procedure %s and its %s events", dto.id(), dto.events().size());

        for (var event : dto.events()) {
            switch (event) {
                case ReportEventWriteDto e -> eventService.updateReportEvent(e);
                case AlertEventWriteDto e -> logger.infof("Event %s is not updatable because it's an alert event", e.getId());
                case OperatorEventWriteDto e -> eventService.updateOperatorEvent(e);
                default -> throw new IllegalStateException("Unexpected value: " + event);
            }
        }
    }

}
