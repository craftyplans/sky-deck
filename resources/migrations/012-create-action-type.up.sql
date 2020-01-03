create table action_type (
   id uuid primary key default uuid_generate_v1mc(),
   slug varchar(32),
   name varchar(256),
   unique (slug)
);
