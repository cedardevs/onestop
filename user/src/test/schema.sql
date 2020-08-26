DROP TABLE IF EXISTS savesearch;
CREATE TABLE savesearch
(
    id VARCHAR(255) PRIMARY KEY NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    name VARCHAR(255),
    value VARCHAR(2047),
    filter VARCHAR(2047),
    created_on DATE ,
    last_updated_on DATE
);

DROP TABLE IF EXISTS onestop_users_roles CASCADE;
DROP TABLE IF EXISTS onestop_roles CASCADE;
DROP TABLE IF EXISTS onestop_users CASCADE;

CREATE TABLE onestop_users (
  user_id VARCHAR(255)  NOT NULL,
  created_on DATE ,
  last_updated_on DATE,
--   email varchar(45) NOT NULL,
--   enabled tinyint(4) DEFAULT NULL,
  PRIMARY KEY (user_id)
);

CREATE TABLE onestop_roles (
  role_id VARCHAR(255)  NOT NULL,
  name varchar(45) NOT NULL,
  PRIMARY KEY (role_id)
);


CREATE TABLE onestop_users_roles (
  user_id VARCHAR(255)  NOT NULL REFERENCES onestop_users (user_id) ON UPDATE CASCADE ON DELETE CASCADE,
  role_id VARCHAR(255)  NOT NULL REFERENCES onestop_roles (role_id) ON UPDATE CASCADE,
  CONSTRAINT user_roles_pkey PRIMARY KEY (user_id, role_id)
);
--
-- INSERT INTO onestop_roles (name) VALUES ('PUBLIC');
-- INSERT INTO onestop_roles (name) VALUES ('ADMIN');