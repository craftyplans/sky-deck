create table person (
    id uuid primary key default uuid_generate_v1mc(),
    created_at timestamptz not null default clock_timestamp(),
    updated_at timestamptz not null default clock_timestamp(),
    deleted_at timestamptz,
    username varchar(256),
    email varchar(256),
    password text,
    enabled boolean default false,
    unique (email),
    unique (username)
)
