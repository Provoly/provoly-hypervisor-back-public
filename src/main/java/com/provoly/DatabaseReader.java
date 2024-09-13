package com.provoly;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.*;

import com.provoly.equipment.EquipmentEntity;
import com.provoly.event.Category;
import com.provoly.event.Category_;
import com.provoly.event.Domain;
import com.provoly.event.Domain_;

import org.apache.commons.lang3.StringUtils;

@ApplicationScoped
public class DatabaseReader {
    protected EntityManager em;

    public DatabaseReader() {
        // This constructor is needed to avoid following error :
        // "It's not possible to automatically add a synthetic no-args constructor to an unproxyable bean class."
    }

    public DatabaseReader(EntityManager em) {
        this.em = em;
    }

    public Expression<String> unaccent(CriteriaBuilder cb, Expression<String> property) {
        return cb.lower(cb.function("unaccent", String.class, property));
    }

    public Optional<Domain> getOptionalDomainByCode(String code) {
        var builder = em.getCriteriaBuilder();
        CriteriaQuery<Domain> criteriaQuery = builder.createQuery(Domain.class);
        Root<Domain> root = criteriaQuery.from(Domain.class);

        var query = criteriaQuery.select(root)
                .where(builder.equal(root.get(Domain_.code), code));

        return em.createQuery(query)
                .getResultStream()
                .findFirst();
    }

    public Domain getDomainByCode(String code) {
        return getOptionalDomainByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Domain %s invalid".formatted(code)));
    }

    public Optional<Category> getOptionalCategoryByCode(String code) {
        var builder = em.getCriteriaBuilder();
        CriteriaQuery<Category> criteriaQuery = builder.createQuery(Category.class);
        Root<Category> root = criteriaQuery.from(Category.class);

        var query = criteriaQuery.select(root)
                .where(builder.equal(root.get(Category_.code), code));

        return em.createQuery(query)
                .getResultStream()
                .findFirst();
    }

    public Category getCategoryByCode(String code) {
        return getOptionalCategoryByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Category %s invalid".formatted(code)));
    }

    public Collection<EquipmentEntity> getEquipmentEntities() {
        var builder = em.getCriteriaBuilder();
        CriteriaQuery<EquipmentEntity> criteriaQuery = builder.createQuery(EquipmentEntity.class);
        Root<EquipmentEntity> root = criteriaQuery.from(EquipmentEntity.class);

        return em.createQuery(criteriaQuery.select(root))
                .getResultList();
    }

    protected Predicate[] getPredicatesAsArray(List<Predicate> predicatesList) {
        Predicate[] pred = new Predicate[predicatesList.size()];
        predicatesList.toArray(pred);
        return pred;
    }

    protected String formatId(String search) {
        try {
            return stripAccentAndAddPercents(Integer.valueOf(search).toString());
        } catch (NumberFormatException e) {
            return "";
        }
    }

    protected String stripAccentAndAddPercents(String search) {
        return "%" + StringUtils.stripAccents(search).toLowerCase() + "%";
    }

}