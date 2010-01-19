CREATE TABLE "asgn" ("aid" INTEGER PRIMARY KEY  AUTOINCREMENT  NOT NULL , "name" VARCHAR NOT NULL );
CREATE TABLE "blacklist" ("bid" INTEGER PRIMARY KEY  AUTOINCREMENT  NOT NULL , "tid" INTEGER NOT NULL , "sid" INTEGER NOT NULL );
CREATE TABLE "distribution" ("did" INTEGER PRIMARY KEY  AUTOINCREMENT  NOT NULL , "sid" INTEGER NOT NULL , "pid" INTEGER NOT NULL , "tid" INTEGER NOT NULL );
CREATE TABLE "exemption" ("xid" INTEGER PRIMARY KEY  NOT NULL , "sid" INTEGER NOT NULL , "pid" INTEGER NOT NULL , "note" TEXT);
CREATE TABLE "extension" ("eid" INTEGER PRIMARY KEY  AUTOINCREMENT  NOT NULL , "sid" INTEGER NOT NULL , "pid" INTEGER NOT NULL , "ontime" DATETIME NOT NULL , "note" TEXT);
CREATE TABLE "grade" ("gid" INTEGER PRIMARY KEY  NOT NULL ,"pid" INTEGER NOT NULL ,"sid" INTEGER NOT NULL ,"score" DOUBLE NOT NULL );
CREATE TABLE "part" ("pid" INTEGER PRIMARY KEY  NOT NULL , "name" VARCHAR NOT NULL , "aid" INTEGER NOT NULL );
CREATE TABLE sqlite_sequence(name,seq);
CREATE TABLE "student" ("sid" INTEGER PRIMARY KEY  NOT NULL ,"login" VARCHAR NOT NULL ,"firstname" VARCHAR NOT NULL ,"lastname" VARCHAR NOT NULL ,"enabled" INTEGER NOT NULL  DEFAULT 1 );
CREATE TABLE "ta" ("tid" INTEGER PRIMARY KEY  NOT NULL ,"login" VARCHAR NOT NULL ,"name" VARCHAR NOT NULL  DEFAULT not_listed );
CREATE INDEX asgn_name ON asgn (name);
CREATE INDEX blacklist_student ON blacklist (sid);
CREATE INDEX blacklist_ta ON blacklist (tid);
CREATE INDEX dist_pid ON distribution (pid);
CREATE INDEX dist_tid ON distribution (tid);
CREATE INDEX exemp_pid ON exemption (pid);
CREATE INDEX exemp_sid ON exemption (sid);
CREATE INDEX exten_pid ON extension (pid);
CREATE INDEX exten_sid ON extension (sid);
CREATE INDEX grade_pid ON grade (pid);
CREATE INDEX grade_sid ON grade (sid);
CREATE INDEX part_aid ON part (aid);
CREATE INDEX part_name ON part (name);
CREATE INDEX student_login ON student (login);
CREATE INDEX ta_login ON ta (login);
