create table post(
    id serial primary key,
    name text,
    link text unique not null,
    description text,
    created timestamp
);