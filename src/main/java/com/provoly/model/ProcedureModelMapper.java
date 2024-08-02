package com.provoly.model;

import jakarta.enterprise.context.ApplicationScoped;

import com.provoly.action.ActionMapper;
import com.provoly.event.Domain;

@ApplicationScoped
public class ProcedureModelMapper {

    private final ActionMapper actionMapper;
    private final ProcedureModelDatabaseReader databaseReader;

    public ProcedureModelMapper(ActionMapper actionMapper,
            ProcedureModelDatabaseReader databaseReader) {
        this.actionMapper = actionMapper;
        this.databaseReader = databaseReader;
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
    }

    private Domain mapToDomain(String domain) {
        return databaseReader.getDomainByCode(domain);
    }

}
