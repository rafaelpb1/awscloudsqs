# SQS LocalStack Lab

![Java](https://img.shields.io/badge/Java-21-007396?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.6-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![AWS SQS](https://img.shields.io/badge/AWS%20SQS-LocalStack-FF9900?style=for-the-badge&logo=amazonaws&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-Wrapper-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)

Projeto de estudo para praticar mensageria com **Amazon SQS**, **Spring Boot**, **Spring Cloud AWS** e **LocalStack**.

A aplicação expõe um endpoint HTTP para enviar mensagens para uma fila SQS local. Depois, um consumer escuta essa fila e processa a mensagem usando uma camada de service.

---

## Objetivo

Este projeto foi criado para praticar:

- Producer/Consumer Pattern com SQS
- Configuração local com LocalStack
- `@SqsListener` com Spring Cloud AWS
- `SqsTemplate` para envio de mensagens
- Externalized Configuration com `application.yml`
- Separação de responsabilidades entre controller, producer, consumer e service
- Princípios SOLID aplicados em um exemplo pequeno

---

## Tecnologias

| Tecnologia | Uso |
|---|---|
| Java 21 | Linguagem principal |
| Spring Boot 4.0.6 | Base da aplicação |
| Spring Web MVC | Endpoint REST |
| Spring Cloud AWS 4.0.2 | Integração com SQS |
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
Log da aplicação
```

### Responsabilidades

| Classe | Responsabilidade |
|---|---|
| `MessageController` | Recebe requisições HTTP |
| `MessageProducer` | Define o contrato de envio de mensagem |
| `SqsMessageProducer` | Implementa o envio usando `SqsTemplate` |
| `MyConsumer` | Escuta mensagens da fila com `@SqsListener` |
| `MessageService` | Processa e valida a mensagem recebida |
| `SqsConfig` | Configura o client SQS para LocalStack |
| `MessageRequest` | Representa o contrato da mensagem |

---

## Princípios e Padrões Aplicados

### Dependency Inversion Principle

O controller depende da interface `MessageProducer`, não da implementação concreta `SqsMessageProducer`.

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

A aplicação envia mensagens para uma fila e também consome mensagens dessa fila.

Esse padrão desacopla quem produz eventos de quem processa eventos.

---

## Pré-requisitos

Antes de executar, tenha instalado:

- Java 21+
- Docker
- Postman, Insomnia ou `curl`

O projeto usa Maven Wrapper, então não é necessário instalar Maven manualmente.

---

## Subindo o LocalStack

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

Se você não tiver AWS CLI configurado, use credenciais fake:

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

## Configuração

Arquivo principal:

```text
src/main/resources/application.yml
```

Configuração atual:

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

As credenciais `test/test` são usadas porque o LocalStack exige que o SDK receba credenciais, mas não valida credenciais reais da AWS.

---

## Executando a Aplicação

Na raiz do projeto:

```bash
./mvnw spring-boot:run
```

A aplicação sobe por padrão em:

```text
http://localhost:8080
```

Importante:

- `8080` é a porta da aplicação Spring Boot
- `4566` é a porta do LocalStack

---

## Enviando uma Mensagem

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

## Fluxo da Mensagem

1. O cliente envia `POST /messages`.
2. `MessageController` recebe o JSON.
3. `MessageProducer` abstrai o envio.
4. `SqsMessageProducer` envia a mensagem para a fila `minhafila`.
5. `MyConsumer` escuta a fila com `@SqsListener`.
6. `MessageService` valida e processa a mensagem.
7. A aplicação registra o processamento no log.

---

## Validação da Mensagem

O service rejeita mensagens nulas, vazias ou em branco.

Exemplo inválido:

```json
{
  "content": ""
}
```

Nesse caso, a aplicação lança `MessageIsEmptyException`.

Esse comportamento é intencional para praticar validação no service em vez de deixar a regra espalhada pelo controller ou consumer.

---

## Testes

Execute:

```bash
./mvnw test
```

Observação:

O teste atual sobe o contexto Spring. Como existe um `@SqsListener`, o LocalStack precisa estar disponível em `localhost:4566`.

Uma melhoria futura é adicionar testes unitários para `MessageService`, sem depender de Spring ou LocalStack.

---

## Estrutura do Projeto

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
- Configurações de ambiente devem ficar fora do código.
- Interfaces ajudam a reduzir acoplamento e melhoram testabilidade.
- Services devem concentrar regras de negócio simples.

---

## Próximos Passos de Estudo

- Criar uma Dead Letter Queue (DLQ)
- Configurar retry e visibility timeout
- Adicionar testes unitários para `MessageService`
- Adicionar testes de integração com Testcontainers + LocalStack
- Criar profiles `local` e `test`
- Adicionar validação com Bean Validation
- Documentar endpoint com OpenAPI/Swagger
- Simular falhas no consumer e observar reprocessamento

---

## Comandos Úteis

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

## Licença

Projeto criado para fins de estudo e prática.
