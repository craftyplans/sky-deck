create table hand_action (
   id uuid primary key default uuid_generate_v1mc(),
   hand_id uuid references hand,
   action_type_id uuid references action_type,
   target_id uuid references character
)
