create table equipment_entity
(
    id   bigint primary key,
    name varchar(200) unique not null
);

create table family
(
    id   bigint primary key,
    name varchar(200) unique not null,
    code varchar(10) unique  not null
);

create table domain
(
    id   bigint primary key,
    name varchar(200) unique not null
);

create table equipment
(
    id                  uuid primary key,
    external_id         varchar(200) unique not null,
    name                varchar(200)        not null,
    code                varchar(200) unique not null,
    domain_id           bigint              not null references domain,
    family_id           bigint              not null references family,
    equipment_entity_id bigint              not null references equipment_entity,
    attributes          jsonb default '{}',
    parent_id           uuid,
    constraint fk_parent foreign key (parent_id) references equipment (id)
);

create table procedure
(
    id                 uuid primary key,
    name               varchar(200) unique not null,
    creation_date      timestamptz                  default current_timestamp,
    procedure_progress float               not null default 0
);

create table action
(
    id                     uuid primary key,
    procedure_id           uuid references procedure,
    type                   varchar(100) not null,
    status                 varchar(100) not null,
    name                   varchar(100) not null,
    last_modification_date timestamptz default current_timestamp
);


create table service
(
    id           uuid primary key references action,
    equipment_id uuid references equipment
);

create table todo_action
(
    id uuid primary key references action
);

create table event
(
    id                     uuid primary key,
    name                   varchar(50) unique not null,
    type                   varchar(20)        not null,
    criticality            varchar(50)        not null,
    status                 varchar(20) default 'NEW',
    description            varchar(256)       not null,
    address                varchar(256)       not null,
    creation_date          timestamptz default current_timestamp,
    last_modification_date timestamptz default current_timestamp,
    close_date             timestamptz,
    equipment_id           uuid references equipment,
    procedure_id           uuid references procedure,
    domain_id              bigint references domain
);

create table event_operator
(
    id         uuid primary key references event,
    category   varchar(20) not null check (category in ('MANIFESTATION', 'OPERATOR_EVENT')),
    start_date timestamptz,
    end_date   timestamptz
);

create table event_report
(
    id                  uuid primary key references event,
    category            varchar(50) not null check (category in ('REPORT')),
    external_source_ref varchar     not null
);

create table event_alert
(
    id                  uuid primary key references event,
    category            varchar(50) not null check (category in ('ALERT_LIMIT', 'ALERT_MALFUNCTION')),
    external_source_ref varchar     not null
);