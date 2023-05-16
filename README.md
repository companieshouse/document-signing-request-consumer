# document-signing-request-consumer
Consumes Kafka messages from the `sign-digital-document` topic.

## Environment variables

The following environment variables are all **mandatory**.

| Variable                               | Type   | Description                                                                                                                  | Example                           | Location |
|----------------------------------------|--------|------------------------------------------------------------------------------------------------------------------------------|-----------------------------------|----------|
| BACKOFF_DELAY                          | number | The delay in milliseconds between message republish attempts.                                                                | 100                               | env var  |
| BOOTSTRAP_SERVER_URL                   | url    | The URLs of the Kafka brokers that the consumers will connect to.                                                            | kafka:9092                        | env var  |
| CONCURRENT_LISTENER_INSTANCES          | number | The number of consumers that should participate in the consumer group. Must be equal to the number of main topic partitions. | 1                                 | env var  |
| DOCUMENT_SIGNING_REQUEST_CONSUMER_PORT | number | Port this application runs on when deployed.                                                                                 | 18629                             | start.sh |
| GROUP_ID                               | string | The group ID of the main consume.                                                                                            | document-signing-request-consumer | env var  |
| MAX_ATTEMPTS                           | number | The maximum number of times messages will be processed before they are sent to the dead letter topic.                        | 4                                 | env var  |
| TOPIC                                  | string | The topic from which the main consumer will consume messages.                                                                | sign-digital-document             | env var  |
| INVALID_MESSAGE_TOPIC                  | string | The topic to which consumers will republish messages if any unchecked exception other than RetryableException is thrown.     | sign-digital-document-invalid     | env var  |
| PREFIX                                 | string | The location in which the signed document will be stored                                                                     | location/certified-document       | env var  |

## Endpoints

| Path                                               | Method | Description                                                         |
|----------------------------------------------------|--------|---------------------------------------------------------------------|
| *`/document-signing-request-consumer/healthcheck`* | GET    | Returns HTTP OK (`200`) to indicate a healthy application instance. |