# SQS LocalStack Lab

![Java](https://img.shields.io/badge/Java-21-007396?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.6-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![AWS SQS](https://img.shields.io/badge/AWS%20SQS-LocalStack-FF9900?style=for-the-badge&logo=amazonaws&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-Wrapper-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)

Projeto de estudo para praticar mensageria com **Amazon SQS**, **Spring Boot**, **Spring Cloud AWS** e **LocalStack**.

A aplicacao expoe um endpoint HTTP para enviar mensagens para uma fila SQS local. Depois, um consumer escuta essa fila e processa a mensagem usando uma camada de service.

---

## Objetivo

Este projeto foi criado para praticar:

- Producer/Consumer Pattern com SQS
- Configuracao local com LocalStack
- `@SqsListener` com Spring Cloud AWS
- `SqsTemplate` para envio de mensagens
- Externalized Configuration com `application.yml`
- Separacao de responsabilidades entre controller, producer, consumer e service
- Principios SOLID aplicados em um exemplo pequeno

---

## Tecnologias

| Tecnologia | Uso |
|---|---|
| Java 21 | Linguagem principal |
| Spring Boot 4.0.6 | Base da aplicacao |
| Spring Web MVC | Endpoint REST |
| Spring Cloud AWS 4.0.2 | Integracao com SQS |
| AWS SDK v2 | Client SQS usado pelo Spring Cloud AWS |
| LocalStack | Ambiente AWS local |
| Maven Wrapper | Build sem Maven instalado globalmente |

---

## Arquitetura

```text
Postman / Cliente HTTP
        |
        v
MessageController
        |
        v
MessageProducer (interface)
        |
        v
SqsMessageProducer
        |
        v
LocalStack SQS: minhafila
        |
        v
MyConsumer
        |
        v
MessageService
        |
        v
Log da aplicacao
```

### Responsabilidades

| Classe | Responsabilidade |
|---|---|
| `MessageController` | Recebe requisicoes HTTP |
| `MessageProducer` | Define o contrato de envio de mensagem |
| `SqsMessageProducer` | Implementa o envio usando `SqsTemplate` |
| `MyConsumer` | Escuta mensagens da fila com `@SqsListener` |
| `MessageService` | Processa e valida a mensagem recebida |
| `SqsConfig` | Configura o client SQS para LocalStack |
| `MessageRequest` | Representa o contrato da mensagem |

---

## Principios E Padroes Aplicados

### Dependency Inversion Principle

O controller depende da interface `MessageProducer`, nao da implementacao concreta `SqsMessageProducer`.

Isso permite trocar a tecnologia de mensageria no futuro sem alterar a entrada HTTP.

```text
MessageController -> MessageProducer -> SqsMessageProducer
```

### Single Responsibility Principle

Cada classe tem uma responsabilidade clara:

- Controller recebe HTTP
- Producer envia para SQS
- Consumer escuta SQS
- Service processa regra simples
- Config configura infraestrutura

### Producer/Consumer Pattern

A aplicacao envia mensagens para uma fila e tambem consome mensagens dessa fila.

Esse padrao desacopla quem produz eventos de quem processa eventos.

---

## Pre-requisitos

Antes de executar, tenha instalado:

- Java 21+
- Docker
- Postman, Insomnia ou `curl`

O projeto usa Maven Wrapper, entao nao e necessario instalar Maven manualmente.

---

## Subindo O LocalStack

Execute o LocalStack com suporte a SQS:

```bash
docker run --rm -it \
  -p 4566:4566 \
  -e SERVICES=sqs \
  -e DEFAULT_REGION=sa-east-1 \
  localstack/localstack
```

Em outro terminal, crie a fila:

```bash
aws --endpoint-url=http://localhost:4566 \
  sqs create-queue \
  --queue-name minhafila \
  --region sa-east-1
```

Se voce nao tiver AWS CLI configurado, use credenciais fake:

```bash
export AWS_ACCESS_KEY_ID=test
export AWS_SECRET_ACCESS_KEY=test
export AWS_DEFAULT_REGION=sa-east-1
```

Para listar as filas:

```bash
aws --endpoint-url=http://localhost:4566 \
  sqs list-queues \
  --region sa-east-1
```

---

## Configuracao

Arquivo principal:

```text
src/main/resources/application.yml
```

Configuracao atual:

```yaml
spring:
  application:
    name: sqs
  output:
    ansi:
      enabled: always

app:
  aws:
    sqs:
      queue-name: minhafila
      endpoint: http://localhost:4566
      region: sa-east-1
      access-key: test
      secret-key: test
```

As credenciais `test/test` sao usadas porque o LocalStack exige que o SDK receba credenciais, mas nao valida credenciais reais da AWS.

---

## Executando A Aplicacao

Na raiz do projeto:

```bash
./mvnw spring-boot:run
```

A aplicacao sobe por padrao em:

```text
http://localhost:8080
```

Importante:

- `8080` e a porta da aplicacao Spring Boot
- `4566` e a porta do LocalStack

---

## Enviando Uma Mensagem

Endpoint:

```http
POST http://localhost:8080/messages
```

Body:

```json
{
  "content": "mensagem 1"
}
```

Exemplo com `curl`:

```bash
curl -X POST http://localhost:8080/messages \
  -H "Content-Type: application/json" \
  -d '{"content": "mensagem 1"}'
```

Resposta esperada:

```http
202 Accepted
```

Log esperado:

```text
Processing message content: mensagem 1
```

---

## Fluxo Da Mensagem

1. O cliente envia `POST /messages`.
2. `MessageController` recebe o JSON.
3. `MessageProducer` abstrai o envio.
4. `SqsMessageProducer` envia a mensagem para a fila `minhafila`.
5. `MyConsumer` escuta a fila com `@SqsListener`.
6. `MessageService` valida e processa a mensagem.
7. A aplicacao registra o processamento no log.

---

## Validacao Da Mensagem

O service rejeita mensagens nulas, vazias ou em branco.

Exemplo invalido:

```json
{
  "content": ""
}
```

Nesse caso, a aplicacao lanca `MessageIsEmptyException`.

Esse comportamento e intencional para praticar validacao no service em vez de deixar a regra espalhada pelo controller ou consumer.

---

## Testes

Execute:

```bash
./mvnw test
```

Observacao:

O teste atual sobe o contexto Spring. Como existe um `@SqsListener`, o LocalStack precisa estar disponivel em `localhost:4566`.

Uma melhoria futura e adicionar testes unitarios para `MessageService`, sem depender de Spring ou LocalStack.

---

## Estrutura Do Projeto

```text
src
└── main
    ├── java
    │   └── tech/souza/sqs
    │       ├── config
    │       │   └── SqsConfig.java
    │       ├── consumer
    │       │   └── MyConsumer.java
    │       ├── controller
    │       │   └── MessageController.java
    │       ├── dto
    │       │   └── MessageRequest.java
    │       ├── exceptions
    │       │   └── MessageIsEmptyException.java
    │       ├── producer
    │       │   └── MessageProducer.java
    │       ├── service
    │       │   ├── MessageService.java
    │       │   └── SqsMessageProducer.java
    │       └── SqsApplication.java
    └── resources
        └── application.yml
```

---

## Aprendizados Principais

- Uma fila SQS desacopla produtor e consumidor.
- `@SqsListener` precisa estar em uma classe gerenciada pelo Spring.
- LocalStack permite praticar AWS localmente sem custo.
- `SqsTemplate` simplifica o envio de mensagens.
- Configuracoes de ambiente devem ficar fora do codigo.
- Interfaces ajudam a reduzir acoplamento e melhoram testabilidade.
- Services devem concentrar regras de negocio simples.

---

## Proximos Passos De Estudo

- Criar uma Dead Letter Queue (DLQ)
- Configurar retry e visibility timeout
- Adicionar testes unitarios para `MessageService`
- Adicionar testes de integracao com Testcontainers + LocalStack
- Criar profiles `local` e `test`
- Adicionar validacao com Bean Validation
- Documentar endpoint com OpenAPI/Swagger
- Simular falhas no consumer e observar reprocessamento

---

## Comandos Uteis

Listar filas:

```bash
aws --endpoint-url=http://localhost:4566 sqs list-queues --region sa-east-1
```

Enviar mensagem direto pela AWS CLI:

```bash
aws --endpoint-url=http://localhost:4566 \
  sqs send-message \
  --queue-url http://sqs.sa-east-1.localhost.localstack.cloud:4566/000000000000/minhafila \
  --message-body '{"content": "mensagem via aws cli"}' \
  --region sa-east-1
```

Receber mensagem direto pela AWS CLI:

```bash
aws --endpoint-url=http://localhost:4566 \
  sqs receive-message \
  --queue-url http://sqs.sa-east-1.localhost.localstack.cloud:4566/000000000000/minhafila \
  --region sa-east-1
```

---

## Licenca

Projeto criado para fins de estudo e pratica.
