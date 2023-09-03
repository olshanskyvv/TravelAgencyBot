package bcd.solution.dvgKiprBot.core.handlers.selectHandlers;

import bcd.solution.dvgKiprBot.DvgKiprBot;
import bcd.solution.dvgKiprBot.core.handlers.FeedbackHandler;
import bcd.solution.dvgKiprBot.core.models.CustomTour;
import bcd.solution.dvgKiprBot.core.models.Resort;
import bcd.solution.dvgKiprBot.core.services.*;
import lombok.SneakyThrows;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageCaption;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageMedia;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;

import java.util.List;
import java.util.Optional;

@Component
public class CustomTourHandler {
    private final KeyboardService keyboardService;
    private final MediaService mediaService;
    private final CustomToursService customToursService;
    private final StateMachineService stateMachineService;
    private final CardService cardService;
    private final FeedbackHandler feedbackHandler;

    private final String noTourText = "Авторский тур не найден 🥲, пожалуйста, попрубойте позже!";

    public CustomTourHandler(KeyboardService keyboardService,
                             MediaService mediaService,
                             CustomToursService customToursService,
                             StateMachineService stateMachineService,
                             CardService cardService,
                             FeedbackHandler feedbackHandler) {
        this.keyboardService = keyboardService;
        this.mediaService = mediaService;
        this.customToursService = customToursService;
        this.stateMachineService = stateMachineService;
        this.cardService = cardService;
        this.feedbackHandler = feedbackHandler;
    }

    @Async
    @SneakyThrows
    public void handleCustomTourCallback(CallbackQuery callbackQuery, DvgKiprBot bot) {
//        Look comments in activity handler class
        String action = callbackQuery.getData().split("/")[0];
        switch (action) {
            case "customTours" -> defaultHandler(callbackQuery, bot);
            case "customTours_select" -> selectHandler(callbackQuery, bot);
            case "customTours_card" -> cardHandler(callbackQuery, bot);
            case "customTours_change" -> changeHandler(callbackQuery, bot);
            case "customTours_media" -> mediaHandler(callbackQuery, bot);
        }
    }

    @Async
    @SneakyThrows
    protected void cardHandler(CallbackQuery callbackQuery, DvgKiprBot bot) {
        Long customTourId = Long.parseLong(callbackQuery.getData().split("/")[1]);
        Optional<CustomTour> selectedTour = customToursService.getById(customTourId);
        if (selectedTour.isEmpty()) {
            bot.executeAsync(AnswerCallbackQuery.builder()
                    .callbackQueryId(callbackQuery.getId())
                    .showAlert(true).text(noTourText)
                    .build());
            return;
        }

        bot.executeAsync(SendMessage.builder()
                .chatId(callbackQuery.getMessage().getChatId())
                .text(cardService.getCustomTourCard(selectedTour.get(), true))
                .parseMode(ParseMode.MARKDOWN)
                .replyMarkup(keyboardService.getDeleteKeyboard())
                .build());
        bot.executeAsync(AnswerCallbackQuery.builder()
                .callbackQueryId(callbackQuery.getId())
                .build());
    }

