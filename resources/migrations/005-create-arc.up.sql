create table arc (
   id uuid primary key default uuid_generate_v1mc(),
   created_at timestamptz not null default clock_timestamp(),
   updated_at timestamptz not null default clock_timestamp(),
   deleted_at timestamptz,
   name varchar(256),
   description text,
   campaign_id uuid references campaign
)
