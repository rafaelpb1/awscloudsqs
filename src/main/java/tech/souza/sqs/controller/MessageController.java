package tech.souza.sqs.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.souza.sqs.dto.MessageRequest;
import tech.souza.sqs.producer.MessageProducer;

@RestController
@RequestMapping("/messages")
public class MessageController {

    private final MessageProducer messageProducer;

    public MessageController(MessageProducer messageProducer) {
        this.messageProducer = messageProducer;
    }

    @PostMapping
    public ResponseEntity<Void> send(@RequestBody MessageRequest message) {
        messageProducer.send(message);
        return ResponseEntity.accepted().build();
    }
}
