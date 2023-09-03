package bcd.solution.dvgKiprBot.core.handlers.selectHandlers;

import bcd.solution.dvgKiprBot.DvgKiprBot;
import bcd.solution.dvgKiprBot.core.handlers.FeedbackHandler;
import bcd.solution.dvgKiprBot.core.handlers.extensionsHandlers.FavoritesHandler;
import bcd.solution.dvgKiprBot.core.models.Hotel;
import bcd.solution.dvgKiprBot.core.models.Stars;
import bcd.solution.dvgKiprBot.core.models.StateMachine;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.Optional;

@Component
public class HotelHandler {
    private final Logger logger;
    private final KeyboardService keyboardService;
    private final MediaService mediaService;
    private final HotelService hotelService;
    private final StateMachineService stateMachineService;
    private final CardService cardService;
    private final FeedbackHandler feedbackHandler;
    private final String noHotelText = "По Вашим параметрам ничего не найдено 🥲";

    public HotelHandler(KeyboardService keyboardService,
                        MediaService mediaService,
                        HotelService hotelService,
                        StateMachineService stateMachineService,
                        CardService cardService,
                        FeedbackHandler feedbackHandler) {
        this.logger = LoggerFactory.getLogger(FavoritesHandler.class);
        this.keyboardService = keyboardService;
        this.mediaService = mediaService;
        this.hotelService = hotelService;
        this.stateMachineService = stateMachineService;
        this.cardService = cardService;
        this.feedbackHandler = feedbackHandler;
    }

    @Async
    @SneakyThrows
    public void handleHotelCallback(CallbackQuery callbackQuery, DvgKiprBot bot) {
//        Look comments in activity handler class
        String action = callbackQuery.getData().split("/")[0];
        switch (action) {
            case "hotels" -> defaultHandler(callbackQuery, bot);
            case "hotels_card" -> cardHandler(callbackQuery, bot);
            case "hotels_stars" -> starsHandler(callbackQuery, bot);
            case "hotels_select" -> selectHandler(callbackQuery, bot);
            case "hotels_change" -> changeHandler(callbackQuery, bot);
            case "hotels_media" -> mediaHandler(callbackQuery, bot);
        }
    }


    @Async
    @SneakyThrows
    public void defaultHandler(CallbackQuery callbackQuery, DvgKiprBot bot) {
        bot.executeAsync(EditMessageCaption.builder()
                .chatId(callbackQuery.getMessage().getChatId())
                .messageId(callbackQuery.getMessage().getMessageId())
                .caption("Выберите звёздность отеля")
                .replyMarkup(keyboardService.getHotelsStarsKeyboard())
                .build());
    }

    @Async
    @SneakyThrows
    protected void cardHandler(CallbackQuery callbackQuery, DvgKiprBot bot) {
        Long hotelId = Long.parseLong(callbackQuery.getData().split("/")[1]);
        Optional<Hotel> selectedHotel = hotelService.getById(hotelId);
        if (selectedHotel.isEmpty()) {
            bot.executeAsync(AnswerCallbackQuery.builder()
                    .callbackQueryId(callbackQuery.getId())
                    .showAlert(true).text(noHotelText)
                    .build());
            return;
        }

        bot.executeAsync(SendMessage.builder()
                .chatId(callbackQuery.getMessage().getChatId())
                .text(cardService.getHotelCard(selectedHotel.get(), true))
                .parseMode(ParseMode.MARKDOWN)
                .replyMarkup(keyboardService.getDeleteKeyboard())
                .build());
        bot.executeAsync(AnswerCallbackQuery.builder()
                .callbackQueryId(callbackQuery.getId())
                .build());

    }

    @Async
    @SneakyThrows
    protected void starsHandler(CallbackQuery callbackQuery, DvgKiprBot bot) {

        Stars stars;
        if (callbackQuery.getData().endsWith("noMatter")) {
            stars = null;
        } else {
            stars = Stars.valueOf(callbackQuery.getData().split("/")[1]);
        }

        StateMachine usersState = stateMachineService.setStarsByUserId(callbackQuery.getFrom().getId(), stars);


        List<Hotel> currentHotels = hotelService.findByResortAndActivitiesAndStars(
                usersState.resort,
                usersState.activities,
                stars);

        if (currentHotels.isEmpty()) {
            bot.executeAsync(AnswerCallbackQuery.builder()
                    .callbackQueryId(callbackQuery.getId())
                    .showAlert(true).text(noHotelText)
                    .build());
            return;
        }

        bot.executeAsync(EditMessageMedia.builder()
                .chatId(callbackQuery.getMessage().getChatId())
                .messageId(callbackQuery.getMessage().getMessageId())
                .media(mediaService.getHotelMedia(currentHotels.get(0)))
                .build()).join();
        bot.executeAsync(EditMessageCaption.builder()
                .chatId(callbackQuery.getMessage().getChatId())
                .messageId(callbackQuery.getMessage().getMessageId())
                .caption(cardService.getHotelCard(currentHotels.get(0), false))
                .parseMode(ParseMode.MARKDOWN)
                .replyMarkup(keyboardService.getHotelsKeyboard(
                        0,
                        currentHotels.get(0).getId(),
                        currentHotels.size()))
                .build());
        bot.executeAsync(AnswerCallbackQuery.builder()
                .callbackQueryId(callbackQuery.getId()).build());

    }

