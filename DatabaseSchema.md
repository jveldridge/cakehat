student
  * sid INTEGER PRIMARY KEY AUTOINCREMENT
  * login VARCHAR NOT NULL
  * firstname VARCHAR NOT NULL
  * lastname VARCHAR NOT NULL
  * email VARCHAR NOT NULL
  * enabled INTEGER NOT NULL DEFAULT 1
  * hascollab INTEGER NOT NULL DEFAULT 0
  * CONSTRAINT loginunique UNIQUE (login) ON CONFLICT ROLLBACK

ta
  * tid INTEGER PRIMARY KEY NOT NULL //will be POSIX ID
  * login VARCHAR NOT NULL
  * firstname VARCHAR NOT NULL
  * lastname VARCHAR NOT NULL
  * admin INTEGER NOT NULL DEFAULT 0
  * defaultgrader INTEGER NOT NULL DEFAULT 0
  * CONSTRAINT tidunique UNIQUE (tid) ON CONFLICT ROLLBACK
  * CONSTRAINT loginunique UNIQUE (login) ON CONFLICT ROLLBACK

blacklist
  * sid INTEGER NOT NULL
  * tid INTEGER NOT NULL
  * FOREIGN KEY (sid) REFERENCES student(sid) ON DELETE CASCADE
  * FOREIGN KEY (tid) REFERENCES ta(tid) ON DELETE CASCADE
  * CONSTRAINT sidtidunique UNIQUE (sid, tid) ON CONFLICT IGNORE

assignment
  * aid INTEGER PRIMARY KEY AUTOINCREMENT
  * name VARCHAR NOT NULL
  * ordering INTEGER NOT NULL
  * hasgroups INTEGER NOT NULL DEFAULT 0

gradableevent
  * geid INTEGER PRIMARY KEY AUTOINCREMENT
  * aid INTEGER NOT NULL
  * name VARCHAR NOT NULL
  * ordering INTEGER NOT NULL
  * directory VARCHAR
  * deadlinetype VARCHAR
  * earlydate VARCHAR
  * earlypoints DOUBLE
  * ontimedate VARCHAR
  * latedate VARCHAR
  * latepoints DOUBLE
  * lateperiod VARCHAR
  * FOREIGN KEY (aid) REFERENCES assignment(aid) ON DELETE CASCADE

part
  * pid INTEGER PRIMARY KEY AUTOINCREMENT
  * geid INTEGER NOT NULL
  * name VARCHAR NOT NULL
  * ordering INTEGER NOT NULL
  * gmltemplate VARCHAR
  * outof DOUBLE
  * quickname VARCHAR
  * FOREIGN KEY (geid) REFERENCES gradableevent(geid) ON DELETE CASCADE
  * CONSTRAINT quicknameunique UNIQUE (quickname) ON CONFLICT ROLLBACK

inclusionfilter
  * ifid INTEGER PRIMARY KEY AUTOINCREMENT
  * pid INTEGER NOT NULL
  * type VARCHAR NOT NULL
  * path VARCHAR NOT NULL
  * FOREIGN KEY (pid) REFERENCES part(pid) ON DELETE CASCADE
  * CONSTRAINT pidpathunique UNIQUE (pid, mode) ON CONFLICT ROLLBACK

action
  * acid INTEGER PRIMARY KEY AUTOINCREMENT
  * pid INTEGER NOT NULL
  * name VARCHAR NOT NULL
  * icon VARCHAR NOT NULL
  * ordering VARCHAR NOT NULL
  * task VARCHAR
  * FOREIGN KEY (pid) REFERENCES part(pid) ON DELETE CASCADE

actionproperty
  * apid INTEGER PRIMARY KEY AUTOINCREMENT
  * acid INTEGER NOT NULL
  * key VARCHAR NOT NULL
  * value VARCHAR NOT NULL
  * FOREIGN KEY (acid) REFERENCES partaction(acid) ON DELETE CASCADE
  * CONSTRAINT acidkeyunique UNIQUE (acid, key) ON CONFLICT ROLLBACK

asgngroup
  * agid INTEGER PRIMARY KEY AUTOINCREMENT
  * aid INTEGER NOT NULL
  * name VARCHAR NOT NULL
  * FOREIGN KEY (aid) REFERENCES assignment(aid) ON DELETE CASCADE
  * CONSTRAINT aidnameunique UNIQUE (aid, name) ON CONFLICT ROLLBACK

