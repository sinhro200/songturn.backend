create function user_constraints() returns trigger
    language plpgsql AS
$$
BEGIN
    CASE
        WHEN (length(new.nickname) < 5)
            THEN RAISE EXCEPTION USING MESSAGE =
                    'ErrorMessage{nickname must contain more than 4 symbols}';
        WHEN (length(new.nickname) > 50)
            THEN RAISE EXCEPTION USING MESSAGE =
                    'ErrorMessage{nickname must contain less than 51 symbols}';
        WHEN (length(new.login) < 5)
            THEN RAISE EXCEPTION USING MESSAGE =
                    'ErrorMessage{login must contain more than 4 symbols}';
        WHEN (length(new.login) > 50)
            THEN RAISE EXCEPTION USING MESSAGE =
                    'ErrorMessage{login must contain less than 51 symbols}';
        ELSE
            return NEW;
        END CASE;
END
$$;

CREATE TRIGGER "user_constraints"
    BEFORE UPDATE OR INSERT
    ON "users"
    FOR EACH ROW
EXECUTE PROCEDURE user_constraints()
;

create function countPlaylists(IN roomId int, IN playlistTitle text) returns int
    language plpgsql as
$$
declare
    count int ;
begin

    SELECT count(id)
    INTO count
    FROM playlist as pl
    WHERE pl.room_id = roomId
      and pl.title = playlistTitle;
    return count;
end
$$;

create function playlist_constraints() returns trigger
    language plpgsql AS
$$
BEGIN
    IF (countPlaylists(new.room_id, new.title) > 0)
    THEN
        RAISE EXCEPTION USING MESSAGE =
                'ErrorMessage{Room already has playlist with this title}';
    END IF;
    return new;
END
$$;

CREATE TRIGGER "playlist_constraints"
    BEFORE UPDATE OR INSERT
    ON "playlist"
    FOR EACH ROW
EXECUTE PROCEDURE playlist_constraints()
;