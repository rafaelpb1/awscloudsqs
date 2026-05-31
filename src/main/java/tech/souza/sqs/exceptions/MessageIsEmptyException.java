package tech.souza.sqs.exceptions;

public class MessageIsEmptyException extends RuntimeException {
    public MessageIsEmptyException(String message) {
        super(message);
    }
}
