package tech.souza.sqs.producer;

import tech.souza.sqs.dto.MessageRequest;


public interface MessageProducer {
    void send(MessageRequest message);
}
