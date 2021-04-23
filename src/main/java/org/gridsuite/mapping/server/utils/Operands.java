package org.gridsuite.mapping.server.utils;

public enum Operands {
    // Common
    EQUALS,
    NOT_EQUALS,
    // Number
    LOWER,
    LOWER_OR_EQUALS,
    HIGHER_OR_EQUALS,
    HIGHER,
    // String
    INCLUDES,
    STARTS_WITH,
    ENDS_WITH,
    // Enum
    IN,
    NOT_IN
}