groupmember
  * gmid INTEGER PRIMARY KEY AUTOINCREMENT
  * agid INTEGER NOT NULL
  * sid INTEGER NOT NULL
  * FOREIGN KEY (agid) REFERENCES asgngroup(agid) ON DELETE CASCADE
  * FOREIGN KEY (sid) REFERENCES student(sid) ON DELETE CASCADE
  * CONSTRAINT singlemembership UNIQUE (agid, sid) ON CONFLICT ROLLBACK

grade
  * pid INTEGER NOT NULL
  * agid INTEGER NOT NULL
  * earned DOUBLE
  * daterecorded VARCHAR NOT NULL
  * tid INTEGER NOT NULL //TA which last updated this grade
  * submitted INTEGER NOT NULL DEFAULT 1
  * FOREIGN KEY (agid) REFERENCES asgngroup(agid) ON DELETE CASCADE
  * FOREIGN KEY (pid) REFERENCES part(pid) ON DELETE CASCADE
  * FOREIGN KEY (tid) REFERENCES ta(tid) ON DELETE CASCADE

flag
  * pid INTEGER NOT NULL
  * agid INTEGER NOT NULL
  * note TEXT
  * daterecorded VARCHAR NOT NULL
  * tid INTEGER NOT NULL
  * FOREIGN KEY (agid) REFERENCES asgngroup(agid) ON DELETE CASCADE
  * FOREIGN KEY (pid) REFERENCES part(pid) ON DELETE CASCADE
  * FOREIGN KEY (tid) REFERENCES ta(tid) ON DELETE CASCADE
  * CONSTRAINT oneflag UNIQUE (agid, pid) ON CONFLICT REPLACE

extension
  * geid INTEGER NOT NULL
  * agid INTEGER NOT NULL
  * ontime VARCHAR NOT NULL
  * shiftdates INTEGER NOT NULL DEFAULT 0
  * note TEXT
  * daterecorded VARCHAR NOT NULL
  * tid INTEGER NOT NULL
  * FOREIGN KEY (agid) REFERENCES asgngroup(agid) ON DELETE CASCADE
  * FOREIGN KEY (geid) REFERENCES gradableevent(geid) ON DELETE CASCADE
  * FOREIGN KEY (tid) REFERENCES ta(tid) ON DELETE CASCADE
  * CONSTRAINT singleextension UNIQUE (agid, geid) ON CONFLICT REPLACE

geoccurrence
  * geid INTEGER NOT NULL
  * agid INTEGER NOT NULL
  * time VARCHAR NOT NULL
  * path VARCHAR //if null, means a non-digital handin for which the time was recorded manually
  * daterecorded VARCHAR NOT NULL
  * tid INTEGER //if null, means was a digital handin the time of which was recorded automatically
  * FOREIGN KEY (geid) REFERENCES gradableevent(geid) ON DELETE CASCADE
  * FOREIGN KEY (agid) REFERENCES asgngroup(agid) ON DELETE CASCADE
  * FOREIGN KEY (tid) REFERENCES ta(tid) ON DELETE CASCADE
  * CONSTRAINT singlehandin UNIQUE (agid, geid) ON CONFLICT REPLACE

exemption
  * geid INTEGER NOT NULL
  * agid INTEGER NOT NULL
  * note TEXT
  * daterecorded VARCHAR NOT NULL
  * tid INTEGER NOT NULL
  * FOREIGN KEY (agid) REFERENCES asgngroup(agid) ON DELETE CASCADE
  * FOREIGN KEY (geid) REFERENCES gradableevent(geid) ON DELETE CASCADE
  * FOREIGN KEY (tid) REFERENCES ta(tid) ON DELETE CASCADE
  * CONSTRAINT singleextension UNIQUE (agid, geid) ON CONFLICT REPLACE

adjustment
  * aid INTEGER NOT NULL
  * sid INTEGER NOT NULL
  * note TEXT
  * points DOUBLE
  * tid INTEGER NOT NULL
  * daterecorded VARCHAR NOT NULL
  * FOREIGN KEY (aid) REFERENCES assignment(aid) ON DELETE CASCADE
  * FOREIGN KEY (sid) REFERENCES student(sid) ON DELETE CASCADE
  * CONSTRAINT singleadjustment UNIQUE (aid, sid) ON CONFLICT REPLACE

courseproperties
  * cpid INTEGER PRIMARY KEY AUTOINCREMENT
  * key VARCHAR NOT NULL
  * value BLOB NOT NULL
  * CONSTRAINT keysunique UNIQUE (key) ON CONFLICT ROLLBACK

notifyaddresses
  * naid INTEGER PRIMARY KEY AUTOINCREMENT
  * address VARCHAR NOT NULL