package com.provoly.model;

import jakarta.enterprise.context.ApplicationScoped;

import com.provoly.action.ActionDatabaseReader;
import com.provoly.action.ActionMapper;
import com.provoly.action.AskedService;
import com.provoly.event.Domain;

@ApplicationScoped
public class ProcedureModelMapper {

    private final ActionMapper actionMapper;
    private final ActionDatabaseReader actionDatabaseReader;

    public ProcedureModelMapper(ActionMapper actionMapper, ActionDatabaseReader actionDatabaseReader) {
        this.actionMapper = actionMapper;
        this.actionDatabaseReader = actionDatabaseReader;
    }

    public ProcedureModelReadDto mapToProcedureReadDetailsDto(ProcedureModel model) {
        return new ProcedureModelReadDto(
                model.getId(),
                model.getName(),
                model.getDescription(),
                model.getCreationDate(),
                model.getLastModificationDate(),
                model.getCreator(),
                model.getDomain().getCode(),
                model.getUseCount(),
                actionMapper.mapToActionReadDto(model.getActions()));
    }

    public void updateProcedureModel(ProcedureModel entity, ProcedureModelWriteDto dto) {
        entity.setName(dto.name());
        entity.setDescription(dto.description());
        entity.setDomain(mapToDomain(dto.domain()));
        for (var dtoAction : dto.actions()) {
            actionDatabaseReader.getActionById(dtoAction.id()).ifPresentOrElse(
                    action -> actionMapper.updateAction(action, dtoAction),
                    () -> actionMapper.updateAction(new AskedService(dtoAction.id()), dtoAction));
        }
    }

    private Domain mapToDomain(String domain) {
        return actionDatabaseReader
                .getDomainByCode(domain)
                .orElseThrow(() -> new IllegalArgumentException("Domain name %s not found".formatted(domain)));
    }

}
