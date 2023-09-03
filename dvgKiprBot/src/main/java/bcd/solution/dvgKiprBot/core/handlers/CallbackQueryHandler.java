package bcd.solution.dvgKiprBot.core.handlers;

import bcd.solution.dvgKiprBot.DvgKiprBot;
import bcd.solution.dvgKiprBot.core.handlers.extensionsHandlers.FavoritesHandler;
import bcd.solution.dvgKiprBot.core.models.StateMachine;
import bcd.solution.dvgKiprBot.core.services.*;
import bcd.solution.dvgKiprBot.core.handlers.selectHandlers.ActivityHandler;
import bcd.solution.dvgKiprBot.core.handlers.selectHandlers.CustomTourHandler;
import bcd.solution.dvgKiprBot.core.handlers.selectHandlers.HotelHandler;
import bcd.solution.dvgKiprBot.core.handlers.selectHandlers.ResortHandler;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageCaption;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageMedia;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

@Component
public class CallbackQueryHandler {
    private final Logger logger;
    //Services
    private final KeyboardService keyboardService;
    private final MediaService mediaService;
    private final StateMachineService stateMachineService;
    private final UserService userService;
    //Handlers
    private final AuthHandler authHandler;
    private final ActivityHandler activityHandler;
    private final CustomTourHandler customTourHandler;
    private final HotelHandler hotelHandler;
    private final ResortHandler resortHandler;
    private final CommandsHandler commandsHandler;
    private final FavoritesHandler favoritesHandler;

    @Autowired
    public CallbackQueryHandler(KeyboardService keyboardService,
                                MediaService mediaService,
                                StateMachineService stateMachineService,
                                UserService userService,

                                AuthHandler authHandler,
                                ActivityHandler activityHandler,
                                CustomTourHandler customTourHandler,
                                HotelHandler hotelHandler,
                                ResortHandler resortHandler,
                                CommandsHandler commandsHandler, FavoritesHandler favoritesHandler) {
        this.logger = LoggerFactory.getLogger(FavoritesHandler.class);
        this.keyboardService = keyboardService;
        this.mediaService = mediaService;
        this.stateMachineService = stateMachineService;
        this.userService = userService;

        this.authHandler = authHandler;
        this.activityHandler = activityHandler;
        this.customTourHandler = customTourHandler;
        this.hotelHandler = hotelHandler;
        this.resortHandler = resortHandler;
        this.commandsHandler = commandsHandler;
        this.favoritesHandler = favoritesHandler;
    }


    @Async
    @SneakyThrows
    public void handleQuery(CallbackQuery callbackQuery, DvgKiprBot bot) {
//        Structure of callback data:
//        {action group}[_{action}(optional)]/{current index (by default 0)}[/{current entity id}(optional)]

        String callback_action = callbackQuery.getData().split("_")[0];
        logger.info("callback action is:"+ callback_action);
        switch (callback_action) {
            case "null" -> nothingHandler(callbackQuery, bot);
            case "restart" -> restartHandler(callbackQuery, bot);
            case "start" -> startHandler(callbackQuery, bot);
            case "delete" -> deleteHandler(callbackQuery, bot);
            case "tour" -> tourConstructorHandler(callbackQuery, bot);
            case "select" -> selectHandler(callbackQuery, bot);
            case "auth" -> authHandler.handleCallback(callbackQuery, bot);
            case "resorts" -> resortHandler.handleResortCallback(callbackQuery, bot);
            case "customTours" -> customTourHandler.handleCustomTourCallback(callbackQuery, bot);
            case "activities" -> activityHandler.handleActivityCallback(callbackQuery, bot);
            case "hotels" -> hotelHandler.handleHotelCallback(callbackQuery, bot);
            case "favorite" -> favoritesHandler.defaultHandler(callbackQuery, bot);
            default -> bot.executeAsync(AnswerCallbackQuery.builder()
                    .callbackQueryId(callbackQuery.getId())
                    .text("Здесь пока что ничего нет, но очень скоро появится" + callbackQuery.getData())
                    .showAlert(Boolean.TRUE)
                    .build());
        }
    }

    @Async
    @SneakyThrows
    protected void deleteHandler(CallbackQuery callbackQuery, DvgKiprBot bot) {
        bot.executeAsync(DeleteMessage.builder()
                        .chatId(callbackQuery.getMessage().getChatId())
                        .messageId(callbackQuery.getMessage().getMessageId())
                .build());
        bot.executeAsync(AnswerCallbackQuery.builder()
                .callbackQueryId(callbackQuery.getId())
                .build());
    }

