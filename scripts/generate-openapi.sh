#!/bin/bash
# Generate OpenAPI specs from running services
# Usage: ./scripts/generate-openapi.sh
# Requires: Services must be running (docker-compose up -d)

set -e

DOCS_DIR="docs/api"
mkdir -p "$DOCS_DIR"

declare -A SERVICES=(
  ["inventory"]="8083"
  ["order"]="8086"
  ["payment"]="8084"
)

echo "Generating OpenAPI specifications..."

for service in "${!SERVICES[@]}"; do
  port="${SERVICES[$service]}"
  output="$DOCS_DIR/${service}-openapi.json"
  
  echo -n "  $service (port $port)... "
  
  if curl -sf "http://localhost:$port/v3/api-docs" -o "$output" 2>/dev/null; then
    echo "OK -> $output"
  else
    echo "FAILED (service not running?)"
  fi
done

echo ""
echo "Swagger UI available at:"
for service in "${!SERVICES[@]}"; do
  echo "  - $service: http://localhost:${SERVICES[$service]}/swagger-ui.html"
done
