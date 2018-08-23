create keyspace if not exists starwars
with replication = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 };

create type if not exists starwars.episode (
  id tinyint,
  name text
);

create type if not exists starwars.character (
  id text,
  name text,
  appears_in list<frozen<starwars.episode>>
);

create type if not exists starwars.droid (
  id text,
  name text,
  friends list<frozen<starwars.character>>,
  appears_in list<frozen<starwars.episode>>,
  primary_function text
);

create type if not exists starwars.human (
  id text,
  name text,
  friends list<frozen<starwars.character>>,
  appears_in list<frozen<starwars.episode>>,
  home_planet text
);

create table if not exists starwars.movie(
  id text,
  year_released smallint,
  episode_released episode,
  droids list<frozen<starwars.droid>>,
  humans list<frozen<starwars.human>>,
  comment text,
  primary key (id, year_released)
) with clustering order by (year_released desc);
