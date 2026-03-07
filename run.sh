CLIENTS_FILE="clients.txt"
NUM_SERVERS=5

mvn exec:java -Dexec.mainClass="com.chatapp.server.ChatServer" -Dexec.args="$CLIENTS_FILE $NUM_SERVERS"

