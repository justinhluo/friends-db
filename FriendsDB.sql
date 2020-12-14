DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS photos CASCADE;
DROP TABLE IF EXISTS photo_stats CASCADE;
DROP TABLE IF EXISTS follows CASCADE;
DROP TABLE IF EXISTS comments CASCADE;
DROP TABLE IF EXISTS user_tag CASCADE;
DROP TABLE IF EXISTS photo_tag CASCADE;
DROP TABLE IF EXISTS Userid CASCADE;
DROP TABLE IF EXISTS Photoid CASCADE;


CREATE TABLE Userid (
useridcnt int NOT NULL,
PRIMARY KEY (useridcnt)
);

INSERT INTO userid VALUES(1);

CREATE TABLE Photoid (
photoidcnt int NOT NULL,
PRIMARY KEY (photoidcnt)
);

INSERT INTO Photoid VALUES(1);

CREATE TABLE Users (
	
	user_id int NOT NULL, 
	user_name varchar(30) unique,
	email varchar(30),
	password varchar(30),
	num_followers int, 
	num_following int, 
	PRIMARY KEY (user_id)
);

CREATE TABLE Photos (
	photo_id int NOT NULL, 
	user_id int NOT NULL,
	imgname text,
	img bytea,
	title varchar(100),
	caption varchar(2200), 
	day varchar(10), 
	PRIMARY KEY (photo_id),
	FOREIGN KEY (user_id) REFERENCES users(user_id)
);

CREATE TABLE Photo_stats (
	photo_id int NOT NULL, 
	num_likes int, 
	num_dislikes int,
	num_comments int, 
	num_views int, 
	PRIMARY KEY (photo_id)
);

CREATE TABLE Follows (
	user_id int NOT NULL,
	following_id int NOT NULL,
	PRIMARY KEY (user_id, following_id)
);

CREATE TABLE Comments (
	photo_id int NOT NULL,
	user_id int NOT NULL,
	comment varchar(300)
	
);

CREATE TABLE Photo_tag (
	photo_id int NOT NULL, 
	tag text[],
	PRIMARY KEY (photo_id, tag)
);

CREATE TABLE User_tag (
	photo_id int NOT NULL, 
	user_name text[],
	PRIMARY KEY (photo_id, user_name)
);


