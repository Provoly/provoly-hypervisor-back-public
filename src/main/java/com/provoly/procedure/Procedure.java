package com.provoly.procedure;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import jakarta.persistence.*;

import com.provoly.action.Action;
import com.provoly.event.Status;

@Entity
public class Procedure {
    @Id
    private UUID id;

    private String name;

    private Instant creationDate;

    private float procedureProgress;

    @OneToMany(mappedBy = "procedure", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Collection<Action> actions = new ArrayList<>();

    public Procedure() {
        // Only for JPA
    }

    public Procedure(UUID id, String name, Instant creationDate) {
        this.id = id;
        this.name = name;
        this.creationDate = creationDate;
        this.procedureProgress = 0;
    }

    public void addAction(Action action) {
        actions.add(action);
        action.setProcedure(this);
        setProcedureProgress(calculateProgressActions());
    }

    public void deleteAction(Action action) {
        actions.remove(action);
        setProcedureProgress(calculateProgressActions());
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Instant getCreationDate() {
        return creationDate;
    }

    public Collection<Action> getActions() {
        return actions;
    }

    public float getProcedureProgress() {
        return procedureProgress;
    }

    public void setProcedureProgress(float procedureProgress) {
        this.procedureProgress = procedureProgress;
    }

    private float calculateProgressActions() {
        if (actions.isEmpty()) {
            return 0L;
        }
        double doneActions = actions.stream().filter(action -> action.getStatus() == Status.DONE).count();
        return Math.round((doneActions / actions.size()) * 100);
    }
}
