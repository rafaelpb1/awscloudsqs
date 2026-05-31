package tech.souza.sqs.consumer;

import io.awspring.cloud.sqs.annotation.SqsListener;
import org.springframework.stereotype.Component;
import tech.souza.sqs.dto.MessageRequest;
import tech.souza.sqs.service.MessageService;

@Component
public class MyConsumer {

    private final MessageService messageService;

    public MyConsumer(MessageService messageService) {
        this.messageService = messageService;
    }

    @SqsListener("${app.aws.sqs.queue-name}")
    public void listen(MessageRequest message) {
        messageService.process(message);
    }
}
