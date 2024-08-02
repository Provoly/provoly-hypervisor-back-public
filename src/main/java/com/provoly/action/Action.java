package com.provoly.action;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.*;

import com.provoly.event.Status;
import com.provoly.model.ProcedureModel;
import com.provoly.procedure.Procedure;

import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public class Action {
    @Id
    private UUID id;

    private String type;

    @UpdateTimestamp
    private Instant lastModificationDate;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(name = "action_order")
    private int order;

    @ManyToOne
    @Immutable
    private Procedure procedure;

    @ManyToOne
    @Immutable
    private ProcedureModel procedureModel;

    public Action() {
        // Only for JPA
    }

    public Action(UUID id) {
        this.id = id;
    }

    protected Action(UUID id, String type, Status status, int order) {
        this.id = id;
        this.type = type;
        this.status = status;
        this.order = order;
    }

    protected Action(int order, String type) {
        this.id = UUID.randomUUID();
        this.type = type;
        this.status = Status.NEW;
        this.order = order;

    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Instant getLastModificationDate() {
        return lastModificationDate;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public Procedure getProcedure() {
        return procedure;
    }

    public void setProcedure(Procedure procedure) {
        this.procedure = procedure;
    }

    public void setProcedureModel(ProcedureModel procedureModel) {
        this.procedureModel = procedureModel;
    }
}
