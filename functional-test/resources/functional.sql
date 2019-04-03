INSERT INTO TILE (ID, TILE_TYPE, COLOUR, FETCH_URL, TITLE, ICON) VALUES
  ('tile', 'count', 1, 'http://provider', 'Printer Credit', 'print'),
  ('other-tile', 'count', 2, 'http://provider', 'Mail', 'envelope'),
  ('heron-tile', 'count', 3, 'http://herons-eat-ducklings', 'Mail', 'envelope');

-- comment
INSERT INTO TILE_GROUP (TILE_ID, "GROUP") VALUES
  ('tile', 'staff'),
  ('tile', 'student'),
  ('other-tile', 'staff'),
  ('heron-tile', 'student'),
  ('heron-tile', 'anonymous');
