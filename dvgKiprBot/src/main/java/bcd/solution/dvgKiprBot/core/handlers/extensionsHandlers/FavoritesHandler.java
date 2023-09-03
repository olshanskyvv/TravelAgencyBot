package bcd.solution.dvgKiprBot.core.handlers.extensionsHandlers;

import bcd.solution.dvgKiprBot.DvgKiprBot;
import bcd.solution.dvgKiprBot.core.models.CustomTour;
import bcd.solution.dvgKiprBot.core.models.Hotel;
import bcd.solution.dvgKiprBot.core.models.Resort;
import bcd.solution.dvgKiprBot.core.models.StateMachine;
import bcd.solution.dvgKiprBot.core.services.*;
import javassist.NotFoundException;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

@Component
public class FavoritesHandler {
    private final Logger logger;
    private final StateMachineService stateMachineService;
    private final CardService cardService;
    private final KeyboardService keyboardService;
    private final MediaService mediaService;
    private final HotelService hotelService;
    private final ResortService resortService;
    private final CustomToursService customToursService;


    public FavoritesHandler(
            StateMachineService stateMachineService,
            CardService activityService,
            KeyboardService keyboardService,
            MediaService mediaService,
            HotelService hotelService,
            ResortService resortService,
            CustomToursService customToursService
    ) {
        this.logger = LoggerFactory.getLogger(FavoritesHandler.class);
        this.stateMachineService = stateMachineService;
        this.cardService = activityService;
        this.keyboardService = keyboardService;
        this.mediaService = mediaService;
        this.hotelService = hotelService;
        this.resortService = resortService;
        this.customToursService = customToursService;
    }

    @SneakyThrows
    @Async
    public void defaultHandler(CallbackQuery callbackQuery, DvgKiprBot bot) {

        String[] dataArray = callbackQuery.getData().split("/");
        StateMachine stateMachine = stateMachineService.getByUserId(callbackQuery.getFrom().getId());

        int index = Integer.parseInt(dataArray[1]);
        List<Hotel> currentHotels;
        List<Resort> currentResorts;

        Long someId = Long.parseLong(dataArray[2]);
        Hotel currentHotel;
        CustomTour currentCustomTour;
        Resort currentResort;

        String type = callbackQuery.getData().split("/")[3];
        logger.info("All objects initialized");
              try {
                  switch (type) {
                      case "hotel" -> {
                          logger.info("hotel");
                          currentHotel = hotelService.getById(someId).get();
                          currentHotels = hotelService.findByResortAndActivitiesAndStars(
                                  stateMachine.resort, stateMachine.activities, stateMachine.stars);
                          addHotelToFavorites(callbackQuery, bot, currentHotels, currentHotel, someId, index);
                      }
                      case "resort" -> {
                          logger.info("resort");
                          currentResort = resortService.getById(someId).get();
                          currentResorts = resortService.getByActivities(stateMachine.activities);
                          addResortToFavorites(callbackQuery, bot, currentResorts, currentResort, someId, index);
                      }
                      case "customTour" -> {
                          logger.info("customTour");
                          currentCustomTour = customToursService.getById(someId).get();
                          addCustomTourToFavorites(callbackQuery, bot, currentCustomTour, someId, index);
                      }
                  }
              } catch (Exception e){
                  logger.info(e.getMessage());
              }

    }

    @Async
    @SneakyThrows
    private void addHotelToFavorites(CallbackQuery callbackQuery,
                                     DvgKiprBot bot,
                                     List<Hotel> currentHotels,
                                     Hotel currentHotel,
                                     Long hotelId,
                                     int index) {

        String description = cardService.getFavoriteCard(currentHotel);

        List<List<InputMedia>> allMedias;
        try {
            allMedias = mediaService.getHotelMedias(currentHotel);
            if (!allMedias.isEmpty()) {
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
            } else {
                bot.executeAsync(SendPhoto.builder()
                        .chatId(callbackQuery.getFrom().getId())
                        .photo(mediaService.getHotelFile(currentHotel))
                        .build());
            }
        }  catch (Exception e) {
            logger.error("Unhandled exception in addHotelToFavorites " + e.getMessage());
        }

        bot.executeAsync(
                SendMessage.builder()
                        .chatId(callbackQuery.getFrom().getId())
                        .text(description)
                        .parseMode(ParseMode.MARKDOWN)
                        .build()

        );

        bot.executeAsync(DeleteMessage.builder()
                .chatId(callbackQuery.getMessage().getChatId())
                .messageId(callbackQuery.getMessage().getMessageId())
                .build());
        bot.executeAsync(SendPhoto.builder()
                .chatId(callbackQuery.getFrom().getId())
                .photo(mediaService.getHotelFile(currentHotel))
                .caption(cardService.getHotelCard(currentHotel, false))
                .parseMode(ParseMode.MARKDOWN)
                .replyMarkup(keyboardService.getHotelsKeyboard(index, hotelId, currentHotels.size()))
                .build());
        bot.executeAsync(AnswerCallbackQuery.builder()
                .callbackQueryId(callbackQuery.getId())
                .build());
    }

