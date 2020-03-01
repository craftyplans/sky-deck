create type battle_state as enum (
    'started',
    'completed'
);

create table battle (
    id uuid primary key default uuid_generate_v1mc(),
    created_at timestamptz not null default clock_timestamp(),
    updated_at timestamptz not null default clock_timestamp(),
    deleted_at timestamptz,
    number serial,
    state battle_state default 'started',
    initiated_by_id uuid references character,
    session_id uuid references session,
    max_anonymous_characters int default 10,
    campaign_id uuid references campaign
)
