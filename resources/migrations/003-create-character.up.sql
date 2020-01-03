create type character_type as enum (
    'player',
    'npc'
);

create table character (
    id uuid primary key default uuid_generate_v1mc(),
    created_at timestamptz not null default clock_timestamp(),
    updated_at timestamptz not null default clock_timestamp(),
    deleted_at timestamptz,
    name varchar(256),
    actions int default 2,
    type character_type default 'player',
    hit_point_max int,
    hit_point_current int,
    age int,
    agility int,
    strength int,
    mind int,
    soul int,
    skill_points int,
    reputation  int,
    master_points int,
    divinity_points int,
    moments int,
    past_lives int,
    charges int,
    background text
)
