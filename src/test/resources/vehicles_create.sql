
CREATE TABLE IF NOT EXISTS cars(
  id integer PRIMARY KEY NOT NULL,
  seats integer,
  price integer,
  proprietary text
);

CREATE INDEX IF NOT EXISTS cars_proprietary ON cars(proprietary);

