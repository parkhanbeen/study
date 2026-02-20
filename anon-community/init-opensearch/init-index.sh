#!/bin/bash
# Initialize OpenSearch index for anon-community
# This script is kept in sync with PostIndexMapping.kt (source of truth)
# Index: posts-v4, Alias: posts-read

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
OS_URL="${OPENSEARCH_URL:-http://localhost:9200}"
INDEX_NAME="posts-v4"
ALIAS_NAME="posts-read"

echo "Waiting for OpenSearch to be ready..."
while ! curl -sf "$OS_URL/_cluster/health" > /dev/null 2>&1; do
    sleep 2
done

# Check if index already exists
if curl -sf "$OS_URL/$INDEX_NAME" > /dev/null 2>&1; then
    echo "Index '$INDEX_NAME' already exists, skipping creation."
else
    echo "Creating '$INDEX_NAME' index..."
    RESPONSE=$(curl -s -w "\n%{http_code}" -X PUT "$OS_URL/$INDEX_NAME" \
      -H "Content-Type: application/json" \
      -d @"$SCRIPT_DIR/posts-index-mapping.json")

    HTTP_CODE=$(echo "$RESPONSE" | tail -1)
    BODY=$(echo "$RESPONSE" | head -n -1)

    if [ "$HTTP_CODE" = "200" ]; then
        echo "Index '$INDEX_NAME' created successfully."
    else
        echo "Failed to create index (HTTP $HTTP_CODE): $BODY"
        exit 1
    fi
fi

# Check if alias already points to the index
ALIAS_TARGET=$(curl -sf "$OS_URL/_alias/$ALIAS_NAME" 2>/dev/null | python3 -c "import sys,json; print(','.join(json.load(sys.stdin).keys()))" 2>/dev/null || echo "")

if echo "$ALIAS_TARGET" | grep -q "$INDEX_NAME"; then
    echo "Alias '$ALIAS_NAME' already points to '$INDEX_NAME'."
else
    echo "Creating alias '$ALIAS_NAME' -> '$INDEX_NAME'..."
    curl -s -X POST "$OS_URL/_aliases" \
      -H "Content-Type: application/json" \
      -d "{
        \"actions\": [
          { \"add\": { \"index\": \"$INDEX_NAME\", \"alias\": \"$ALIAS_NAME\" } }
        ]
      }"
    echo ""
    echo "Alias created."
fi

echo ""
echo "Verifying..."
curl -s "$OS_URL/$INDEX_NAME/_mapping" | python3 -m json.tool | head -10
echo "..."
echo ""
echo "Done. Index: $INDEX_NAME, Alias: $ALIAS_NAME"
