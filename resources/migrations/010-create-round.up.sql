create table round (
  id uuid primary key default uuid_generate_v1mc(),
  battle_id uuid references battle
)
