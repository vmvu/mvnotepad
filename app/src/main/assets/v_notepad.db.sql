BEGIN TRANSACTION;
CREATE TABLE IF NOT EXISTS `v_typeoftext` (
	`_id`	INTEGER PRIMARY KEY AUTOINCREMENT,
	`name`	TEXT
);
INSERT INTO `v_typeoftext` (_id,name) VALUES (1,'text');
INSERT INTO `v_typeoftext` (_id,name) VALUES (2,'checklist');
CREATE TABLE IF NOT EXISTS `v_note` (
	`_id`	INTEGER PRIMARY KEY AUTOINCREMENT,
	`keySync`	TEXT,
	`title`	TEXT,
	`content`	TEXT,
	`date_created`	TEXT,
	`last_on`	TEXT,
	`pass`	TEXT,
	`pass_key`	TEXT,
	`id_color`	INTEGER,
	`id_typeoftext`	INTEGER,
	`isdelete`	INTEGER DEFAULT 0
);
CREATE TABLE IF NOT EXISTS `v_images` (
	`name_path`	TEXT,
	`note_id`	INTEGER,
	`sync`	INTEGER DEFAULT 0,
	PRIMARY KEY(`name_path`)
);
CREATE TABLE IF NOT EXISTS `v_account` (
	`id_account`	TEXT,
	PRIMARY KEY(`id_account`)
);
CREATE TABLE IF NOT EXISTS `note_ready_deleted` (
	`keySync`	TEXT
);
COMMIT;
