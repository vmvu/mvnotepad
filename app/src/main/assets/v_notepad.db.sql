BEGIN TRANSACTION;
CREATE TABLE IF NOT EXISTS `text_style` (
	`_id`	INTEGER PRIMARY KEY AUTOINCREMENT,
	`name`	TEXT
);
INSERT INTO `text_style` (_id,name) VALUES (1,'text');
INSERT INTO `text_style` (_id,name) VALUES (2,'checklist');
CREATE TABLE IF NOT EXISTS `note` (
	`_id`	INTEGER PRIMARY KEY AUTOINCREMENT,
	`title`	TEXT,
	`content`	TEXT,
	`creation_time`	TEXT,
	`last_edit_time`	TEXT,
	`pass`	TEXT,
	`pass_salt`	TEXT,
	`id_color`	INTEGER,
	`id_text_style`	INTEGER,
	`is_delete`	INTEGER DEFAULT 0,
	`synch_keys`	TEXT
);
CREATE TABLE IF NOT EXISTS `images` (
	`path`	TEXT primary KEY,
	`id_note`	INTEGER,
	`sync_state`	INTEGER
);
CREATE TABLE IF NOT EXISTS `note_ready_deleted` (
	`synch_keys`	TEXT,
	`id_note`	INTEGER
);

CREATE TABLE IF NOT EXISTS `last_sync`(
    `long_time`  INTEGER
);
COMMIT;
