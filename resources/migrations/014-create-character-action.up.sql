create table character_action (
    id uuid primary key default uuid_generate_v1mc(),
    character_id uuid references character,
    action_type_id uuid references action_type
);
