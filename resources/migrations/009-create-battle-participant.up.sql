create table battle_participant (
    id uuid primary key default uuid_generate_v1mc(),
    battle_id uuid references  battle,
    character_id uuid references character
);
