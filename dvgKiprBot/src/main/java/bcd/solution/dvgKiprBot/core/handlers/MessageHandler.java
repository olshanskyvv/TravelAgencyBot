package bcd.solution.dvgKiprBot.core.handlers;

import bcd.solution.dvgKiprBot.DvgKiprBot;
import bcd.solution.dvgKiprBot.core.models.StateMachine;
import bcd.solution.dvgKiprBot.core.services.StateMachineService;
import lombok.SneakyThrows;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;


import java.util.List;
import java.util.Optional;

@Component
public class MessageHandler {

    private final CommandsHandler commandsHandler;
    private final AuthHandler authHandler;
    private final StateMachineService stateMachineService;

    public MessageHandler(CommandsHandler commandsHandler,
                          AuthHandler authHandler,
                          StateMachineService stateMachineService) {
        this.commandsHandler = commandsHandler;
        this.authHandler = authHandler;
        this.stateMachineService = stateMachineService;
    }

    @Async
    @SneakyThrows
    public void handleMessage(Message message, DvgKiprBot bot) {
        StateMachine stateMachine = stateMachineService.getOrAddIfNodeExist(
                message.getFrom().getId(),
                message.getFrom().getUserName());

        if (message.hasContact() && stateMachine.waitPhone) {
            authHandler.contactHandler(message, bot);
        }

        if (!message.hasText()) {
            return;
        }

        List<MessageEntity> entities = message.getEntities();
        if (!message.hasEntities() || entities.stream().noneMatch(e -> "bot_command".equals(e.getType()))) {
            if (stateMachine.wait_password) {
                authHandler.passwordHandler(message, bot, stateMachine);
            }
            return;
        }

        Optional<MessageEntity> commandEntity =
                entities.stream().filter(e -> "bot_command".equals(e.getType())).findFirst();
        if (commandEntity.isPresent()) {
            String command = message.getText().substring(
                    commandEntity.get().getOffset(),
                    commandEntity.get().getLength());
            switch (command) {
                case "/start" -> commandsHandler.startHandler(bot, message.getFrom().getId(), message.getChatId());
                default -> bot.executeAsync(SendMessage.builder()
                        .text("Команда не найдена")
                        .chatId(message.getChatId())
                        .build());
            }

        }
    }
}
