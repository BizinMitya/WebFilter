--h2 dialect
CREATE TABLE IF NOT EXISTS blacklist
(
  ip varchar2(40) PRIMARY KEY NOT NULL,
  host varchar2(100) NOT NULL
);
CREATE UNIQUE INDEX IF NOT EXISTS blacklist_host_uindex
  ON blacklist (ip);