create table equipment_entity
(
    id   bigint primary key,
    name varchar(200) unique not null,
    code varchar(200) unique not null
);

create table family
(
    id   bigint primary key,
    name varchar(200) unique not null,
    code varchar(50) unique  not null
);

create table domain
(
    id   bigint primary key,
    name varchar(200) unique not null,
    code varchar(200) unique not null
);

create table service_category
(
    id   bigint primary key,
    name varchar(100) unique not null,
    code varchar(20) unique  not null
);

create table city
(
    id   bigint primary key,
    name varchar(100) unique not null,
    code varchar(20) unique  not null
);

create table district
(
    id   bigint primary key,
    name varchar(100) unique not null,
    code varchar(20) unique  not null
);

create table equipment
(
    id                  uuid primary key,
    external_id         varchar(200) unique not null,
    name                varchar(200)        not null,
    code                varchar(200) unique not null,
    address             varchar(200)        not null,
    domain_id           bigint              not null references domain,
    family_id           bigint              not null references family,
    city_id             bigint              not null references city,
    district_id         bigint              not null references district,
    equipment_entity_id bigint              not null references equipment_entity,
    attributes          jsonb default '{}',
    parent_id           uuid,
    constraint fk_parent foreign key (parent_id) references equipment (id)
);

create table service
(
    id                     uuid primary key,
    external_id            varchar(50) unique not null,
    equipment_id           uuid               not null references equipment,
    domain_id              bigint             not null references domain,
    description            varchar(200),
    start_date             timestamptz,
    end_date               timestamptz,
    close_date             timestamptz,
    creation_date          timestamptz        not null,
    last_modification_date timestamptz        not null,
    category_id            bigint             not null references service_category,
    status                 varchar(20)
);

create table procedure
(
    id                 serial primary key,
    name               varchar(200) not null,
    description        varchar(200) not null,
    creation_date      timestamptz           default current_timestamp,
    procedure_progress float        not null default 0
);

create table procedure_model
(
    id                     serial primary key,
    name                   varchar(200) unique not null,
    description            varchar(200)        not null,
    creator                varchar(200)        not null,
    creation_date          timestamptz                  default current_timestamp,
    last_modification_date timestamptz                  default current_timestamp,
    use_count              int                 not null default 0,
    domain_id              bigint references domain
);

create table action
(
    id                     uuid primary key,
    procedure_id           int references procedure,
    procedure_model_id     int references procedure_model,
    type                   varchar(100) not null,
    status                 varchar(100) not null,
    name                   varchar(100) not null,
    last_modification_date timestamptz default current_timestamp
        check (procedure_id is not null or procedure_model_id is not null)
);


create table asked_service
(
    id uuid primary key references action
    -- service_id uuid references service ?
);

create table todo_action
(
    id uuid primary key references action
);

create table event
(
    id                     serial primary key,
    name                   varchar(50) unique not null,
    type                   varchar(20)        not null,
    criticality            varchar(50)        not null,
    category               varchar(50)        not null,
    status                 varchar(20) default 'NEW',
    description            varchar(256)       not null,
    address                varchar(256)       not null,
    creation_date          timestamptz default current_timestamp,
    last_modification_date timestamptz default current_timestamp,
    close_date             timestamptz,
    equipment_id           uuid references equipment,
    procedure_id           int references procedure,
    domain_id              bigint references domain
);

create table event_operator
(
    id         serial primary key references event,
    start_date timestamptz,
    end_date   timestamptz
);

create table event_report
(
    id                  serial primary key references event,
    external_source_ref varchar not null
);

create table event_alert
(
    id                  serial primary key references event,
    external_source_ref varchar not null
);