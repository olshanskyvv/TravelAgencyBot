package bcd.solution.dvgKiprBot;

import bcd.solution.dvgKiprBot.core.handlers.CallbackQueryHandler;
import bcd.solution.dvgKiprBot.core.handlers.MessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;


@Component
public class DvgKiprBot extends TelegramLongPollingBot {

    private final MessageHandler messageHandler;
    private final CallbackQueryHandler callbackQueryHandler;

    private final String botName;
    private final String botToken;

    private final Logger log = LoggerFactory.getLogger(DvgKiprBot.class);


    public DvgKiprBot(@Value("${bot.name}") String botUsername,
                      @Value("${bot.token}") String botToken,
                      MessageHandler messageHandler,
                      CallbackQueryHandler callbackQueryHandler) throws TelegramApiException {
        this.botName = botUsername;
        this.botToken = botToken;

        this.messageHandler = messageHandler;
        this.callbackQueryHandler = callbackQueryHandler;
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        telegramBotsApi.registerBot(this);

        this.executeAsync(SetMyCommands.builder()
                        .command(BotCommand.builder()
                                .command("/start")
                                .description("Запуск бота")
                                .build())
                .build());


        log.info("Info log");
        log.debug("Debug log");
        log.error("Error log");
        log.warn("Warn log");

    }

    @Async
    @Override
    public void onUpdateReceived(Update update) {

        //TODO check some issues with locks
        if (update.hasMessage() && update.getMessage() != null) {
            Message message = update.getMessage();

            // Create a new thread to handle the message asynchronously
//            Thread messageHandlerThread = new Thread(() -> {
                messageHandler.handleMessage(message, this);
//            });
//            messageHandlerThread.start();
        }
        if (update.hasCallbackQuery() && update.getCallbackQuery() != null) {
            CallbackQuery callbackQuery = update.getCallbackQuery();

            // Create a new thread to handle the callback query asynchronously
//            Thread callbackQueryHandlerThread = new Thread(() -> {
                callbackQueryHandler.handleQuery(callbackQuery, this);
//            });
//            callbackQueryHandlerThread.start();
        }
    }

    @Override
    public String getBotUsername() {
        return this.botName;
    }

    @Override
    public String getBotToken() {
        return this.botToken;
    }
}
