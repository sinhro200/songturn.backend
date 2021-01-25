-- create procedure whatShouldUpdate(
--     roomId int,
--     userId int
-- )
--     language plpgsql
-- as $$
-- begin
--     select *
--     from room_action uact
--     where uact.room_id = roomId
--       and uact.user_id = userId
--       and is_room_changed = false
--       and uact.timestamp < (
--           select ract.timestamp
--           from room_action ract
--           where ract.room_id = roomId
--             and is_room_changed = true
--             and uact.action_type = ract.action_type
--     );
--
--     -- commit;
-- end;$$;

create function what_should_update(roomId int,
                                   userId int)
    returns table
            (
                action_type int
            )
    language plpgsql
as
$$
begin
    return query
        select uact.action_type
        from room_action uact
        where uact.room_id = roomId
          and uact.user_id = userId
          and is_change_action = false
          and uact.timestamp < (
            select ract.timestamp
            from room_action ract
            where ract.room_id = roomId
              and is_change_action = true
              and uact.action_type = ract.action_type
        );
end;
$$;

create function calculate_song_rating(calculated_song_id int)
    returns song
    language plpgsql
as
$$
declare
    sum         bigint;
    result_song song;
begin
    SELECT sum(vs.action)
    FROM voted_songs vs
    WHERE vs.song_id = calculated_song_id
    limit 1
    into sum;

    update song
    set rating = sum
    where id = calculated_song_id
    returning *
    into result_song;

    return result_song
    ;
end;
$$;