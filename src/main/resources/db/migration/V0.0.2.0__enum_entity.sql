insert into domain (id,name,code) values (1, 'Eclairage Public','EP');
insert into domain (id,name,code) values (2, 'Vidéoprotection et surveillance','VP');
insert into domain (id,name,code) values (3, 'Signalisation Lumineuse Tricolore ','SLT');
insert into domain (id,name,code) values (4, 'Réseaux télécoms','RT');
insert into domain (id,name,code) values (5, 'Patrimoine','PAT');
insert into domain (id,name,code) values (6, 'Stationnement et mobilité','MOB');
insert into domain (id,name,code) values (7, 'Cadre de vie','CDV');


insert into family (id,name,code) values (1,'Armoire','EP_ARMOIRE'); -- TODO: rename code into ARMOIRE-EP
insert into family (id,name,code) values (2,'Foyer Lumineux','EP_FOYER_LUMINEUX'); -- TODO: rename code into FOYER-LUMINEUX
insert into family (id,name,code) values (3,'Ouvrage','EP_OUVRAGE'); -- TODO -- rename code into OUVRAGE-EP

insert into service_category (id,name,code) values (1,'Préventive','PREV');
insert into service_category (id,name,code) values (2,'Curative','CURA');

insert into equipment_entity (id,code,name) values (1,'COMMUN', 'COMMUN');
insert into equipment_entity (id,code,name) values (2,'CHALONS_COMMUN', 'CHALONS_COMMUN');
insert into equipment_entity (id,code,name) values (3,'AGGLO_COMMUN', 'AGGLO_COMMUN');
insert into equipment_entity (id,code,name) values (4,'FAGNIERES_COMMUN', 'FAGNIERES_COMMUN');
insert into equipment_entity (id,code,name) values (5,'SAINT_MARTIN_COMMUN', 'SAINT_MARTIN_COMMUN');
insert into equipment_entity (id,code,name) values (6,'CHALONS_EP', 'CHALONS_EP');
insert into equipment_entity (id,code,name) values (7,'CHALONS_SLT', 'CHALONS_SLT');
insert into equipment_entity (id,code,name) values (8,'CHALONS_VP', 'CHALONS_VP');
insert into equipment_entity (id,code,name) values (9,'CHALONS_RT', 'CHALONS_RT');
insert into equipment_entity (id,code,name) values (10,'CHALONS_MOB', 'CHALONS_MOB');
insert into equipment_entity (id,code,name) values (11,'CHALONS_PAT', 'CHALONS_PAT');
insert into equipment_entity (id,code,name) values (12,'CHALONS_CDV', 'CHALONS_CDV');
insert into equipment_entity (id,code,name) values (13,'AGGLO_EP', 'AGGLO_EP');
insert into equipment_entity (id,code,name) values (14,'AGGLO_SLT', 'AGGLO_SLT');
insert into equipment_entity (id,code,name) values (15,'AGGLO_RT', 'AGGLO_RT');
insert into equipment_entity (id,code,name) values (16,'AGGLO_MOB', 'AGGLO_MOB');
insert into equipment_entity (id,code,name) values (17,'AGGLO_PAT', 'AGGLO_PAT');
insert into equipment_entity (id,code,name) values (18,'GE_BYES_EP', 'GE_BYES_EP');
insert into equipment_entity (id,code,name) values (19,'GE_BYES_VP', 'GE_BYES_VP');
insert into equipment_entity (id,code,name) values (20,'GE_BYES_RT', 'GE_BYES_RT');
insert into equipment_entity (id,code,name) values (21,'GE_BYES_COMMUN', 'GE_BYES_COMMUN');
insert into equipment_entity (id,code,name) values (22,'GE_AXIMUM_SLT', 'GE_AXIMUM_SLT');