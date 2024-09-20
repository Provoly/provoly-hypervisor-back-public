package com.provoly.procedure;

import java.time.Instant;
import java.util.*;

import jakarta.persistence.*;

import com.provoly.action.Action;
import com.provoly.comment.Comment;
import com.provoly.event.Event;
import com.provoly.event.Status;

import org.hibernate.annotations.CreationTimestamp;

@Entity
public class Procedure {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;
    private String description;

    @CreationTimestamp
    private Instant creationDate;

    private float procedureProgress;

    @OneToMany(mappedBy = "procedure", fetch = FetchType.EAGER)
    private List<Event> events = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("order ASC")
    @JoinTable
    private List<Action> actions = new ArrayList<>();

    @OneToOne
    private Comment closeComment;

    public Procedure() {
        // Only for JPA
    }

    public Procedure(String name, String description) {
        this.name = name;
        this.description = description;
        this.procedureProgress = 0;
    }

    public void addAction(Action action) {
        actions.add(action);
        calculateProgressActions();
    }

    public void removeAction(Action action) {
        actions.remove(action);
        calculateProgressActions();
    }

    public void addEvent(Event event) {
        events.add(event);
        event.setProcedure(this);
        event.setStatus(Status.IN_PROGRESS);
    }

    public Optional<Action> getAction(UUID id) {
        return actions
                .stream()
                .filter(a -> a.getId().equals(id))
                .findFirst();
    }

    public void dissociateEvents() {
        events.forEach(event -> {
            event.setProcedure(null);
            event.setStatus(Status.NEW);
        });
        events.clear();
    }

    public void removeActions() {
        actions.clear();
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public Comment getCloseComment() {
        return closeComment;
    }

    public void setCloseComment(Comment closeComment) {
        this.closeComment = closeComment;
    }

    public void calculateProgressActions() {
        if (actions.isEmpty()) {
            setProcedureProgress(0);
            return;
        }
        double doneActions = actions.stream().filter(action -> action.getStatus() == Status.DONE).count();
        var result = Math.round((doneActions / actions.size()) * 100);
        setProcedureProgress(result);
    }
}
