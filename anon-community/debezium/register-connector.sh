#!/bin/bash
echo "Waiting for Kafka Connect to be ready..."
while ! curl -s http://localhost:8083/connectors > /dev/null 2>&1; do
    sleep 2
done

echo "Registering Debezium connector..."
curl -X POST http://localhost:8083/connectors \
  -H "Content-Type: application/json" \
  -d @"$(dirname "$0")/register-connector.json"

echo ""
echo "Connector registered. Checking status..."
sleep 3
curl -s http://localhost:8083/connectors/anon-community-connector/status | python3 -m json.tool
