create table session (
      id uuid primary key default uuid_generate_v1mc(),
      created_at timestamptz not null default clock_timestamp(),
      updated_at timestamptz not null default clock_timestamp(),
      deleted_at timestamptz,

      campaign_id uuid references campaign,
      arc_id uuid references arc
);
