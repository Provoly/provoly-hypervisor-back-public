package com.provoly.equipment;

import java.util.Objects;

import jakarta.persistence.Entity;

import com.provoly.EnumEntity;

@Entity
public class EquipmentEntity extends EnumEntity {

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        EquipmentEntity entity = (EquipmentEntity) o;
        return name.equals(entity.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return this.name;
    }

}
