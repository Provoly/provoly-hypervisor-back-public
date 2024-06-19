package com.provoly.procedure;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import jakarta.persistence.*;

import com.provoly.action.Action;
import com.provoly.event.Event;
import com.provoly.event.Status;

import org.hibernate.annotations.CreationTimestamp;

@Entity
public class Procedure {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;

    @CreationTimestamp
    private Instant creationDate;

    private float procedureProgress;

    @OneToMany(mappedBy = "procedure", fetch = FetchType.EAGER)
    private List<Event> events = new ArrayList<>();

    @OneToMany(mappedBy = "procedure", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Collection<Action> actions = new ArrayList<>();

    public Procedure() {
        // Only for JPA
    }

    public Procedure(String name) {
        this.name = name;
        this.procedureProgress = 0;
    }

    public void addAction(Action action) {
        actions.add(action);
        action.setProcedure(this);
        setProcedureProgress(calculateProgressActions());
    }

    public void addEvent(Event event) {
        events.add(event);
        event.setProcedure(this);
    }

    public void deleteAction(Action action) {
        actions.remove(action);
        setProcedureProgress(calculateProgressActions());
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

    public List<Event> getEvents() {
        return events;
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
