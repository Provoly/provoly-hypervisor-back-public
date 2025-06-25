ALTER TABLE custom_action_type ALTER COLUMN name TYPE VARCHAR(80);
update custom_action_type SET name='Demander une levée de doute au CSU', code='DOUTE_CSU' WHERE id=1;
update custom_action_type SET name='Demander une levée de doute au Patrouilleur', code='DOUTE_PAT' WHERE id=2;
insert into custom_action_type (id,name,code) values (3,'Appeler le métier/police pour sécurisation','PHONE');
insert into custom_action_type (id,name,code) values (4,'Informer le métier par téléphone/sms (personne/service)','SMS');
insert into custom_action_type (id,name,code) values (5,'Informer le métier par mail (personne/service)','EMAIL');
insert into custom_action_type (id,name,code) values (6,'Vérifier l''état d''un équipement connexe','CHECK_EQUIPEMENT');
insert into custom_action_type (id,name,code) values (7,'Vérifier hyperviseur/GMAO si événement en cours/associé','CHECK_EVENT');
insert into custom_action_type (id,name,code) values (8,'Créer un ticket DSI (vérification fonctionnement réseau/logiciel)','CREATE_TICKET');
insert into custom_action_type (id,name,code) values (9,'Faire un point de situation (relance, fait nouveau)','TAKE_STOCK');
insert into custom_action_type (id,name,code) values (10,'Créer une demande d’intervention GMAO','ASKED_SERVICE');
insert into custom_action_type (id,name,code) values (11,'Alerter Élus/Direction','ALERT_ELECTED');
insert into custom_action_type (id,name,code) values (12,'Autre','OTHER');
insert into custom_action_type (id,name,code) values (13,'Clôturer l''événement','CLOSE_EVENT');

