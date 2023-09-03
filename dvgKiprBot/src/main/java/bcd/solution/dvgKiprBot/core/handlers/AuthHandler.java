package bcd.solution.dvgKiprBot.core.handlers;

import bcd.solution.dvgKiprBot.DvgKiprBot;
import bcd.solution.dvgKiprBot.core.models.StateMachine;
import bcd.solution.dvgKiprBot.core.services.*;
import lombok.SneakyThrows;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.pinnedmessages.PinChatMessage;
import org.telegram.telegrambots.meta.api.methods.pinnedmessages.UnpinAllChatMessages;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.*;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;


@Component
public class AuthHandler {

    private final StateMachineService stateMachineService;
    private final MediaService mediaService;
    private final KeyboardService keyboardService;
    private final UserService userService;
    private final AuthorizationService authorizationService;
    private final CommandsHandler commandsHandler;

    public AuthHandler(StateMachineService stateMachineService,
                       MediaService mediaService,
                       KeyboardService keyboardService,
                       UserService userService,
                       AuthorizationService authorizationService,
                       CommandsHandler commandsHandler) {
        this.stateMachineService = stateMachineService;
        this.mediaService = mediaService;
        this.keyboardService = keyboardService;
        this.userService = userService;
        this.authorizationService = authorizationService;
        this.commandsHandler = commandsHandler;
    }

    @Async
    @SneakyThrows
    public void handleCallback(CallbackQuery callbackQuery, DvgKiprBot bot) {
        String action = callbackQuery.getData().split("/")[0];
        switch (action) {
            case "auth" -> authHandler(callbackQuery, bot);
            case "auth_cancel" -> cancelHandler(callbackQuery, bot);
            case "auth_getPhone" -> getPhoneHandler(callbackQuery, bot);
            case "auth_phoneCancel" -> phoneCancelHandler(callbackQuery, bot);
        }
    }

    @Async
    @SneakyThrows
    protected void authHandler(CallbackQuery callbackQuery, DvgKiprBot bot) {
        bot.executeAsync(EditMessageMedia.builder()
                .chatId(callbackQuery.getMessage().getChatId())
                .messageId(callbackQuery.getMessage().getMessageId())
                .media(mediaService.getAuthMedia())
                .build()).join();
        if (authorizationService.isAuthorized(callbackQuery.getFrom().getId())) {
            bot.executeAsync(EditMessageCaption.builder()
                    .chatId(callbackQuery.getMessage().getChatId())
                    .messageId(callbackQuery.getMessage().getMessageId())
                    .caption("Вы уже авторизованы")
                    .replyMarkup(keyboardService.getRestartKeyboard())
                    .build()).join();
            bot.executeAsync(UnpinAllChatMessages.builder()
                    .chatId(callbackQuery.getMessage().getChatId())
                    .build()).join();
            bot.executeAsync(PinChatMessage.builder()
                    .chatId(callbackQuery.getMessage().getChatId())
                    .messageId(callbackQuery.getMessage().getMessageId())
                    .build());
            return;
        }

        bot.executeAsync(EditMessageCaption.builder()
                .chatId(callbackQuery.getMessage().getChatId())
                .messageId(callbackQuery.getMessage().getMessageId())
                .caption("Введите пароль")
                .replyMarkup(keyboardService.getAuthCancelKeyboard())
                .build()).join();

        stateMachineService.setWaitPasswordByUserId(
                callbackQuery.getFrom().getId(),
                true,
                callbackQuery.getMessage().getMessageId());
    }

    @Async
    @SneakyThrows
    protected void phoneCancelHandler(CallbackQuery callbackQuery, DvgKiprBot bot) {
        bot.executeAsync(DeleteMessage.builder()
                .chatId(callbackQuery.getMessage().getChatId())
                .messageId(callbackQuery.getMessage().getMessageId())
                .build());
        bot.executeAsync(SendMessage.builder()
                .chatId(callbackQuery.getMessage().getChatId())
                .text("Хорошо, но Вы можете ввести его позже")
                .replyMarkup(new ReplyKeyboardRemove(true))
                .build());

        commandsHandler.startHandler(bot,
                callbackQuery.getFrom().getId(),
                callbackQuery.getMessage().getChatId());
    }

