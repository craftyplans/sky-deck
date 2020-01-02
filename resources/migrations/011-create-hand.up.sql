create type hand_state as enum (
    'open',
    'closed'
);

create table hand (
   id uuid primary key default uuid_generate_v1mc(),
   round_id uuid references round,
   character_id uuid references character,
   battle_id uuid references battle,
   state hand_state default 'open'
);
