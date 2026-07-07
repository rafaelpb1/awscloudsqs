# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Study project (Portuguese docs/comments) for practicing messaging with Amazon SQS, Spring Boot, Spring Cloud AWS, and LocalStack. A REST endpoint accepts a message and sends it to a local SQS queue; a `@SqsListener` consumer picks it up and processes it via a service layer.

## Commands

```bash
./mvnw spring-boot:run   # run the app (port 8080)
./mvnw test               # run tests
./mvnw test -Dtest=SqsApplicationTests#contextLoads   # run a single test
./mvnw clean package       # build
```

The app requires LocalStack running locally with SQS support, because `SqsApplicationTests` boots the full Spring context and `MyConsumer` has an active `@SqsListener`:

```bash
docker run --rm -it -p 4566:4566 -e SERVICES=sqs -e DEFAULT_REGION=sa-east-1 localstack/localstack

aws --endpoint-url=http://localhost:4566 sqs create-queue --queue-name minhafila --region sa-east-1
```

If AWS CLI isn't configured, use fake credentials (LocalStack requires *some* credentials but doesn't validate them):

```bash
export AWS_ACCESS_KEY_ID=test
export AWS_SECRET_ACCESS_KEY=test
export AWS_DEFAULT_REGION=sa-east-1
```

## Architecture

Message flow is a straight pipeline, one class per responsibility:

```
MessageController -> MessageProducer (interface) -> SqsMessageProducer -> LocalStack SQS ("minhafila")
                                                                                   |
                                                                                   v
                                                                             MyConsumer (@SqsListener)
                                                                                   |
                                                                                   v
                                                                             MessageService
```

- `controller.MessageController` — receives `POST /messages` with a `MessageRequest` body, delegates to `MessageProducer`, returns 202 Accepted.
- `producer.MessageProducer` — interface only; the controller depends on this, not on the concrete SQS implementation (DIP), so the messaging backend could be swapped without touching the HTTP layer.
- `service.SqsMessageProducer` — the concrete `MessageProducer` implementation. Note it lives in the `service` package, not `producer`, despite implementing the `producer` interface.
- `consumer.MyConsumer` — `@SqsListener("${app.aws.sqs.queue-name}")`, deserializes into `MessageRequest`, delegates to `MessageService`. Must be a Spring-managed bean for the listener annotation to work.
- `service.MessageService` — validation/business logic. Rejects null/empty/blank `content` by throwing `exceptions.MessageIsEmptyException`; otherwise logs the processed content.
- `config.SqsConfig` — builds the `SqsAsyncClient` bean pointed at the LocalStack endpoint using static credentials from config.
- `dto.MessageRequest` — single-field record (`content`), the wire/queue payload shape shared by producer and consumer.

All SQS connection details (endpoint, region, queue name, fake credentials) are externalized under `app.aws.sqs.*` in `src/main/resources/application.yml` — read from there rather than assuming values.

There are currently no unit tests for `MessageService`; the only test boots the full Spring context and therefore depends on LocalStack being reachable at `localhost:4566`.
