INSERT INTO TILE (ID, TILE_TYPE, DEFAULT_SIZE, DEFAULT_POSITION, COLOUR, FETCH_URL, TITLE, ICON) VALUES
  ('tile', 'count', 'large', 0, 1, 'http://provider', 'Printer Credit', 'print'),
  ('other-tile', 'count', 'wide', 1, 2, 'http://provider', 'Mail', 'envelope-o'),
  ('heron-tile', 'count', 'small', 2, 3, 'http://herons-eat-ducklings', 'Mail', 'envelope-o');

-- comment
INSERT INTO TILE_GROUP (TILE_ID, "GROUP") VALUES
  ('tile', 'staff'),
  ('tile', 'student'),
  ('other-tile', 'staff'),
  ('heron-tile', 'student'),
  ('heron-tile', 'anonymous');