## create procedure

```sql
delimiter //
drop procedure if exists request_approver;
create procedure request_approver()
begin
	declare notfound int default 0;
	declare t_error int default 0;
	declare v_flag int default 0;

	declare v_request_id int(11) default 0;
	declare v_trip_id int(11) default 0;
	declare v_persons tinyint(4) default 0;
	declare v_process_status tinyint(4) default 0;
	declare v_filled_id int(11) default 0;
	declare v_num_approved tinyint(4) default 0;
	declare v_capacity tinyint(4) default 0;

	declare cur1 cursor for select * from work_job1;
	declare continue handler for not found set notfound = 1;
	declare continue handler for SQLEXCEPTION set t_error = 1;
	open cur1;
	fetch cur1 into v_request_id, v_trip_id, v_persons, v_process_status, v_filled_id, v_num_approved, v_capacity;
	if notfound = 1 then 
	set v_flag = 0;
	else
	if v_num_approved + v_persons <= v_capacity then
		start transaction;
			update customerrequests set process_status = 8 where request_id = v_request_id;
			update filledcapacity set num_approved = v_persons + v_num_approved where filled_id = v_filled_id;
		if t_error = 1 then
			rollback;
		else
			commit;
		end if;
	else
		start transaction;
			update customerrequests set process_status = 9 where request_id = v_request_id;
		if t_error = 1 then
			rollback;
		else
			commit;
		end if;
	end if;
	end if;
	close cur1;	
end;
//
delimiter ;
```


## create view
```sql
 CREATE VIEW `work_job1` AS 
select 
`TA`.`request_id` AS `request_id`,`TA`.`trip_id` AS `trip_id`,
`TA`.`persons` AS `persons`,`TA`.`process_status` AS `process_status`,
`TB`.`filled_id` AS `filled_id`,`TB`.`num_approved` AS `num_approved`,
`TC`.`capacity` AS `capacity` 
from (
  (`customerrequests` `TA` join `filledcapacity` `TB` on((`TA`.`trip_id` = `TB`.`trip_id`))) 
  join `pubtrips` `TC` on((`TA`.`trip_id` = `TC`.`pubtrip_id`))
) 
where (`TA`.`process_status` = 0) order by `TA`.`request_id` limit 1 
```

## create table
```sql
CREATE TABLE `pubtrips` (
  `pubtrip_id` int(11) NOT NULL AUTO_INCREMENT,
  `publisher_id` int(11) NOT NULL,
  `from_l` int(11) NOT NULL,
  `to_l` int(11) NOT NULL,
  `capacity` tinyint(4) NOT NULL DEFAULT '1',
  `departure_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `created_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `last_updated_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `process_status` tinyint(4) NOT NULL DEFAULT '0',
  PRIMARY KEY (`pubtrip_id`),
  KEY `pubtrips_ibfk_1` (`publisher_id`),
  KEY `pubtrips_ibfk_2` (`from_l`),
  KEY `pubtrips_ibfk_3` (`to_l`),
  CONSTRAINT `pubtrips_ibfk_1` FOREIGN KEY (`publisher_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE,
  CONSTRAINT `pubtrips_ibfk_2` FOREIGN KEY (`from_l`) REFERENCES `locations` (`location_id`) ON DELETE CASCADE,
  CONSTRAINT `pubtrips_ibfk_3` FOREIGN KEY (`to_l`) REFERENCES `locations` (`location_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8
```
