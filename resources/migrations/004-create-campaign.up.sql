create type campaign_state as enum (
    'new',
    'started',
    'finished'
);

create table campaign (
    id uuid primary key default uuid_generate_v1mc(),
    created_at timestamptz not null default clock_timestamp(),
    updated_at timestamptz not null default clock_timestamp(),
    deleted_at timestamptz,

    number serial,
    state campaign_state default 'new',
    dungeon_master_id uuid references person
);
