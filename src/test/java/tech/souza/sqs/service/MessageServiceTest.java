package tech.souza.sqs.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import tech.souza.sqs.dto.MessageRequest;
import tech.souza.sqs.exceptions.MessageIsEmptyException;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MessageServiceTest {

    private final MessageService messageService = new MessageService();

    @Test
    void processesMessageWithContent() {
        assertThatCode(() -> messageService.process(new MessageRequest("mensagem 1")))
                .doesNotThrowAnyException();
    }

    @Test
    void throwsWhenMessageIsNull() {
        assertThatThrownBy(() -> messageService.process(null))
                .isInstanceOf(MessageIsEmptyException.class)
                .hasMessage("Message content is required");
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", " ", "\t\n"})
    void throwsWhenContentIsNullEmptyOrBlank(String content) {
        assertThatThrownBy(() -> messageService.process(new MessageRequest(content)))
                .isInstanceOf(MessageIsEmptyException.class)
                .hasMessage("Message content is required");
    }
}
