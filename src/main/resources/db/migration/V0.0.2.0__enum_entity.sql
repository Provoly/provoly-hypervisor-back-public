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
insert into family (id,name,code) values (4,'Camera','VP_CAM');

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

insert into equipment_entity (id,code,name) values (1,'AGGLO-COMMUN', 'Châlons Agglomération');
insert into equipment_entity (id,code,name) values (2,'CHALONS-COMMUN', 'Châlons-en-Champagne');
insert into equipment_entity (id,code,name) values (3,'FAGNIERES-COMMUN', 'Fagnières');
insert into equipment_entity (id,code,name) values (4,'SAINT-MARTIN-COMMUN', 'St-Martin-sur-le-Pré');

insert into city (id,code,name) values (1,'CH', 'Châlons en Champagne');
insert into city (id,code,name) values (2,'FAGN', 'Fagnières');
insert into city (id,code,name) values (3,'SMP', 'Saint Martin-sur-le-Pré');
insert into city (id,code,name) values (4,'CONDE', 'Condé');
insert into city (id,code,name) values (5,'MATOU', 'Matougues');
insert into city (id,code,name) values (6,'MOUR', 'Mourmelon le Grand');
insert into city (id,code,name) values (7,'BACONNES', 'Baconnes');
insert into city (id,code,name) values (8,'GRD-LOGES', 'Les Grandes Loges');
insert into city (id,code,name) values (9,'SARRY', 'Sarry');
insert into city (id,code,name) values (10,'STM', 'Saint-Memmie');
insert into city (id,code,name) values (11,'RECY', 'Recy');
insert into city (id,code,name) values (12,'COMPERTRIX', 'Compertrix');
insert into city (id,code,name) values (13,'COOLUS', 'Coolus');

insert into district (id,code,name) values (1,'CH_C', 'Centre');
insert into district (id,code,name) values (2,'CH_CD', 'Croix Dampierre');
insert into district (id,code,name) values (3,'CH_FSA', 'Faubourg Saint-Antoine - Madagascar');
insert into district (id,code,name) values (4,'CH_LA', 'Langevin - Laforest');
insert into district (id,code,name) values (5,'CH_MH', 'Mont Héry');
insert into district (id,code,name) values (6,'CH_MSM', 'Mont Saint-Michel - Bidée');
insert into district (id,code,name) values (7,'CH_OFG', 'Oradour - Frison - Gare');
insert into district (id,code,name) values (8,'CH_S', 'Schmit');
insert into district (id,code,name) values (9,'CH_VSP', 'Vallée Saint-Pierre');
insert into district (id,code,name) values (10,'CH_VER', 'Verbeau');
insert into district (id,code,name) values (11,'FAGN', 'Fagnières');
insert into district (id,code,name) values (12,'SMP', 'Saint Martin-sur-le-Pré');
insert into district (id,code,name) values (13,'CONDE', 'Condé');
insert into district (id,code,name) values (14,'MATOU', 'Matougues');
insert into district (id,code,name) values (15,'MOUR', 'Mourmelon le Grand');
insert into district (id,code,name) values (16,'BACONNES', 'Baconnes');
insert into district (id,code,name) values (17,'GRD-LOGES', 'Les Grandes Loges');
insert into district (id,code,name) values (18,'SARRY', 'Sarry');
insert into district (id,code,name) values (19,'STM', 'Saint-Memmie');
insert into district (id,code,name) values (20,'RECY', 'Recy');
insert into district (id,code,name) values (21,'COMPERTRIX', 'Compertrix');
insert into district (id,code,name) values (22,'COOLUS', 'Coolus');