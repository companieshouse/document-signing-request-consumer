#!/bin/bash

# Start script for document-signing-request-consumer


PORT=8080
exec java -jar -Dserver.port="${PORT}" "document-signing-request-consumer.jar"
