alter table print_order add column transfered boolean;
update print_order set transfered=true;
alter table print_order alter column transfered set not null;

alter table print_station add column transfer_proportion integer;
update print_station set transfer_proportion=1000;
alter table print_station alter column transfer_proportion set not null;

alter table print_order add column transfer_proportion integer;
update print_order set transfer_proportion=1000;
alter table print_order alter column transfer_proportion set not null;

update ad_set set company_id=0 where company_id=1;

ALTER TABLE print_station ALTER COLUMN id DROP DEFAULT;
drop sequence print_station_id_seq;