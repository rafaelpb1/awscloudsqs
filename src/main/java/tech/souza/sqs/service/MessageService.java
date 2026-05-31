package tech.souza.sqs.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tech.souza.sqs.dto.MessageRequest;
import tech.souza.sqs.exceptions.MessageIsEmptyException;

@Service
public class MessageService {

    private static final Logger log = LoggerFactory.getLogger(MessageService.class);

    public void process(MessageRequest message) {
        if (message == null || message.content() == null || message.content().isBlank()) {
            throw new MessageIsEmptyException("Message content is required");
        }

        log.info("Processing message content: {}", message.content());
    }
}
