package org.moment.monroe;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Location {
    private String id;
    private String bulkId;

    @Override
    public String toString(){
        return String.format("{\"id\":\"%s\",\"bulkId\":\"%s\"}", this.id,this.bulkId);
    }

    @JsonIgnore
    public boolean isValid(){
        return this.id!=null;
    }
}
