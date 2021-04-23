CREATE TABLE mappings.script_mappings (
    id text,
    name text,
    script text,
    PRIMARY KEY (id)
);

CREATE TABLE mappings.rules_mappings (
    mappingId text,
    name text,
    rules set<text>,
    PRIMARY KEY (id)
);

CREATE TABLE mappings.rules (
    ruleId text,
    mappingId text,
    equipmentType text,
    mappedModel text,
    composition text,
    filterId text,
    property text,
    operand text,
    value text,
    PRIMARY KEY ((mappingId, ruleId), filterId)
)
