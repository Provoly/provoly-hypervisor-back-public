package com.provoly.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

import jakarta.persistence.*;

import com.provoly.action.Action;
import com.provoly.event.Domain;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
public class ProcedureModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;

    private String creator;

    private String description;

    private int useCount = 0;

    @ManyToOne
    private Domain domain;

    @CreationTimestamp
    @Column(updatable = false)
    private Instant creationDate;

    @UpdateTimestamp
    private Instant lastModificationDate;

    @OneToMany(mappedBy = "procedureModel", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("order ASC")
    private Collection<Action> actions = new ArrayList<>();

    public ProcedureModel() {
        // Only for JPA
    }

    public ProcedureModel(String creator) {
        this.creator = creator;
    }

    public ProcedureModel(String name, String creator, String description, Domain domain,
            Collection<Action> actions) {
        this.name = name;
        this.creator = creator;
        this.description = description;
        this.domain = domain;
        this.actions = actions;
    }

    public Optional<Action> getAction(UUID id) {
        return actions
                .stream()
                .filter(a -> a.getId().equals(id))
                .findFirst();
    }

    public void addAction(Action action) {
        actions.add(action);
        action.setProcedureModel(this);
    }

    public void removeAction(Action action) {
        actions.remove(action);
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Instant getCreationDate() {
        return creationDate;
    }

    public Instant getLastModificationDate() {
        return lastModificationDate;
    }

    public Collection<Action> getActions() {
        return actions;
    }

    public String getCreator() {
        return creator;
    }

    public Domain getDomain() {
        return domain;
    }

    public void setDomain(Domain domain) {
        this.domain = domain;
    }

    public void setActions(Collection<Action> actions) {
        this.actions = actions;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getUseCount() {
        return useCount;
    }

    public void incrementUseCount() {
        this.useCount++;
    }
}