    @Async
    @SneakyThrows
    private void addResortToFavorites(CallbackQuery callbackQuery,
                                      DvgKiprBot bot,
                                      List<Resort> currentResorts,
                                      Resort currentResort,
                                      Long resortId,
                                      int index) {

        String description = cardService.getFavoriteCard(currentResort);

        List<List<InputMedia>> allMedias;
        try {
            allMedias = mediaService.getResortMedias(currentResort);
            if (!allMedias.isEmpty()) {
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
            } else {
                bot.executeAsync(SendPhoto.builder()
                        .chatId(callbackQuery.getFrom().getId())
                        .photo(mediaService.getResortFile(currentResort))
                        .build());
            }
        } catch (Exception e) {
            logger.error("Unhandled exception in addResortToFavorites " + e.getMessage());
        }

        bot.executeAsync(SendMessage.builder()
                .chatId(callbackQuery.getMessage().getChatId())
                .text(description)
                .parseMode(ParseMode.MARKDOWN)
                .build());

        bot.executeAsync(DeleteMessage.builder()
                .chatId(callbackQuery.getMessage().getChatId())
                .messageId(callbackQuery.getMessage().getMessageId())
                .build());
        bot.executeAsync(SendPhoto.builder()
                .chatId(callbackQuery.getFrom().getId())
                .photo(mediaService.getResortFile(currentResorts.get(index)))
                .caption(cardService.getResortCard(currentResorts.get(index), false))
                .replyMarkup(keyboardService.getResortCardKeyboard(index, resortId, currentResorts.size()))
                .parseMode(ParseMode.MARKDOWN)
                .build());
        bot.executeAsync(AnswerCallbackQuery.builder()
                .callbackQueryId(callbackQuery.getId())
                .build());


    }

    @SneakyThrows
    private void addCustomTourToFavorites(CallbackQuery callbackQuery,
                                          DvgKiprBot bot,
                                          CustomTour currentCustomTour,
                                          Long customTourId,
                                          int index
    ) {
        String description;
        description = cardService.getFavoriteCard(currentCustomTour);

        List<List<InputMedia>> allMedias;
        try {
            allMedias = mediaService.getCustomTourMedias(currentCustomTour);
            if (!allMedias.isEmpty()) {
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
            } else {
                bot.executeAsync(SendPhoto.builder()
                        .chatId(callbackQuery.getFrom().getId())
                        .photo(mediaService.getCustomTourFile(currentCustomTour))
                        .build());
            }
        }catch (Exception e) {
            logger.error("Unhandled exception in addCustomTourToFavorites " + e.getMessage());
        }

        bot.executeAsync(SendMessage.builder()
                .chatId(callbackQuery.getMessage().getChatId())
                .text(description)
                .parseMode(ParseMode.MARKDOWN)
                .build());

        bot.executeAsync(DeleteMessage.builder()
                .chatId(callbackQuery.getMessage().getChatId())
                .messageId(callbackQuery.getMessage().getMessageId())
                .build());
        bot.executeAsync(SendPhoto.builder()
                .chatId(callbackQuery.getFrom().getId())
                .photo(mediaService.getCustomTourFile(currentCustomTour))
                .caption(cardService.getCustomTourCard(currentCustomTour, false))
                .replyMarkup(keyboardService.getCustomToursKeyboard(index, customTourId))
                .parseMode(ParseMode.MARKDOWN)
                .build());
        bot.executeAsync(AnswerCallbackQuery.builder()
                .callbackQueryId(callbackQuery.getId())
                .build());

    }
}