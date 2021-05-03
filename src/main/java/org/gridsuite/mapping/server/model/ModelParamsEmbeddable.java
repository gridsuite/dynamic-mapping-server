package org.gridsuite.mapping.server.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.gridsuite.mapping.server.utils.ParamsType;

import javax.persistence.*;

@Inheritance
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Embeddable
@AttributeOverrides({
        @AttributeOverride( name = "name", column = @Column(name ="params_id")),
        @AttributeOverride( name = "type", column = @Column(name = "params_type")),
})
public class ModelParamsEmbeddable {

    private String name;

    private ParamsType type;
}
