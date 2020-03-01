create type character_type as enum (
    'player',
    'anonymous',
    'npc'
);

create table character (
    id uuid primary key default uuid_generate_v1mc(),
    created_at timestamptz not null default clock_timestamp(),
    updated_at timestamptz not null default clock_timestamp(),
    deleted_at timestamptz,
    name varchar(256),
    actions bigint default 2,
    type character_type default 'player',
    hit_point_max bigint,
    hit_point_current bigint,
    age bigint,
    agility bigint,
    strength bigint,
    mind bigint,
    soul bigint,
    skill_points bigint,
    reputation  bigint,
    master_points bigint,
    divinity_points bigint,
    moments bigint,
    past_lives bigint,
    charges bigint,
    background text
)
