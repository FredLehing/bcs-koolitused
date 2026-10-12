#!/bin/bash
# Postgresi konteineri esmakäivitus (ainult tühja pgdata volume'i korral):
# loob bcs_koolitused skeemi ja laeb docs/database skriptid 1–3.
set -e
export PGOPTIONS="-c search_path=bcs_koolitused"
for f in 1_reset_database.sql 2_create.sql 3_import.sql; do
  echo "Laen $f"
  psql -v ON_ERROR_STOP=1 -U postgres -d vali_it -f "/database/$f"
done
