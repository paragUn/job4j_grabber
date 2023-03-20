create table post(
    id serial primary key,
    title text,
    description text,
    link text unique not null,
    created timestamp
);
