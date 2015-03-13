gradingsheetsection
  * gs\_sid INTEGER PRIMARY KEY AUTOINCREMENT
  * pid INTEGER NOT NULL
  * name VARCHAR NOT NULL
  * ordering INTEGER NOT NULL
  * outof DOUBLE //may be null; if not null, used for subtractive grading as earned and out of
  * FOREIGN KEY pid REFERENCES part(pid) ON DELETE CASCADE
  * CONSTRAINT uniquenames UNIQUE (pid, gs\_sid, name) ON CONFLICT ROLLBACK

gradingsheetsubsection
  * gs\_ssid INTEGER PRIMARY KEY AUTOINCREMENT
  * gs\_sid INTEGER NOT NULL
  * text VARCHAR NOT NULL
  * ordering INTEGER NOT NULL
  * outof DOUBLE //may be null; will mean grading sheet visualization shows no red/yellow
  * FOREIGN KEY gs\_sid REFERENCES gradingsheetsection(gs\_sid) ON DELETE CASCADE
  * CONSTRAINT uniquenames UNIQUE (gs\_sid, gs\_ssid, name) ON CONFLICT ROLLBACK

gradingsheetdetail
  * gs\_did INTEGER PRIMARY KEY AUTOINCREMENT
  * gs\_ssid INTEGER NOT NULL
  * text VARCHAR NOT NULL
  * ordering INTEGER NOT NULL
  * FOREIGN KEY gs\_ssid REFERENCES gradingsheetsubsection CONFLICT ROLLBACK

groupgradingsheet
  * ggsid INTEGER PRIMARY KEY AUTOINCREMENT
  * pid INTEGER NOT NULL
  * agid INTEGER NOT NULL
  * assignedto INTEGER
  * submittedby INTEGER //ta who last submitted the group grading sheet; null if never submitted
  * datesubmitted VARCHAR //time the group grading sheet was last submitted; null if never submitted
  * FOREIGN KEY pid REFERENCES part(pid) ON DELETE CASCADE
  * FOREIGN KEY agid REFERENCES asgngroup(agid) ON DELETE CASCADE
  * FOREIGN KEY assignedto REFERENCES ta(tid) ON DELETE CASCADE
  * FOREIGN KEY submittedby REFERENCES ta(tid) ON DELETE CASCADE
  * CONSTRAINT onepergroupperpart UNIQUE(pid, agid) ON CONFLICT REPLACE

groupgradingsheetsubsection
  * ggsid INTEGER NOT NULL
  * gs\_ssid INTEGER NOT NULL
  * earned DOUBLE
  * lastmodifiedby INTEGER NOT NULL
  * lastmodifieddate VARCHAR NOT NULL
  * FOREIGN KEY ggsid REFERENCES groupgradingsheet(ggsid) ON DELETE CASCADE
  * FOREIGN KEY gs\_ssid REFERENCES gradingsheetsubsection(gs\_ssid) ON DELETE CASCADE
  * FOREIGN KEY lastmodifiedby REFERENCES ta(tid) ON DELETE CASCADE
  * CONSTRAINT onepersubsectionpergroup UNIQUE (agid, gs\_ssid) ON CONFLICT REPLACE

groupgradingsheetcomments
  * ggsid INTEGER NOT NULL
  * gs\_sid INTEGER NOT NULL
  * comments VARCHAR
  * lastmodifiedby INTEGER NOT NULL
  * lastmodifieddate VARCHAR NOT NULL
  * FOREIGN KEY ggsid REFERENCES groupgradingsheet(ggsid) ON DELETE CASCADE
  * FOREIGN KEY gs\_sid REFERENCES gradingsheetsection(gs\_sid) ON DELETE CASCADE
  * FOREIGN KEY lastmodifiedby REFERENCES ta(tid) ON DELETE CASCADE
  * CONSTRAINT onepersectionpergroup UNIQUE (agid, gs\_sid) ON CONFLICT REPLACE