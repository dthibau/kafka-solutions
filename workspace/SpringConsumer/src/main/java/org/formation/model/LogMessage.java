package org.formation.model;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class LogMessage {
    @Id
    String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
