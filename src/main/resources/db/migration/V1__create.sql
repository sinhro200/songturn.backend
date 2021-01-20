
CREATE TABLE "role" (
    "id" SERIAL PRIMARY KEY,
    "name" TEXT DEFAULT 'ROLE_USER' NOT NULL UNIQUE
    );

CREATE TABLE "users" (
    "id" SERIAL PRIMARY KEY,
    "nickname" TEXT NOT NULL,
    "email" TEXT UNIQUE,           -- NOT NULL
    "login" TEXT NOT NULL UNIQUE,
    "password" TEXT NOT NULL,
    "role_id" INTEGER NOT NULL,
    "first_name" TEXT ,                     -- NOT NULL
    "last_name" TEXT ,                      -- NOT NULL
    "is_verified" BOOLEAN NOT NULL DEFAULT FALSE,
    "room_id" INTEGER,
    CONSTRAINT "users_role_id-role_id"
        FOREIGN KEY ("role_id") REFERENCES "role"("id")
            ON DELETE RESTRICT
            ON UPDATE CASCADE
);

CREATE TABLE "confirmation_token" (
    "id" SERIAL PRIMARY KEY,
    "token" TEXT NOT NULL UNIQUE,
    "created_date" TIMESTAMP NOT NULL,
    "user_id" INTEGER NOT NULL,
    CONSTRAINT "confirmation_token_user_id-users_id"
        FOREIGN KEY ("user_id") REFERENCES "users"("id")
            ON DELETE CASCADE
            ON UPDATE CASCADE
);

CREATE TABLE "room" (
    "id" SERIAL PRIMARY KEY,
    "invite" TEXT NOT NULL UNIQUE,
    "token" TEXT UNIQUE, -- NOT NULL
    "title" TEXT NOT NULL , -- UNIQUE
    "owner_id" INTEGER,
    "rs_priority_rarely_ordering_users" BOOLEAN DEFAULT false NOT NULL,
    "rs_allow_votes" BOOLEAN NOT NULL DEFAULT true,
    "rs_song_owners_visible" BOOLEAN NOT NULL DEFAULT false,
    "rs_any_can_listen" BOOLEAN NOT NULL DEFAULT false,
    CONSTRAINT "room_owner_id-users_id"
        FOREIGN KEY ("owner_id") REFERENCES "users"("id")
            ON DELETE RESTRICT
            ON UPDATE CASCADE
);

CREATE TABLE "room_action" (
    "id" SERIAL PRIMARY KEY,
    "user_id" INTEGER NOT NULL,
    "room_id" INTEGER NOT NULL,
    "action_type" INTEGER NOT NULL,
    "is_change_action" BOOLEAN NOT NULL default false,
    "timestamp" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    -- PRIMARY KEY(room_id, user_id, action_type, is_room_action)
     CONSTRAINT "room_id-room_id"
        FOREIGN KEY ("room_id") REFERENCES "room"("id")
            ON DELETE CASCADE
            ON UPDATE CASCADE,
    CONSTRAINT "user_id-user_id"
        FOREIGN KEY ("user_id") REFERENCES "users"("id")
            ON DELETE RESTRICT
            ON UPDATE CASCADE
);

CREATE TABLE "playlist" (
    "id" SERIAL PRIMARY KEY,
    "title" TEXT NOT NULL,
    "description" TEXT NOT NULL,
    "room_id" INTEGER NOT NULL,
    "current_song_id" INTEGER,
    "listener_id" INTEGER,
    CONSTRAINT "playlist_room_id-room_id"
        FOREIGN KEY ("room_id") REFERENCES "room"("id")
            ON DELETE CASCADE
            ON UPDATE CASCADE ,
    CONSTRAINT "playlist_listener_id-users_id"
        FOREIGN KEY ("listener_id") REFERENCES "users"("id")
            ON DELETE SET NULL
            ON UPDATE CASCADE
);

CREATE TABLE "song" (
    "id" SERIAL PRIMARY KEY,
    "artist" TEXT NOT NULL,
    "title" TEXT NOT NULL,
    "link" TEXT NOT NULL,
    "duration" INTEGER NOT NULL,
    "ordered_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "expires_at" TIMESTAMP DEFAULT NULL,
    "playlist_id" INTEGER NOT NULL,
    "user_id" INTEGER DEFAULT NULL,
    "rating" INTEGER DEFAULT 0 NOT NULL,
    "link_from_user" TEXT NOT NULL,
    CONSTRAINT "song_playlist_id-playlist_id"
        FOREIGN KEY ("playlist_id") REFERENCES "playlist"("id")
            ON DELETE CASCADE
            ON UPDATE CASCADE ,
    CONSTRAINT "song_user_id-users_id"
        FOREIGN KEY ("user_id") REFERENCES "users"("id")
            ON DELETE SET NULL
            ON UPDATE CASCADE

);

ALTER TABLE "playlist" ADD CONSTRAINT "playlist_current_song_id-song_id"
    FOREIGN KEY ("current_song_id") REFERENCES "song"("id")
        ON DELETE SET NULL
        ON UPDATE CASCADE;

CREATE TABLE "voted_songs" (
    "id" SERIAL PRIMARY KEY,
    "user_id" INTEGER NOT NULL,
    "song_id" INTEGER NOT NULL,
    "action" INTEGER NOT NULL,                  -- {+1,-1} or 0 mb
    CONSTRAINT "voted_songs_user_id-users_id"
        FOREIGN KEY ("user_id") REFERENCES "users"("id")
            ON DELETE CASCADE
            ON UPDATE CASCADE,
    CONSTRAINT "voted_songs_song_id-song_id"
        FOREIGN KEY ("song_id") REFERENCES "song"("id")
            ON DELETE CASCADE
            ON UPDATE CASCADE
);