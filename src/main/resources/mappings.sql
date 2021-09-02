
    create table automata (
       automaton_id uuid not null,
        type int4 not null,
        model varchar(255) not null,
        set_group varchar(255) not null,
        watched_element varchar(255) not null,
        mappingName varchar(255),
        primary key (automaton_id)
    );

    create table automaton_properties (
       automaton_id uuid not null,
        name varchar(255) not null,
        type int4,
        value varchar(255),
        primary key (automaton_id, name)
    );

    create table filters (
       filter_id varchar(255) not null,
        rule_id uuid not null,
        operand int4,
        property varchar(255),
        type int4,
        value varchar(255),
        primary key (filter_id, rule_id)
    );

    create table mappings (
       name varchar(255) not null,
        control_parameters boolean not null,
        primary key (name)
    );

    create table model_parameter_definitions (
       model_name varchar(255) not null,
        name varchar(255) not null,
        fixed_value varchar(255),
        origin int4,
        origin_name varchar(255),
        type int4,
        primary key (model_name, name)
    );

    create table model_parameter_sets (
       group_name varchar(255) not null,
        model_name varchar(255) not null,
        name varchar(255) not null,
        last_modified_date timestamp,
        primary key (group_name, model_name, name)
    );

    create table model_parameters (
       model_name varchar(255) not null,
        name varchar(255) not null,
        set_name varchar(255) not null,
        group_name varchar(255),
        value varchar(255),
        primary key (model_name, name, set_name)
    );

    create table model_sets_group (
       model_name varchar(255) not null,
        name varchar(255) not null,
        type int4,
        primary key (model_name, name)
    );

    create table models (
       model_name varchar(255) not null,
        equipment_type int4,
        primary key (model_name)
    );

    create table rules (
       rule_id uuid not null,
        composition varchar(255) not null,
        type int4 not null,
        model varchar(255) not null,
        set_group varchar(255) not null,
        mappingName varchar(255),
        primary key (rule_id)
    );

    create table scripts (
       name varchar(255) not null,
        created_date timestamp,
        parameters_file TEXT,
        parent varchar(255),
        script TEXT not null,
        primary key (name)
    );
create index automaton_mappingName_index on automata (mappingName);
create index property_automaton_id_index on automaton_properties (automaton_id);
create index filter_rule_id_index on filters (rule_id);
create index model_parameter_definitions_model_name_index on model_parameter_definitions (model_name);
create index model_parameter_sets_group_name_index on model_parameter_sets (group_name);
create index model_parameter_set_index on model_parameters (set_name);
create index model_sets_group_model_name_index on model_sets_group (model_name);
create index rule_mappingName_index on rules (mappingName);

    alter table if exists automata 
       add constraint mapping_automata_fk 
       foreign key (mappingName) 
       references mappings;

    alter table if exists automaton_properties 
       add constraint automata_property_fk 
       foreign key (automaton_id) 
       references automata;

    alter table if exists filters 
       add constraint rules_filter_fk 
       foreign key (rule_id) 
       references rules;

    alter table if exists model_parameter_definitions 
       add constraint model_parameter_definition_fk 
       foreign key (model_name) 
       references models;

    alter table if exists model_parameter_sets 
       add constraint model_parameter_sets_fk 
       foreign key (model_name, group_name) 
       references model_sets_group;

    alter table if exists model_parameters 
       add constraint parameter_set_fk 
       foreign key (group_name, model_name, set_name) 
       references model_parameter_sets;

    alter table if exists model_sets_group 
       add constraint model_sets_groups_fk 
       foreign key (model_name) 
       references models;

    alter table if exists rules 
       add constraint mapping_rules_fk 
       foreign key (mappingName) 
       references mappings;
