package com.provoly.event;

import java.time.Instant;
import java.util.Objects;

import jakarta.persistence.*;

import com.provoly.equipment.Equipment;
import com.provoly.procedure.Procedure;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String name;
    private String address;
    private String description;

    @ManyToOne
    private Domain domain;

    @Enumerated(EnumType.STRING)
    private Criticality criticality;

    @Enumerated(EnumType.STRING)
    private Category category;

    @Enumerated(EnumType.STRING)
    private EventType type;

    @Enumerated(EnumType.STRING)
    private Status status;

    @CreationTimestamp
    @Column(updatable = false)
    private Instant creationDate;

    @UpdateTimestamp
    private Instant lastModificationDate;

    private Instant closeDate;

    @ManyToOne
    @JoinColumn(name = "equipment_id")
    private Equipment equipment;

    @ManyToOne
    @JoinColumn(name = "procedure_id")
    private Procedure procedure;

    protected Event() {
        // Only for JPA
    }

    protected Event(EventType type) {
        this.type = type;
        this.status = Status.NEW;
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Criticality getCriticality() {
        return criticality;
    }

    public void setCriticality(Criticality criticality) {
        this.criticality = criticality;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public EventType getType() {
        return type;
    }

    public void setType(EventType type) {
        this.type = type;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Instant getCreationDate() {
        return creationDate;
    }

    public Instant getLastModificationDate() {
        return lastModificationDate;
    }

    public void setLastModificationDate(Instant lastModificationDate) {
        this.lastModificationDate = lastModificationDate;
    }

    public Instant getCloseDate() {
        return closeDate;
    }

    public void setCloseDate(Instant closeDate) {
        this.closeDate = closeDate;
    }

    public Equipment getEquipment() {
        return equipment;
    }

    public void setEquipment(Equipment equipment) {
        if (equipment != null) {
            equipment.addEvent(this);
        }
        this.equipment = equipment;
    }

    public Procedure getProcedure() {
        return procedure;
    }

    public void setProcedure(Procedure procedure) {
        this.procedure = procedure;
    }

    public Domain getDomain() {
        return domain;
    }

    public void setDomain(Domain domain) {
        this.domain = domain;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Event entityId = (Event) o;
        return id.equals(entityId.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