    @Async
    @SneakyThrows
    public void contactHandler(Message message, DvgKiprBot bot) {
        StateMachine stateMachine = stateMachineService.getByUserId(message.getFrom().getId());
        String phoneNumber = message.getContact().getPhoneNumber();

        userService.setPhoneById(message.getFrom().getId(), phoneNumber);

        bot.executeAsync(DeleteMessage.builder()
                .chatId(message.getChatId())
                .messageId(stateMachine.phoneMessageId)
                .build());
        bot.executeAsync(DeleteMessage.builder()
                .chatId(message.getChatId())
                .messageId(message.getMessageId())
                .build());
        bot.executeAsync(SendMessage.builder()
                .chatId(message.getChatId())
                .text("Спасибо, что предоставили Ваш номер телефона!"
//                        + " Теперь Вам доступен конструктор туров"
                )
                .replyMarkup(new ReplyKeyboardRemove(true))
                .build());
        stateMachineService.setWaitPhoneByUserId(
                message.getFrom().getId(),
                false, 0);
        commandsHandler.startHandler(bot, message.getFrom().getId(), message.getChatId());
    }

    @Async
    @SneakyThrows
    protected void getPhoneHandler(CallbackQuery callbackQuery, DvgKiprBot bot) {
        bot.executeAsync(EditMessageCaption.builder()
                .chatId(callbackQuery.getMessage().getChatId())
                .messageId(callbackQuery.getMessage().getMessageId())
                .replyMarkup(null)
                .build());
        bot.executeAsync(SendMessage.builder()
                .chatId(callbackQuery.getMessage().getChatId())
                .text("Пожалуйста, отправьте свой контакт.")
                .replyMarkup(keyboardService.getPhoneKeyboard())
                .build());
        Message message = bot.executeAsync(SendMessage.builder()
                .chatId(callbackQuery.getMessage().getChatId())
                .text("Для этого воспользуйтесь клавиатурой")
                .replyMarkup(keyboardService.getPhoneCancelKeyboard())
                .build()).join();
        stateMachineService.setWaitPhoneByUserId(
                callbackQuery.getFrom().getId(),
                true,
                message.getMessageId());

        bot.executeAsync(AnswerCallbackQuery.builder()
                .callbackQueryId(callbackQuery.getId())
                .build());
    }

    @Async
    @SneakyThrows
    public void passwordHandler(Message message, DvgKiprBot bot, StateMachine stateMachine) {
        String password = message.getText();

        if (!authorizationService.authByPassword(message.getFrom().getId(), password)) {
            try {
                bot.execute(EditMessageCaption.builder()
                        .chatId(message.getChatId())
                        .messageId(stateMachine.auth_message_id)
                        .caption("Пароль не найден, попробуйте снова")
                        .replyMarkup(keyboardService.getAuthCancelKeyboard())
                        .build());
            } catch (TelegramApiRequestException ignored) {

            }

        } else {
            bot.executeAsync(EditMessageCaption.builder()
                    .chatId(message.getChatId())
                    .messageId(stateMachine.auth_message_id)
                    .caption("Вы авторизованы!")
                    .replyMarkup(keyboardService.getRestartKeyboard())
                    .build()).join();

            bot.executeAsync(PinChatMessage.builder()
                    .chatId(message.getChatId())
                    .messageId(stateMachine.auth_message_id)
                    .disableNotification(true)
                    .build());
            stateMachineService.setWaitPasswordByUserId(message.getFrom().getId(), false, 0);
        }
        bot.executeAsync(DeleteMessage.builder()
                .chatId(message.getChatId())
                .messageId(message.getMessageId())
                .build());
//        commandsHandler.choosingMessageSender(
//                message.getChatId(),
//                bot, userService.hasPhoneById(message.getFrom().getId()));
    }

    @Async
    @SneakyThrows
    protected void cancelHandler(CallbackQuery callbackQuery, DvgKiprBot bot) {
        bot.executeAsync(DeleteMessage.builder()
                .chatId(callbackQuery.getMessage().getChatId())
                .messageId(callbackQuery.getMessage().getMessageId())
                .build());

        stateMachineService.setWaitPasswordByUserId(callbackQuery.getFrom().getId(), false, 0);
        commandsHandler.startHandler(bot,
                callbackQuery.getFrom().getId(),
                callbackQuery.getMessage().getChatId());
        bot.executeAsync(AnswerCallbackQuery.builder()
                .callbackQueryId(callbackQuery.getId())
                .text("Ввод пароля отменен")
                .build());
    }

}
