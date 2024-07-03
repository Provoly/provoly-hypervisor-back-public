insert into domain (id,name,code) values (1, 'Eclairage Public','EP');
insert into domain (id,name,code) values (2, 'Vidéoprotection et surveillance','VP');
insert into domain (id,name,code) values (3, 'Signalisation Lumineuse Tricolore ','SLT');
insert into domain (id,name,code) values (4, 'Réseaux télécoms','RT');
insert into domain (id,name,code) values (5, 'Patrimoine','PAT');
insert into domain (id,name,code) values (6, 'Stationnement et mobilité','MOB');
insert into domain (id,name,code) values (7, 'Cadre de vie','CDV');

insert into family (id,name,code) values (1,'Armoire','EP_ARMOIRE');
insert into family (id,name,code) values (2,'Foyer Lumineux','EP_FOYER_LUMINEUX');
insert into family (id,name,code) values (3,'Ouvrage','EP_OUVRAGE');

insert into category (id,name,code, parent_id) values (1,'Manifestation','MANIFESTATION',null);
insert into category (id,name,code, parent_id) values (2,'Hors Service','OUTOFORDER',null);
insert into category (id,name,code, parent_id) values (3,'Seuil','LIMIT',null);
insert into category (id,name,code, parent_id) values (4,'Anomalie','ANOMALY',null);
insert into category (id,name,code, parent_id) values (5,'Affluence anormal de personne','UNUSUAL_FLOW',4);
insert into category (id,name,code, parent_id) values (6,'Congestion de vehicule','TRAFFIC_CONGESTION',4);
insert into category (id,name,code, parent_id) values (7,'Dépôt Sauvage','WILD_STORAGE',4);

insert into service_category (id,name,code) values (1,'Préventive','PREV');
insert into service_category (id,name,code) values (2,'Curative','CURA');

insert into custom_action_type (id,name,code) values (1,'Appeler le CSU','CSU');
insert into custom_action_type (id,name,code) values (2,'Levée de doute','DOUTE');

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


insert into city (id,code,name) values (1,'CH', 'Châlons en Champagne');
insert into city (id,code,name) values (2,'CHA', 'Châlons Agglo'); -- TODO: remove
insert into city (id,code,name) values (3,'CONDE', 'Condé sur Marne'); -- TODO: remove
insert into city (id,code,name) values (4,'DAMP', 'Dampierre au Temple'); -- TODO: remove
insert into city (id,code,name) values (5,'FAGN', 'Fagnières');
insert into city (id,code,name) values (6,'HAUSS', 'Haussimont'); -- TODO: remove
insert into city (id,code,name) values (7,'JAL', 'Jâlons'); -- TODO: remove
insert into city (id,code,name) values (8,'MAT', 'Matougues'); -- TODO: remove
insert into city (id,code,name) values (9,'MOUR', 'Mourmelon le Grand');
insert into city (id,code,name) values (10,'SMP', 'Saint Martin-sur-le-Pré');
insert into city (id,code,name) values (11,'SMS', 'Sommesous'); -- TODO: remove
insert into city (id,code,name) values (12,'VRA', 'Vraux'); -- TODO: remove

insert into district (id,code,name) values (1,'CH_C', 'Centre');
insert into district (id,code,name) values (2,'CH_CD', 'Croix Dampierre'); -- TODO: remove
insert into district (id,code,name) values (3,'CH_FSA', 'Faubourg Saint-Antoine - Madagascar');
insert into district (id,code,name) values (4,'CH_LA', 'Langevin - Laforest');-- TODO: remove
insert into district (id,code,name) values (5,'CH_MH', 'Mont Héry');-- TODO: remove
insert into district (id,code,name) values (6,'CH_MSM', 'Mont Saint-Michel - Bidée');
insert into district (id,code,name) values (7,'CH_OFG', 'Oradour - Frison - Gare');
insert into district (id,code,name) values (8,'CH_S', 'Schmit');
insert into district (id,code,name) values (9,'CH_VSP', 'Vallée Saint-Pierre');
insert into district (id,code,name) values (10,'CH_VER', 'Verbeau');
insert into district (id,code,name) values (11,'FAGN', 'Fagnières');
insert into district (id,code,name) values (12,'SMP', 'Saint Martin-sur-le-Pré');
-- TODO: insert new Districts and remove unused city and distrcits
-- TODO: insert city and remove unused city :
/*Baconnes BACONNES
Les Grandes Loges GRD-LOGES
Sarry SARRY
Saint-Memmie STM
Recy RECY
Compertrix COMPERTRIX
Coolus COOLUS*/