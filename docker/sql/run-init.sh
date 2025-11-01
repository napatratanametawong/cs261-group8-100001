#!/bin/bash
set -Eeuo pipefail

echo "== run-init.sh: start =="

# --- resolve sqlcmd path robustly ---
resolve_sqlcmd() {
  # 1) ภาพ mssql-tools รุ่นเก่า
  if [ -x /opt/mssql-tools/bin/sqlcmd ]; then
    echo /opt/mssql-tools/bin/sqlcmd; return 0
  fi
  # 2) ภาพ mssql-tools18 รุ่นใหม่
  if [ -x /opt/mssql-tools18/bin/sqlcmd ]; then
    echo /opt/mssql-tools18/bin/sqlcmd; return 0
  fi
  # 3) เผื่อมีใน PATH
  if command -v sqlcmd >/dev/null 2>&1; then
    command -v sqlcmd; return 0
  fi
  return 1
}

SQLCMD="$(resolve_sqlcmd)" || { echo "FATAL: sqlcmd not found in container"; exit 127; }
echo "SQLCMD at: $SQLCMD"

echo "== waiting for SQL Server on mssql:1433 =="
for i in {1..120}; do
  if "$SQLCMD" -S mssql,1433 -U sa -P "${SA_PASSWORD}" -Q "SELECT 1" -l 2 >/dev/null 2>&1; then
    break
  fi
  echo "waiting... ($i/120)"; sleep 2
done

echo "== prepare /tmp/init_core.sql (inject password) =="
# escape single quotes for T-SQL literal (abc'd -> abc''d)
PW_ESC="$(printf "%s" "${SPRING_DATASOURCE_PASSWORD}" | sed "s/'/''/g")"
sed "s/__APPPASS__/${PW_ESC}/g" /sql/init_core.sql > /tmp/init_core.sql

echo "== ensure bookingDB exists (preflight) =="
"$SQLCMD" -S mssql,1433 -U sa -P "${SA_PASSWORD}" -b -V 16 -l 5 -d master -Q "IF DB_ID('bookingDB') IS NULL CREATE DATABASE [bookingDB];"

echo "== run init_core.sql =="
"$SQLCMD" -S mssql,1433 -U sa -P "${SA_PASSWORD}" -b -V 16 -l 5 -i /tmp/init_core.sql

echo "== seed rooms =="
"$SQLCMD" -S mssql,1433 -U sa -P "${SA_PASSWORD}" -b -V 16 -l 5 -d bookingDB -i /sql/seed_rooms.sql

echo "== seed timeslots =="
"$SQLCMD" -S mssql,1433 -U sa -P "${SA_PASSWORD}" -b -V 16 -l 5 -d bookingDB -i /sql/seed_timeslots.sql

echo "DB init + seed done."
