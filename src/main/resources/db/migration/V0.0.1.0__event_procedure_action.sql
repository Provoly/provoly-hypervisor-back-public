create table equipment_entity
(
    id   bigint primary key,
    name varchar(50) unique not null,
    code varchar(20) unique not null
);

create table family
(
    id   bigint primary key,
    name varchar(50) unique not null,
    code varchar(20) unique not null
);

create table domain
(
    id   bigint primary key,
    name varchar(50) unique not null,
    code varchar(20) unique not null
);

create table category
(
    id        bigint primary key,
    name      varchar(50) unique not null,
    code      varchar(20) unique not null,
    parent_id bigint,
    constraint fk_parent foreign key (parent_id) references category (id)
);

create table service_category
(
    id   bigint primary key,
    name varchar(50) unique not null,
    code varchar(20) unique not null
);

create table custom_action_type
(
    id   bigint primary key,
    name varchar(50) unique not null,
    code varchar(20) unique not null
);

create table city
(
    id   bigint primary key,
    name varchar(50) unique not null,
    code varchar(20) unique not null
);

create table district
(
    id   bigint primary key,
    name varchar(50) unique not null,
    code varchar(20) unique not null
);

create table provoly_user
(
    id        uuid primary key,
    subject   uuid unique  not null,
    username  varchar(100) not null,
    full_name varchar(100) not null
);

create table equipment
(
    id                  uuid primary key,
    name                varchar(50)         not null,
    code                varchar(200) unique not null,
    address             varchar(256)        not null,
    domain_id           bigint              not null references domain,
    family_id           bigint              not null references family,
    city_id             bigint              not null references city,
    district_id         bigint              not null references district,
    equipment_entity_id bigint              not null references equipment_entity,
    attributes          jsonb   default '{}',
    parent_id           uuid,
    deleted             boolean default false,
    constraint fk_parent foreign key (parent_id) references equipment (id)
);

create table equipment_external_id
(
    equipment_id    uuid         not null references equipment,
    external_id     varchar(255),
    external_id_key varchar(255) not null,
    primary key (equipment_id, external_id_key)
);

create table service
(
    id                     uuid primary key,
    external_id            varchar(50) unique not null,
    equipment_id           uuid               not null references equipment,
    domain_id              bigint             not null references domain,
    description            varchar(256),
    start_date             timestamptz,
    end_date               timestamptz,
    close_date             timestamptz,
    creation_date          timestamptz        not null,
    last_modification_date timestamptz        not null,
    category_id            bigint             not null references service_category,
    status                 varchar(20)
);

create table comment
(
    id                     uuid primary key,
    user_id                uuid         not null references provoly_user,
    creation_date          timestamptz default current_timestamp,
    last_modification_date timestamptz default current_timestamp,
    message                varchar(255) not null
);

create table procedure
(
    id                 serial primary key,
    name               varchar(50)  not null,
    description        varchar(256) not null,
    creation_date      timestamptz           default current_timestamp,
    procedure_progress float        not null default 0,
    close_comment_id   uuid references comment
);

create table procedure_model
(
    id                     serial primary key,
    name                   varchar(50) unique not null,
    description            varchar(256)       not null,
    creator                varchar(100)       not null,
    creation_date          timestamptz                 default current_timestamp,
    last_modification_date timestamptz                 default current_timestamp,
    use_count              int                not null default 0,
    domain_id              bigint references domain
);

create table action
(
    id                     uuid primary key,
    type                   varchar(20) not null,
    status                 varchar(20)          default 'NEW',
    action_order           int         not null default 0,
    last_modification_date timestamptz          default current_timestamp
);

create table procedure_action
(
    procedure_id integer not null references procedure,
    actions_id   uuid    not null references action
);

create table procedure_model_action
(
    procedure_model_id integer not null references procedure_model,
    actions_id         uuid    not null references action
);

create table asked_service
(
    id                  uuid primary key references action,
    name                varchar(50) not null,
    service_external_id varchar(50)
);

create table other_action
(
    id   uuid primary key references action,
    name varchar(50) not null
);

create table email_action
(
    id    uuid primary key references action,
    name  varchar(30)  not null,
    email varchar(100) not null
);


create table phone_action
(
    id     uuid primary key references action,
    name   varchar(30) not null,
    number varchar(15) not null
);

create table sms_action
(
    id uuid primary key references phone_action
);

create table event
(
    id                     serial primary key,
    name                   varchar(50)                not null,
    criticality            varchar(50)                not null,
    category_id            bigint references category not null,
    status                 varchar(20) default 'NEW',
    description            varchar(256)               not null,
    address                varchar(256),
    creation_date          timestamptz                not null,
    last_modification_date timestamptz default current_timestamp,
    close_date             timestamptz,
    start_date             timestamptz,
    end_date               timestamptz,
    equipment_id           uuid references equipment,
    procedure_id           int references procedure,
    domain_id              bigint references domain,
    external_source_ref    varchar,
    parent_id              integer,
    constraint fk_parent foreign key (parent_id) references event (id)
);

create table event_comment
(
    event_id    integer not null references event,
    comments_id uuid    not null references comment
);

create table action_comment
(
    action_id   uuid not null references action,
    comments_id uuid not null references comment
);

create table service_type
(
    id     bigint primary key,
    type   varchar not null,
    domain varchar not null,
    gti    integer,
    gtr    integer,
    gtrp   integer
)