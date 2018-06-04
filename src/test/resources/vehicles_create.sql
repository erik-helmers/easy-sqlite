
CREATE TABLE IF NOT EXISTS cars(
  id text PRIMARY KEY,
  seats integer,
  price integer,
  proprietary text
);

CREATE INDEX IF NOT EXISTS cars_proprietary ON cars(proprietary);

