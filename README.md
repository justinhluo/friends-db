# FriendsDB - Social Photo Sharing Console Application

FriendsDB is a Java console-based social networking application that allows users to create accounts, login, and interact by uploading photos, tagging photos and users, liking/disliking photos, following other users, commenting, and searching photos by various criteria.

This project demonstrates building a fully-featured interactive social media platform backend using Java with a PostgreSQL database.

---

## Features

- User registration with validation (username length, email format, password confirmation)
- User login and session management
- Upload photos with file type validation (.jpg and .png supported)
- View, download photos
- Add photo tags and tag other users
- Like and dislike photos
- Follow other users (except yourself)
- Add comments on photos with length restriction
- Search users/photos by tag, title, date, likes, dislikes
- View personal profile and news feed
- Discover recent photos sorted by date and title
- List most popular users by likes or followers

---

## Getting Started

### Prerequisites

- Java Development Kit (JDK) 8+
- PostgreSQL database configured and running
- FriendsDB PostgreSQL schema set up as per project instructions

### How to Run

1. Clone or download this repository.
2. Compile the Java source files:

```bash

-upload the sql to postgres

compile: javac FriendsDB.java

run: java -cp "c:\path\postgresql-42.2.18.jar;c:\path\" FriendsDB DatabaseName PostgresMasterPassword
