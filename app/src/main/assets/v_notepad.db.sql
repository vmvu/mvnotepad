BEGIN TRANSACTION;
CREATE TABLE IF NOT EXISTS `v_typeoftext` (
	`_id`	INTEGER PRIMARY KEY AUTOINCREMENT,
	`name`	TEXT
);
INSERT INTO `v_typeoftext` (_id,name) VALUES (1,'text');
INSERT INTO `v_typeoftext` (_id,name) VALUES (2,'checklist');
CREATE TABLE IF NOT EXISTS `v_note` (
	`_id`	INTEGER PRIMARY KEY AUTOINCREMENT,
	`title`	TEXT,
	`content`	TEXT,
	`date_created`	TEXT,
	`last_on`	TEXT,
	`pass`	TEXT,
	`pass_key`	TEXT,
	`id_color`	INTEGER,
	`id_typeoftext`	INTEGER,
	`account`	TEXT,
	`isdelete`	INTEGER DEFAULT 0
);
CREATE TABLE IF NOT EXISTS `v_images` (
	`name_path`	TEXT,
	`note_id`	INTEGER,
	PRIMARY KEY(`name_path`)
);
CREATE TABLE IF NOT EXISTS `v_account` (
	`id_account`	TEXT,
	PRIMARY KEY(`id_account`)
);
COMMIT;
