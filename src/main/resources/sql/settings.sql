--h2 dialect
CREATE TABLE IF NOT EXISTS settings
(
  `key` varchar2(40) PRIMARY KEY NOT NULL,
  value varchar2(40)             NOT NULL
);
CREATE UNIQUE INDEX IF NOT EXISTS settings_key_uindex
  ON settings (`key`);