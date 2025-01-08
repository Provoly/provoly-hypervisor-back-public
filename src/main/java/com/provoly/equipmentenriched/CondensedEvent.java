package com.provoly.equipmentenriched;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.provoly.event.Criticality;

public record CondensedEvent(String category, Criticality criticality) {

    @JsonCreator
    public CondensedEvent(String category, Criticality criticality) {
      this.category = category;
      this.criticality = criticality;
    }

}
