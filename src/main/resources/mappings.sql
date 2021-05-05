CREATE TABLE script_mappings (
    id varchar(255),
    name varchar(255),
    script text,
    PRIMARY KEY (id)
);

CREATE TABLE rules_mappings (
    name varchar(255),
    PRIMARY KEY (name)
);

CREATE TABLE rules (
    rule_id varchar(255),
    mappingName varchar(255),
    equipmentType varchar(255),
    mappedModel varchar(255),
    composition varchar(255),
    property varchar(255),
    operand varchar(255),
    value varchar(255),
    PRIMARY KEY (mappingName, rule_id)
);


CREATE TABLE filters (
    filter_id varchar(255),
    rule_id varchar(255),
    property varchar(255),
    type varchar(255),
    operand varchar(255),
    value varchar(255),
    PRIMARY KEY (filter_id,rule_id)
);

CREATE TABLE instance_models (
    id varchar(255),
    modelName varchar(255),
    equipmentType varchar(255),
    params_id varchar(255),
    params_type varchar(255),
    PRIMARY KEY (id)
);
