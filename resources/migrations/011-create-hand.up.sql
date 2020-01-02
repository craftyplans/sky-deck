create table hand (
   id uuid primary key default uuid_generate_v1mc(),
   round_id uuid references round,
   character_id uuid references character,
   battle_id uuid references battle
);