    @Async
    @SneakyThrows
    protected void selectHandler(CallbackQuery callbackQuery, DvgKiprBot bot) {
        String data = callbackQuery.getData();
        String model = data.split("/")[0].split("_")[1];
        StateMachine stateMachine;
        switch (model) {
            case "resort" -> {
                stateMachine = stateMachineService.setResortGotByIdByUserId(callbackQuery.getFrom().getId());
                if (!data.endsWith("noMatter")) {
                    stateMachine = resortHandler.selectHandler(callbackQuery, bot);
                }
                if (stateMachine == null) {
                    return;
                }
                if (!stateMachine.activitiesGot) {
                    activityHandler.defaultHandler(callbackQuery, bot);
                    return;
                }
            }
            case "activity" -> {
                if (data.endsWith("noMatter")) {
                    stateMachineService.clearActivitiesByUserId(callbackQuery.getFrom().getId());
                }
                stateMachine = stateMachineService.setActivityGotByIdByUserId(callbackQuery.getFrom().getId());
                if (stateMachine == null) {
                    return;
                }
                if (!stateMachine.resortGot) {
                    resortHandler.defaultHandler(callbackQuery, bot);
                    return;
                }
            }
        }
        hotelHandler.defaultHandler(callbackQuery, bot);
    }

    @Async
    @SneakyThrows
    protected void tourConstructorHandler(CallbackQuery callbackQuery, DvgKiprBot bot) {
        bot.executeAsync(EditMessageMedia.builder()
                .chatId(callbackQuery.getMessage().getChatId())
                .messageId(callbackQuery.getMessage().getMessageId())
                .media(mediaService.getTourConstructorMedia())
                .build()).join();
        bot.executeAsync(EditMessageCaption.builder()
                .chatId(callbackQuery.getMessage().getChatId())
                .messageId(callbackQuery.getMessage().getMessageId())
                .caption("От чего Вы хотите отталкиваться при подборе отеля?")
                .replyMarkup(keyboardService.getTourConstructorKeyboard())
                .build()).join();
        bot.executeAsync(AnswerCallbackQuery.builder()
                .callbackQueryId(callbackQuery.getId())
                .build());
    }

    @Async
    @SneakyThrows
    protected void nothingHandler(CallbackQuery callbackQuery, DvgKiprBot bot) {
        bot.executeAsync(AnswerCallbackQuery.builder()
                .callbackQueryId(callbackQuery.getId()).build());
    }

    @Async
    @SneakyThrows
    protected void restartHandler(CallbackQuery callbackQuery, DvgKiprBot bot) {
        stateMachineService.clearStateByUserId(callbackQuery.getFrom().getId());

        bot.executeAsync(EditMessageMedia.builder()
                .chatId(callbackQuery.getMessage().getChatId())
                .messageId(callbackQuery.getMessage().getMessageId())
                .media(mediaService.getStartMedia())
                .build()).join();
        bot.executeAsync(EditMessageCaption.builder()
                .chatId(callbackQuery.getMessage().getChatId())
                .messageId(callbackQuery.getMessage().getMessageId())
                .caption(commandsHandler.inviteString)
                .replyMarkup(keyboardService.getTourChoosingKeyboard(
                        userService.hasPhoneById(callbackQuery.getFrom().getId()),
                        userService.isAuthorized(callbackQuery.getFrom().getId())))
                .build()).join();
        bot.executeAsync(AnswerCallbackQuery.builder()
                .callbackQueryId(callbackQuery.getId()).build());
    }

    @Async
    @SneakyThrows
    protected void startHandler(CallbackQuery callbackQuery, DvgKiprBot bot) {
        if (!userService.hasPhoneById(callbackQuery.getFrom().getId())) {
            bot.executeAsync(EditMessageMedia.builder()
                    .chatId(callbackQuery.getMessage().getChatId())
                    .messageId(callbackQuery.getMessage().getMessageId())
                    .media(mediaService.getStartMedia())
                    .build()).join();
            bot.executeAsync(EditMessageCaption.builder()
                    .chatId(callbackQuery.getMessage().getChatId())
                    .messageId(callbackQuery.getMessage().getMessageId())
                    .caption("Для доступа к полному функционалу бота необходимо указать номер телефона."
//                        + " Но Вы все равно можете выбрать один из авторских туров"
                    )
//                    .caption("Для повышения качесва обслуживания нам неоходим Ваш номер телефона")
                    .replyMarkup(keyboardService.getStarterKeyboard())
                    .build()).join();

            bot.executeAsync(AnswerCallbackQuery.builder()
                    .callbackQueryId(callbackQuery.getId()).build());
            return;
        }

        commandsHandler.choosingMessageSender(
                callbackQuery.getMessage().getChatId(),
                bot,
                userService.hasPhoneById(callbackQuery.getFrom().getId()),
                userService.isAuthorized(callbackQuery.getFrom().getId()));
        bot.executeAsync(EditMessageReplyMarkup.builder()
                .chatId(callbackQuery.getMessage().getChatId())
                .messageId(callbackQuery.getMessage().getMessageId())
                .replyMarkup(null)
                .build());
    }
}
