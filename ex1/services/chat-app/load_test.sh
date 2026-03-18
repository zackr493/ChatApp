#!/bin/bash
URL="http://localhost:8080"
TIMEOUT_MS=30000
CHAT_DURATION=3
BATCH=100

WALL_START=$(date +%s)
echo "Starting load test..."

while read -r name; do
    (
        create_res=$(curl -s -X POST "$URL/clients" \
            -H "Content-Type: application/json" \
            -d "{\"clientName\": \"$name\"}")
        client_id=$(echo "$create_res" | grep -o '"id":"[^"]*"' | head -1 | cut -d'"' -f4)
        [ -z "$client_id" ] && exit

        join_res=$(curl -s -X POST "$URL/servers/join" \
            -H "Content-Type: application/json" \
            -d "{\"clientId\": \"$client_id\", \"timeoutMs\": $TIMEOUT_MS}")
        session_id=$(echo "$join_res" | grep -o '"data":"[^"]*"' | cut -d'"' -f4)
        [ -z "$session_id" ] && exit

        status="WAITING"
        while [ "$status" = "WAITING" ] || [ "$status" = "CREATED" ]; do
            sleep 1
            status=$(curl -s "$URL/session/$session_id" | grep -o '"status":"[^"]*"' | cut -d'"' -f4)
        done

        [ "$status" != "ASSIGNED" ] && exit

        sleep $CHAT_DURATION
        rating=$(( (RANDOM % 5) + 1 ))
        curl -s -X POST "$URL/servers/finish" \
            -H "Content-Type: application/json" \
            -d "{\"sessionId\": \"$session_id\", \"rating\": $rating}" > /dev/null
    ) &

    if (( $(jobs -r | wc -l) >= BATCH )); then
        wait -n 2>/dev/null || wait
    fi

done < clients.txt

wait
echo "Done in $(( $(date +%s) - WALL_START ))s — check the dashboard for stats."