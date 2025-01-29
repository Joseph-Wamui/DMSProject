#!/bin/sh

# wait-for-db.sh

set -e

retries=10
count=0
host="$1"
shift
cmd="$@"

until nc -z "$host" 3306 || [ "$count" -eq "$retries" ]; do
  echo "Waiting for database at $host to be ready... (attempt $((count + 1)))"
  count=$((count + 1))
  sleep 2
done

if [ "$count" -eq "$retries" ]; then
  echo "Database not ready after $retries attempts. Exiting."
  exit 1
fi

exec $cmd