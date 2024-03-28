/* Insert procedures */

insert into procedure (id, name, procedure_progress)
values ('f7b37e0e-a3d1-4159-a2c1-f6b7fd1f7a82', 'procedure_1', 100);

insert into procedure (id, name)
values ('5c925baf-7164-4bfe-af37-f0860aacc570', 'procedure maintenance');

insert into procedure (id, name)
values ('585d17af-e7d5-42e1-9052-fabadd23ff42', 'procedure_festival');

/* Insert Actions */

insert into action (id, procedure_id, type, status, name)
values ('313f22b0-bcab-4760-be67-2111734e9d96', 'f7b37e0e-a3d1-4159-a2c1-f6b7fd1f7a82', 'SERVICE',
        'DONE', 'intervention procedure_1');
insert into service(id)
values ('313f22b0-bcab-4760-be67-2111734e9d96');


insert into action (id, procedure_id, type, status, name, last_modification_date)
values ('6b5ca865-87e5-467b-a996-4c0629e8f94c', '5c925baf-7164-4bfe-af37-f0860aacc570', 'SERVICE',
        'IN_PROGRESS', 'intervention procedure_2', '2024-02-20T11:12:39.375184Z');
insert into service(id)
values ('6b5ca865-87e5-467b-a996-4c0629e8f94c');

insert into action (id, procedure_id, type, status, name, last_modification_date)
values ('a08e2110-3a9c-4f77-bf65-31150201bbfa', '5c925baf-7164-4bfe-af37-f0860aacc570', 'SERVICE',
        'NEW', 'intervention procedure_2 bis', '2024-02-28T11:12:39.375184Z');
insert into service(id)
values ('a08e2110-3a9c-4f77-bf65-31150201bbfa');

insert into action (id, procedure_id, type, status, name, last_modification_date)
values ('93e66ff4-ebea-42ab-89e8-ac3a2f9e87f7', '585d17af-e7d5-42e1-9052-fabadd23ff42', 'SERVICE',
        'NEW', 'intervention procedure_festival', '2024-02-25T11:12:39.375184Z');
insert into service(id)
values ('93e66ff4-ebea-42ab-89e8-ac3a2f9e87f7');

/* Insert Equipments */

insert into equipment (id, name, family_id, equipment_entity_id)
values ('0c728960-d5fd-49ea-8fe8-f72cb9cfcccd', 'P-1000', 2, 4);

insert into equipment (id, name, family_id, equipment_entity_id)
values ('4ead3c85-f120-4144-8ca2-29ac9882ff5e', 'A-230', 1, 5);

insert into equipment (id, name, family_id, equipment_entity_id)
values ('4ead3c85-f120-4144-8ca2-29ac9882ff5f', 'A-4901', 1, 3);

insert into equipment (id, name, family_id, equipment_entity_id)
values ('12a64ae7-70ca-4d12-bcd9-9f1c75f23915', 'C-1034', 3, 3);

insert into equipment(id, name, family_id, equipment_entity_id)
values ('12a64ae7-70ca-4d12-bcd9-9f1c75f23916', 'C-761', 3, 2);

-- /* Insert Events */

insert into event (id, name, type, criticality, status, description, address, equipment_id, procedure_id, domain_id)
values ('8076a2ce-bee8-40f9-a993-862c755cd357', 'Signalement encombrant', 'REPORT', 'LOW', 'NEW', 'description',
        '16 Rue des Martyrs de la Résistance, 51000 Châlons-en-Champagne', null, null, 2);
insert into event_report (id, category, external_source_ref)
values ('8076a2ce-bee8-40f9-a993-862c755cd357', 'REPORT', 'external_source1');


insert into event (id, name, type, criticality, status, description, address, equipment_id, procedure_id, creation_date,
                   last_modification_date)