    @Async
    @SneakyThrows
    protected void mediaHandler(CallbackQuery callbackQuery, DvgKiprBot bot) {
        String[] dataArray = callbackQuery.getData().split("/");
        Integer index = Integer.parseInt(dataArray[1]);
        Long customTourId = Long.parseLong(dataArray[2]);

        CustomTour customTour = customToursService.getByIndex(index);
        List<List<InputMedia>> allMedias;
        try {
            allMedias = mediaService.getCustomTourMedias(customTour);
        } catch (Exception e) {
            bot.executeAsync(AnswerCallbackQuery.builder()
                    .callbackQueryId(callbackQuery.getId())
                    .text("Дополнительных фото нет")
                    .showAlert(true)
                    .build());
            return;
        }

        if (allMedias.isEmpty()) {
            bot.executeAsync(AnswerCallbackQuery.builder()
                    .callbackQueryId(callbackQuery.getId())
                    .text("Дополнительных фото нет")
                    .showAlert(true)
                    .build());
            return;
        }

        for (List<InputMedia> medias : allMedias) {
            if (medias.size() > 1) {
                bot.executeAsync(SendMediaGroup.builder()
                        .chatId(callbackQuery.getFrom().getId())
                        .medias(medias)
                        .build());
            } else {
                InputFile file = new InputFile(
                        medias.get(0).getNewMediaStream(),
                        medias.get(0).getMediaName());
                bot.executeAsync(SendPhoto.builder()
                        .chatId(callbackQuery.getFrom().getId())
                        .photo(file)
                        .build());
            }
        }

        bot.executeAsync(DeleteMessage.builder()
                .chatId(callbackQuery.getMessage().getChatId())
                .messageId(callbackQuery.getMessage().getMessageId())
                .build());
        bot.executeAsync(SendPhoto.builder()
                .chatId(callbackQuery.getFrom().getId())
                .photo(mediaService.getCustomTourFile(customTour))
                .caption(cardService.getCustomTourCard(customTour, false))
                .replyMarkup(keyboardService.getCustomToursKeyboard(index, customTourId))
                .parseMode(ParseMode.MARKDOWN)
                .build());
        bot.executeAsync(AnswerCallbackQuery.builder()
                .callbackQueryId(callbackQuery.getId())
                .build());
    }

    @Async
    @SneakyThrows
    protected void changeHandler(CallbackQuery callbackQuery, DvgKiprBot bot) {
        Integer index = Integer.parseInt(callbackQuery.getData().split("/")[1]);

        CustomTour currentTour = customToursService.getByIndex(index);

        bot.executeAsync(EditMessageMedia.builder()
                .chatId(callbackQuery.getMessage().getChatId())
                .messageId(callbackQuery.getMessage().getMessageId())
                .media(mediaService.getCustomTourMedia(currentTour))
                .build()).join();
        bot.executeAsync(EditMessageCaption.builder()
                .chatId(callbackQuery.getMessage().getChatId())
                .messageId(callbackQuery.getMessage().getMessageId())
                .caption(cardService.getCustomTourCard(currentTour, false))
                .parseMode(ParseMode.MARKDOWN)
                .replyMarkup(keyboardService.getCustomToursKeyboard(index, currentTour.getId()))
                .build()).join();
        bot.executeAsync(AnswerCallbackQuery.builder()
                .callbackQueryId(callbackQuery.getId()).build());

    }

    @Async
    @SneakyThrows
    protected void selectHandler(CallbackQuery callbackQuery, DvgKiprBot bot) {
        Long customTourId = Long.parseLong(callbackQuery.getData().split("/")[1]);

        Optional<CustomTour> selectedCustomTour = customToursService.getById(customTourId);
        if (selectedCustomTour.isEmpty()) {
            bot.executeAsync(AnswerCallbackQuery.builder()
                    .callbackQueryId(callbackQuery.getId())
                    .showAlert(true).text("Авторский тур не найден, попробуйте позже")
                    .build());
            return;
        }

        stateMachineService.setCustomTourByUserId(selectedCustomTour.get(), callbackQuery.getFrom().getId());

        feedbackHandler.feedbackHandler(callbackQuery, bot);
    }

    @Async
    @SneakyThrows
    public void defaultHandler(CallbackQuery callbackQuery, DvgKiprBot bot) {
        CustomTour currentTour = customToursService.getByIndex(0);

        bot.executeAsync(EditMessageMedia.builder()
                .chatId(callbackQuery.getMessage().getChatId())
                .messageId(callbackQuery.getMessage().getMessageId())
                .media(mediaService.getCustomTourMedia(currentTour))
                .build()).join();
        bot.executeAsync(EditMessageCaption.builder()
                .chatId(callbackQuery.getMessage().getChatId())
                .messageId(callbackQuery.getMessage().getMessageId())
                .caption(cardService.getCustomTourCard(currentTour, false))
                .parseMode(ParseMode.MARKDOWN)
                .replyMarkup(keyboardService.getCustomToursKeyboard(0, currentTour.getId()))
                .build()).join();
        bot.executeAsync(AnswerCallbackQuery.builder()
                .callbackQueryId(callbackQuery.getId()).build());
    }
}
