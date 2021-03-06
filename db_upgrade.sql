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

alter table print_station alter column id drop default;
drop sequence print_station_id_seq;

alter table position drop column city_id;
drop table city cascade;

alter table position add column address_nation varchar(100);
alter table position add column address_province varchar(100);
alter table position add column address_city varchar(100);
alter table position add column address_district varchar(100);
alter table position add column address_street varchar(100);
update position set address_nation='中国',address_province='北京',address_city='北京',address_district='海淀区',address_street='';

alter table print_station drop column city_id;
alter table print_station add column address_nation varchar(100);
alter table print_station add column address_province varchar(100);
alter table print_station add column address_city varchar(100);
alter table print_station add column address_district varchar(100);
alter table print_station add column address_street varchar(100);
update print_station set address_nation='中国',address_province='北京',address_city='北京',address_district='海淀区',address_street='';

alter table print_station add column name varchar(50);
update print_station set name='';
alter table print_station alter column name set not null;

alter table company_wx_account add column sequence integer;
update company_wx_account set sequence=0;
alter table company_wx_account alter column sequence set not null;

alter table ad_set add column public_resource boolean;
update ad_set set public_resource=true, company_id=1;
alter table ad_set alter column public_resource set not null;

alter table wx_ent_transfer_record_item add column charge integer;
update wx_ent_transfer_record_item set charge=0;
alter table wx_ent_transfer_record_item alter column charge set not null;

alter table print_station_login_session drop column version;

insert into printer_type (name,resolution) values ('CY', 300);
insert into printer_type (name,resolution) values ('EPSON SL-D700', 360);
insert into printer_type (name,resolution) values ('Shinko CHC-S2145', 300);
insert into printer_type (name,resolution) values ('EPSON', 360);

alter table print_order add column page_count integer;
update print_order set page_count = (select sum(copies) from print_order_item where print_order_id=print_order.id);
update print_order set page_count = 0 where page_count is NULL;
alter table print_order alter column page_count set not null;




alter table printer_type add column media_alert_thresholds varchar(100);
update printer_type set media_alert_thresholds='';
update printer_type set media_alert_thresholds='50,25,10' where name='CY';
alter table printer_type alter column media_alert_thresholds set not null;

alter table printer_type add column display_name varchar(100);
update printer_type set display_name = name;
update printer_type set display_name = 'EPSON 桌面打印机' where name='EPSON';
alter table printer_type alter column display_name set not null;

alter table printer_type add column roll_paper boolean;
update printer_type set roll_paper = false;
update printer_type set roll_paper = true where name='EPSON SL-D700';
alter table printer_type alter column roll_paper set not null;

insert into printer_type (name,display_name,resolution,media_alert_thresholds,roll_paper) values ('Fujifilm DX100', 'Fujifilm DX100', 360, '', true);

alter table printer_stat_record add column error_code integer;
update printer_stat_record set error_code = 0;
alter table printer_stat_record alter column error_code set not null;

alter table product add column deleted boolean;
update product set deleted = false;
alter table product alter column deleted set not null;
alter table product drop column enabled;
alter table product drop constraint fkghawd5rtv8ok565nwpdyyuto9;
update product set company_id=0 where company_id=1;

ALTER TABLE print_order ADD COLUMN canceled BOOLEAN DEFAULT FALSE  NOT NULL;
ALTER TABLE print_order ADD  COLUMN print_type INTEGER DEFAULT 0  ;
INSERT INTO product (id, company_id, default_price, name, remark, template_id, sequence, deleted) VALUES (9528, 0, 100, '展会8', '', 1, 9528, false);

alter table template add column deleted boolean default false not null;

alter table printer_type add column default_icc_file_name varchar(100);
insert into printer_type (name,display_name,resolution,media_alert_thresholds,roll_paper) values ('Any', '无限制', 360, '', false);


ALTER TABLE product ADD refined BOOLEAN DEFAULT FALSE  NOT NULL;
COMMENT ON COLUMN product.refined IS '专家精修';

insert into icc_config (printer_model, os_name, icc_file_name) values ('CY', null, 'UNI-CY-WIN-PPG-HOME-b104-1500-20180608-4.icm');
insert into icc_config (printer_model, os_name, icc_file_name) values ('EPSON SL-D700', null, 'UNI-D700-WIN-PDPPG-HOME-b104-1500-20180608-4.icm');

alter table ad_set drop column public_resource;
update ad_set set company_id = 0;
alter table ad_image_file add column enabled boolean default true not null;


alter table template_image_info add column angle_clip DOUBLE default 0.0 not null;
UPDATE template_image_info set angle_clip=0.0;
UPDATE template_image_info set layer_type=2;
UPDATE template_image_info set type=0;


ALTER TABLE address ADD deleted BOOLEAN default false NOT NULL;


alter table company drop column wei_xin_pay_config_id;

alter table company_wx_account add column wx_mp_account_id integer default 1;
alter table wx_mp_account add column mch_id varchar(100) default '' not null;
alter table wx_mp_account add column pay_key varchar(100) default '' not null;

alter table print_station drop column password;

alter table print_order_item add column page_count integer default 0 not null;
alter table print_order add column total_page_count integer default 0 not null;
alter table print_order add column user_name varchar(200) default '' not null;
alter table print_order add column company_name varchar(50) default '' not null;
alter table print_order add column position_id integer default 0 not null;
alter table print_order add column position_name varchar(50) default '' not null;
alter table print_order add column print_station_name varchar(50) default '' not null;
alter table print_order add column product_names varchar(200) default '' not null;
alter table print_order add column transfer_time timestamp default null;
alter table print_order add column transfer_receiver_name varchar(50) default '';
alter table print_order add column transfer_amount integer default 0 not null;
alter table print_order add column transfer_charge integer default 0 not null;

update print_order as p set user_name = COALESCE(u.nick_name, u.full_name, '') from joyspace_user as u where p.user_id = u.id;
update print_order as p set company_name = c.name from company as c where p.company_id = c.id;
update print_order as po set position_name = c.position_name from (select p.id as print_station_id, d.name as position_name from print_station p inner join position d on p.position_id=d.id) as c where po.print_station_id = c.print_station_id;
update print_order as po set position_id = c.position_id from (select p.id as print_station_id, d.id as position_id from print_station p inner join position d on p.position_id=d.id) as c where po.print_station_id = c.print_station_id;
update print_order as po set print_station_name = c.name from print_station as c where po.print_station_id = c.id;
update print_order set print_station_name = concat('自助机', print_station_id) where print_station_name = '';
update print_order_item set page_count = copies;

CREATE TABLE "database_upgrade_record" (
	"name" VARCHAR(255) NOT NULL,
	PRIMARY KEY ("name")
)
;

insert into database_upgrade_record (name) values ('InitPrintOrderNewColumns');



alter table product add column area_price integer default 0 not null;
alter table product add column piece_price integer default 0 not null;

alter table print_order add column image_file_cleared boolean default false not null;

alter table position add column short_name varchar (50) default '' not null;
alter table company add column business_model integer default 0 not null;
alter table print_station add column station_type integer default 0 not null;