values ('b0d1a93c-ec57-401c-9a74-2cc364bbab9f', 'Festival Chalons', 'OPERATOR', 'MEDIUM',
        'IN_PROGRESS', 'description', '28 E Rue du Général Fery, 51000 Châlons-en-Champagne',
        '4ead3c85-f120-4144-8ca2-29ac9882ff5e',
        '585d17af-e7d5-42e1-9052-fabadd23ff42', '2024-01-20T11:12:39.375184Z', '2024-02-20T11:12:39.375184Z');
insert into event_operator (id, category, start_date, end_date)
values ('b0d1a93c-ec57-401c-9a74-2cc364bbab9f', 'MANIFESTATION', '2008-01-03 00:00:00 ', '2008-01-04 00:00:00');


insert into event (id, name, type, criticality, status, description, address, equipment_id, procedure_id, creation_date,
                   last_modification_date)
values ('ebbfbbd1-b8de-467c-8a9b-88443a49f807', 'Accident voiture', 'REPORT', 'HIGH', 'IN_PROGRESS', 'description',
        '10 Rue du Camp d''Attila, 51000 Châlons-en-Champagne',
        '4ead3c85-f120-4144-8ca2-29ac9882ff5f', 'f7b37e0e-a3d1-4159-a2c1-f6b7fd1f7a82', '2024-01-21T11:12:39.375184Z',
        '2024-02-21T11:12:39.375184Z');
insert into event_report (id, category, external_source_ref)
values ('ebbfbbd1-b8de-467c-8a9b-88443a49f807', 'REPORT', 'external_source2');


insert into event (id, name, type, criticality, status, description, address, equipment_id, procedure_id, creation_date,
                   last_modification_date, close_date)
values ('01ffde9d-30d2-4273-b61f-c6addd8747c8', 'Defaillance lampe', 'ALERT', 'LOW', 'DONE', 'description',
        '112 Av. de Paris, 51000 Châlons-en-Champagne',
        '12a64ae7-70ca-4d12-bcd9-9f1c75f23915', '5c925baf-7164-4bfe-af37-f0860aacc570', '2024-01-22T11:12:39.375184Z',
        '2024-02-22T11:12:39.375184Z', '2024-02-22T11:12:39.375184Z');
insert into event_alert (id, category, external_source_ref)
values ('01ffde9d-30d2-4273-b61f-c6addd8747c8', 'ALERT_MALFUNCTION', 'citylinx_1');

insert into event (id, name, type, criticality, status, description, address, equipment_id, procedure_id, creation_date,
                   last_modification_date, close_date)
values ('eb8e2f6b-f33e-408c-bb6c-e042f204d687', 'Maintenance ouvrage2', 'REPORT', 'MEDIUM', 'DONE',
        'description', '63 Rue de Fagnières, 51000 Châlons-en-Champagne', '12a64ae7-70ca-4d12-bcd9-9f1c75f23916',
        '5c925baf-7164-4bfe-af37-f0860aacc570',
        '2024-01-23T11:12:39.375184Z', '2024-02-23T11:12:39.375184Z', '2024-02-23T11:12:39.375184Z');
insert into event_report (id, category, external_source_ref)
values ('eb8e2f6b-f33e-408c-bb6c-e042f204d687', 'REPORT', 'external_source3');

insert into event (id, name, type, criticality, status, description, address, equipment_id, procedure_id, creation_date,
                   last_modification_date)
values ('9f15215e-94bd-4056-89e6-1e69f0691419', 'Surconsommation ouvrage2', 'ALERT', 'LOW', 'NEW',
        'description', '46 Av. Daniel Simonnot, 51000 Châlons-en-Champagne', '12a64ae7-70ca-4d12-bcd9-9f1c75f23916',
        null, '2024-01-24T11:12:39.375184Z',
        '2024-02-24T11:12:39.375184Z');
insert into event_alert (id, category, external_source_ref)
values ('9f15215e-94bd-4056-89e6-1e69f0691419', 'ALERT_LIMIT', 'citylinx_2');
