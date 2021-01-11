create function user_constraints() returns trigger
    language plpgsql AS
$$
    BEGIN
        CASE
            WHEN (length(new.nickname) < 5)
                THEN RAISE EXCEPTION USING MESSAGE = 'ErrorMessage{nickname must contain more than 4 symbols}';
            WHEN (length(new.nickname) > 50)
                THEN RAISE EXCEPTION USING MESSAGE = 'ErrorMessage{nickname must contain less than 51 symbols}';
            WHEN (length(new.login) < 5)
                THEN RAISE EXCEPTION USING MESSAGE = 'ErrorMessage{login must contain more than 4 symbols}';
            WHEN (length(new.login) > 50)
                THEN RAISE EXCEPTION USING MESSAGE = 'ErrorMessage{login must contain less than 51 symbols}';
            ELSE
                return NEW;
        END CASE;
    END
$$;

CREATE TRIGGER "user_constraints"
    BEFORE UPDATE OR INSERT ON "users"
    FOR EACH ROW
    EXECUTE PROCEDURE user_constraints()
;