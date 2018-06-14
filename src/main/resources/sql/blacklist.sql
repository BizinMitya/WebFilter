--h2 dialect
CREATE TABLE IF NOT EXISTS blacklist
(
  host varchar2(40) PRIMARY KEY NOT NULL
);
CREATE UNIQUE INDEX IF NOT EXISTS blacklist_host_uindex
  ON blacklist (host);