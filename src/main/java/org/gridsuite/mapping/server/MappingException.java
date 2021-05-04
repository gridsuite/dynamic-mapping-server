package org.gridsuite.mapping.server;

import java.util.Objects;

public class MappingException extends RuntimeException {

    public enum Type {
        MODEL_NOT_FOUND,
        MAPPING_NOT_FOUND
    }

    private final Type type;

    public MappingException(Type type) {
        super(Objects.requireNonNull(type.name()));
        this.type = type;
    }

    Type getType() {
        return type;
    }
}