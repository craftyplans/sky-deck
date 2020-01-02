create type round_state as enum (
    'open',
    'selected',
    'closed'
);

create table round (
  id uuid primary key default uuid_generate_v1mc(),
  battle_id uuid references battle,
  campaign_id uuid references campaign,
  state round_state default 'open'
)
