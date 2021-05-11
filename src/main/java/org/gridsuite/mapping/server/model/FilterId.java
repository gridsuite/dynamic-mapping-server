package org.gridsuite.mapping.server.model;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
public class FilterId implements Serializable {

    private String filterId;
    private UUID ruleId;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FilterId filterIdClass = (FilterId) o;
        return filterId.equals(filterIdClass.filterId) &&
                ruleId.equals(filterIdClass.ruleId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(filterId, ruleId);
    }

}
