package com.provoly.model;

import java.util.Collection;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import com.provoly.event.SortOrder;

import org.jboss.logging.Logger;

@ApplicationScoped
public class ProcedureModelService {

    private final Logger logger;
    private final ProcedureModelDatabaseReader databaseReader;
    private final ProcedureModelMapper procedureModelMapper;

    public ProcedureModelService(Logger logger, ProcedureModelDatabaseReader databaseReader,
            ProcedureModelMapper procedureModelMapper) {
        this.logger = logger;
        this.databaseReader = databaseReader;
        this.procedureModelMapper = procedureModelMapper;
    }

    @Transactional
    public ProcedureModel getProcedureModelDetails(Integer id) {
        logger.debugf("Get procedure model detail with id %s", id);
        return databaseReader.getProcedureModelById(id);
    }

    @Transactional
    public Collection<ProcedureModel> getProceduresModel(int page, int pageSize, String sort, String order,
            List<String> domains, String search) {
        logger.infof("""
                Get procedures models with from page %s and size %s with :
                domain : %s
                search on procedures model that contains : %s
                sort by %s
                """.formatted(page, pageSize, domains, search, sort));

        var domainEntities = domains.stream()
                .map(code -> databaseReader.getDomainByCode(code)
                        .orElseThrow(() -> new IllegalArgumentException("Domain name %s not found".formatted(code))))
                .toList();
        return databaseReader.getProcedureModels(page,
                pageSize,
                ProcedureModelSort.fromName(sort),
                SortOrder.fromString(order),
                domainEntities,
                search);
    }

    @Transactional
    public ProcedureModel saveProcedureModel(ProcedureModelWriteDto dto) {
        logger.debugf("Save procedure model with name %s and its %s actions", dto.name(), dto.actions().size());
        if (databaseReader.isProcedureModelWithNameExists(dto.name())) {
            throw new IllegalArgumentException("Procedure model with name '%s' already exists".formatted(dto.name()));
        }
        var model = new ProcedureModel(dto.creator());
        procedureModelMapper.updateProcedureModel(model, dto);
        databaseReader.saveProcedureModel(model);
        return model;
    }

    @Transactional
    public void updateProcedureModel(Integer id, ProcedureModelWriteDto dto) {
        logger.debugf("Update procedure model %s and its %s actions", String.valueOf(id), dto.actions().size());
        var model = databaseReader.getProcedureModelById(id);
        if (!dto.creator().equals(model.getCreator())) {
            throw new IllegalArgumentException(
                    "It's not possible to update Procedure model creator for procedure %s".formatted(model.getId()));
        }
        procedureModelMapper.updateProcedureModel(model, dto);
    }
}
