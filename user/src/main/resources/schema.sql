DROP TABLE IF EXISTS savesearch;
CREATE TABLE savesearch
(
    id VARCHAR(255) PRIMARY KEY NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    value VARCHAR(2047),
    created_on DATE ,
    last_updated_on DATE
);