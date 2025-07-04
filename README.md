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

## Terraform ECS
### What does this code do?
The code present in this repository is used to define and deploy a dockerised container in AWS ECS.
This is done by calling a [module](https://github.com/companieshouse/terraform-modules/tree/main/aws/ecs) from terraform-modules. Application specific attributes are injected and the service is then deployed using Terraform via the CICD platform 'Concourse'.
Application specific attributes | Value                                | Description
:---------|:-----------------------------------------------------------------------------|:-----------
**ECS Cluster**        |order-service                                      | ECS cluster stack the service belongs to
**Concourse pipeline**     |[Pipeline link](https://ci-platform.companieshouse.gov.uk/teams/team-development/pipelines/document-signing-request-consumer) <br> [Pipeline code](https://github.com/companieshouse/ci-pipelines/blob/master/pipelines/ssplatform/team-development/document-signing-request-consumer)                               | Concourse pipeline link in shared services
### Contributing
- Please refer to the [ECS Development and Infrastructure Documentation](https://companieshouse.atlassian.net/wiki/spaces/DEVOPS/pages/4390649858/Copy+of+ECS+Development+and+Infrastructure+Documentation+Updated) for detailed information on the infrastructure being deployed.
### Testing
- Ensure the terraform runner local plan executes without issues. For information on terraform runners please see the [Terraform Runner Quickstart guide](https://companieshouse.atlassian.net/wiki/spaces/DEVOPS/pages/1694236886/Terraform+Runner+Quickstart).
- If you encounter any issues or have questions, reach out to the team on the **#platform** slack channel.
### Vault Configuration Updates
- Any secrets required for this service will be stored in Vault. For any updates to the Vault configuration, please consult with the **#platform** team and submit a workflow request.
### Useful Links
- [ECS service config dev repository](https://github.com/companieshouse/ecs-service-configs-dev)
- [ECS service config production repository](https://github.com/companieshouse/ecs-service-configs-production)
