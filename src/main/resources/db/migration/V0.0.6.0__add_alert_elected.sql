create table alert_elected
(
    id                  uuid primary key references action,
    name                varchar(50) not null,
    service_external_id varchar(50)
);
