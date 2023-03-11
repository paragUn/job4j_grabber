create table post(
    id serial primary key,
    name text,
    description text,
    link text unique not null,
    created timestamp
);