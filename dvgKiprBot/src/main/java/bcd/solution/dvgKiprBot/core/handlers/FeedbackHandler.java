package bcd.solution.dvgKiprBot.core.handlers;

import bcd.solution.dvgKiprBot.DvgKiprBot;
import bcd.solution.dvgKiprBot.core.models.StateMachine;
import bcd.solution.dvgKiprBot.core.services.CardService;
import bcd.solution.dvgKiprBot.core.services.KeyboardService;
import bcd.solution.dvgKiprBot.core.services.MediaService;
import bcd.solution.dvgKiprBot.core.services.StateMachineService;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageCaption;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageMedia;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

import java.util.List;

@Component
public class FeedbackHandler {
    private final StateMachineService stateMachineService;
    private final MediaService mediaService;
    private final KeyboardService keyboardService;
    private final CardService cardService;


    private final Long managerId;
    private final String managerUsername;
    private final List<String> contactPhones;

    public FeedbackHandler(StateMachineService stateMachineService,
                           MediaService mediaService,
                           KeyboardService keyboardService,
                           CardService cardService,
                           @Value("${bot.managerId}") Long managerId,
                           @Value("${bot.managerUsername}") String managerUsername,
                           @Value("${bot.contactPhonesList}") List<String> contactPhones) {
        this.stateMachineService = stateMachineService;
        this.mediaService = mediaService;
        this.keyboardService = keyboardService;
        this.cardService = cardService;

        this.managerId = managerId;
        this.managerUsername = managerUsername;
        this.contactPhones = contactPhones;
    }


    @Async
    @SneakyThrows
    public void feedbackHandler(CallbackQuery callbackQuery, DvgKiprBot bot) {
        StateMachine stateMachine = stateMachineService.getByUserId(callbackQuery.getFrom().getId());
        String managerTourCard = cardService.getManagerCard(stateMachine);
        bot.executeAsync(SendMessage.builder()
                .chatId(this.managerId)
                .text(managerTourCard)
                .parseMode(ParseMode.MARKDOWN)
                .build());
        String userTourCard = cardService.getUserCard(
                stateMachine,
                this.managerUsername,
                this.contactPhones
        );
        stateMachineService.clearStateByUserId(callbackQuery.getFrom().getId());

        bot.executeAsync(EditMessageMedia.builder()
                .chatId(callbackQuery.getMessage().getChatId())
                .messageId(callbackQuery.getMessage().getMessageId())
                .media(mediaService.getFeedbackMedia())
                .build()).join();
        bot.executeAsync(EditMessageCaption.builder()
                .chatId(callbackQuery.getMessage().getChatId())
                .messageId(callbackQuery.getMessage().getMessageId())
                .caption(userTourCard)
                .parseMode(ParseMode.MARKDOWN)
                .replyMarkup(keyboardService.getRestartKeyboard())
                .build()).join();
        bot.executeAsync(AnswerCallbackQuery.builder()
                .callbackQueryId(callbackQuery.getId()).build());
    }
}
