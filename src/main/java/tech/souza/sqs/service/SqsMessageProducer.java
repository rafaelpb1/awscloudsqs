package tech.souza.sqs.service;

import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tech.souza.sqs.dto.MessageRequest;
import tech.souza.sqs.producer.MessageProducer;

@Service
public class SqsMessageProducer implements MessageProducer {

    private final SqsTemplate sqsTemplate;
    private final String queueName;

    public SqsMessageProducer(SqsTemplate sqsTemplate,
                              @Value("${app.aws.sqs.queue-name}") String queueName) {
        this.sqsTemplate = sqsTemplate;
        this.queueName = queueName;
    }

    @Override
    public void send(MessageRequest message) {
        sqsTemplate.send(queueName, message);
    }
}
