package org.gridsuite.mapping.server.model;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.util.UUID;

import org.gridsuite.mapping.server.utils.*;

@Inheritance
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "filters")
@IdClass(FilterId.class)
public class FilterEntity implements Serializable {
    @Id
    @Column(name = "filter_id")
    private String filterId;

    @Id
    @Column(name = "rule_id")
    private UUID ruleId;

    @Column(name = "property")
    private String property;

    @Column(name = "type")
    @Enumerated
    private PropertyType type;

    @Column(name = "operand")
    @Enumerated
    private Operands operand;

    // Value is the value casted to a string for persistence issue
    @Column(name = "value")
    private String value;

    public FilterEntity(UUID newID, FilterEntity filterEntity) {
        this.filterId = filterEntity.getFilterId();
        this.ruleId = newID;
        this.property = filterEntity.getProperty();
        this.type = filterEntity.getType();
        this.operand = filterEntity.getOperand();
        this.value = filterEntity.getValue();

    }

}
