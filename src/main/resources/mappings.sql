CREATE TABLE scriptq (
    name varchar(255),
    parent varchar(255),
    script text,
    PRIMARY KEY (name)
);

CREATE TABLE scripts (
    name varchar(255),
    PRIMARY KEY (name)
);

CREATE TABLE rules (
    rule_id uuid,
    mappingName varchar(255),
    type int4,
    model varchar(255),
    composition varchar(255),
    PRIMARY KEY (rule_id)
);


CREATE TABLE filters (
    filter_id varchar(255),
    rule_id uuid,
    property varchar(255),
    type int4,
    operand int4,
    value varchar(255),
    PRIMARY KEY (filter_id,rule_id)
);

CREATE TABLE instance_models (
    id varchar(255),
    modelName varchar(255),
    equipmentType int4,
    params_id varchar(255),
    params_type int4,
    PRIMARY KEY (id)
);
