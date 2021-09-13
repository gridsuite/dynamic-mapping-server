
    create table filters (
       filter_id varchar(255) not null,
        rule_id uuid not null,
        operand int4,
        property varchar(255),
        type int4,
        value varchar(255),
        primary key (filter_id, rule_id)
    );

    create table instance_models (
       id varchar(255) not null,
        equipmentType int4,
        modelName varchar(255),
        params_id varchar(255),
        params_type int4,
        primary key (id)
    );

    create table mappings (
       name varchar(255) not null,
        primary key (name)
    );

    create table networks (
       network_id uuid not null,
        iidm_name varchar(255),
        primary key (network_id)
    );

    create table rules (
       rule_id uuid not null,
        composition varchar(255) not null,
        type int4 not null,
        model varchar(255) not null,
        mappingName varchar(255),
        primary key (rule_id)
    );

    create table scripts (
       name varchar(255) not null,
        parent varchar(255),
        script TEXT not null,
        primary key (name)
    );
create index filter_rule_id_index on filters (rule_id);
create index rule_mappingName_index on rules (mappingName);

    alter table if exists filters 
       add constraint rules_filter_fk 
       foreign key (rule_id) 
       references rules;

    alter table if exists rules 
       add constraint mapping_rules_fk 
       foreign key (mappingName) 
       references mappings;