    @Async
    @SneakyThrows
    protected void mediaHandler(CallbackQuery callbackQuery, DvgKiprBot bot) {
        String[] dataArray = callbackQuery.getData().split("/");
        int index = Integer.parseInt(dataArray[1]);
        Long hotelId = Long.parseLong(dataArray[2]);

        StateMachine stateMachine = stateMachineService.getByUserId(callbackQuery.getFrom().getId());
        List<Hotel> currentHotels = hotelService.findByResortAndActivitiesAndStars(
                stateMachine.resort, stateMachine.activities, stateMachine.stars);
        List<List<InputMedia>> allMedias;
        try {
            allMedias = mediaService.getHotelMedias(currentHotels.get(index));
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

        bot.executeAsync(
                SendMessage.builder()
                        .chatId(callbackQuery.getFrom().getId())
                        .text("_Отель_ "+currentHotels.get(index).name+" "+currentHotels.get(index).stars)
                        .parseMode(ParseMode.MARKDOWN)
                        .build()
        );
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
                .photo(mediaService.getHotelFile(currentHotels.get(index)))
                .caption(cardService.getHotelCard(currentHotels.get(index), false))
                .parseMode(ParseMode.MARKDOWN)
                .replyMarkup(keyboardService.getHotelsKeyboard(index, hotelId, currentHotels.size()))
                .build());

        bot.executeAsync(AnswerCallbackQuery.builder()
                .callbackQueryId(callbackQuery.getId())
                .build());
    }


    @Async
    @SneakyThrows
    protected void changeHandler(CallbackQuery callbackQuery, DvgKiprBot bot) {
        int index = Integer.parseInt(callbackQuery.getData().split("/")[1]);
        StateMachine usersState = stateMachineService.getByUserId(callbackQuery.getFrom().getId());

        List<Hotel> currentHotels = hotelService.findByResortAndActivitiesAndStars(
                usersState.resort,
                usersState.activities,
                usersState.stars);


        if (currentHotels.isEmpty()) {
            bot.executeAsync(AnswerCallbackQuery.builder()
                    .callbackQueryId(callbackQuery.getId())
                    .showAlert(true).text(noHotelText)
                    .build());
            return;
        }

        bot.executeAsync(EditMessageMedia.builder()
                .chatId(callbackQuery.getMessage().getChatId())
                .messageId(callbackQuery.getMessage().getMessageId())
                .media(mediaService.getHotelMedia(currentHotels.get(index)))
                .build()).join();
        bot.executeAsync(EditMessageCaption.builder()
                .chatId(callbackQuery.getMessage().getChatId())
                .messageId(callbackQuery.getMessage().getMessageId())
                .caption(cardService.getHotelCard(currentHotels.get(index), false))
                .parseMode(ParseMode.MARKDOWN)
                .replyMarkup(keyboardService.getHotelsKeyboard(
                        index,
                        currentHotels.get(index).getId(),
                        currentHotels.size()))
                .build()).join();
        bot.executeAsync(AnswerCallbackQuery.builder()
                .callbackQueryId(callbackQuery.getId()).build());

    }

    @Async
    @SneakyThrows
    protected void selectHandler(CallbackQuery callbackQuery, DvgKiprBot bot) {
        Long hotelId = Long.parseLong(callbackQuery.getData().split("/")[1]);

        Optional<Hotel> selectedHotel = hotelService.getById(hotelId);
        if (selectedHotel.isEmpty()) {
            bot.executeAsync(AnswerCallbackQuery.builder()
                    .callbackQueryId(callbackQuery.getId())
                    .showAlert(true).text(noHotelText)
                    .build());
            return;
        }

        stateMachineService.setHotelByUserId(selectedHotel.get(), callbackQuery.getFrom().getId());

        feedbackHandler.feedbackHandler(callbackQuery, bot);

    }
}
