package bcd.solution.dvgKiprBot.core.services;

import bcd.solution.dvgKiprBot.core.models.Activity;
import bcd.solution.dvgKiprBot.core.models.Resort;
import bcd.solution.dvgKiprBot.core.models.Stars;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import bcd.solution.dvgKiprBot.core.repository.CustomTourRepo;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class KeyboardService {
    private final CustomTourRepo customTourRepo;

    private final String rightArrowText = "Далее ➡️";
    private final String leftArrowText = "⬅️ Назад";
    private final String restartButtonText = "🔄 В начало 🔄";
    private final String cancelButtonText = "❌ Отмена ❌";
    private final String phoneButtonText = "☎️ Добавить номер телефона ☎️";
    private final String sendPhoneButtonText = "☎️ Отправить номер телефона ☎️";
    private final String confirmButtonText = "✅ Выбрать ✅";
    private final String showPhotoButtonText = "🖼️ Фотографии";
    private final String noMatterButtonText = "🤷‍♂️ Не важно 🤷‍♂️";
    private final String homeButtonText = "🏠 На домашнюю страницу 🏠";
    private final String authButtonText = "🔐 Авторизация для партнеров 🔐";
    private final String activitiesButtonText = "⚽️ Активности ⚽️";
    private final String resortsButtonText = "🏝️ Курорты 🏝️";
    private final String hotelsButtonText = "🏨 Отели 🏨";
    private final String customToursButtonText = "🗺️ Авторские туры 🗺️";
    private final String toListButtonText = "⬆️ К списку ⬆️️";
    private final String toStarsButtonText = "⭐️ К звездам ⭐️";
    private final String goBackButtonText = "↪️ Скрыть подробную информацию ↩️";
    private final String tourConstructorButtonText = "🏨 Подобрать отели 🏨";
    private final String detailsButtonText = "📝 Подробнее";
    private final String chooseTourButtonText = "🗺️ Подобрать тур 🗺️";
    private final String addToFavorites = "\uD83D\uDCBE Сохранить";



    @Autowired
    public KeyboardService(CustomTourRepo customTourRepo) {
        this.customTourRepo = customTourRepo;
    }
    public InlineKeyboardMarkup getHotelsStarsKeyboard() {
        InlineKeyboardMarkup.InlineKeyboardMarkupBuilder builder = InlineKeyboardMarkup.builder();

        builder.keyboardRow(List.of(InlineKeyboardButton.builder()
                        .text(Stars.threestar.toString())
                        .callbackData("hotels_stars/" + Stars.threestar.name())
                        .build()))
                .keyboardRow(List.of(InlineKeyboardButton.builder()
                        .text(Stars.fourstar.toString())
                        .callbackData("hotels_stars/" + Stars.fourstar.name())
                        .build()))
                .keyboardRow(List.of(InlineKeyboardButton.builder()
                        .text(Stars.fivestar.toString())
                        .callbackData("hotels_stars/" + Stars.fivestar.name())
                        .build()))
                .keyboardRow(List.of(
                        InlineKeyboardButton.builder()
                                .text(noMatterButtonText)
                                .callbackData("hotels_stars/noMatter")
                                .build()))
                .keyboardRow(List.of(
                        InlineKeyboardButton.builder()
                                .text(restartButtonText)
                                .callbackData("restart")
                                .build()));

        return builder.build();
    }

    public InlineKeyboardMarkup getRestartKeyboard() {
        return InlineKeyboardMarkup.builder()
                .keyboardRow(List.of(InlineKeyboardButton.builder()
                        .text(homeButtonText)
                        .callbackData("start")
                        .build()))
                .build();
    }

    public InlineKeyboardMarkup getAuthCancelKeyboard() {
        return InlineKeyboardMarkup.builder()
                .keyboardRow(List.of(InlineKeyboardButton.builder()
                        .text(cancelButtonText)
                        .callbackData("auth_cancel")
                        .build()))
                .build();
    }

    public InlineKeyboardMarkup getPhoneCancelKeyboard() {
        return InlineKeyboardMarkup.builder()
                .keyboardRow(List.of(InlineKeyboardButton.builder()
                        .text(cancelButtonText)
                        .callbackData("auth_phoneCancel")
                        .build()))
                .build();
    }

    public InlineKeyboardMarkup getTourChoosingKeyboard(boolean hasPhone, boolean isAuthorized) {
        InlineKeyboardMarkup.InlineKeyboardMarkupBuilder builder = InlineKeyboardMarkup.builder();
        if (!isAuthorized) {
            builder.keyboardRow(List.of(InlineKeyboardButton.builder()
                    .text(authButtonText)
                    .callbackData("auth")
                    .build()));
        }
//        if (hasPhone) {
        if (true) {
            builder.keyboardRow(List.of(InlineKeyboardButton.builder()
                            .text(tourConstructorButtonText)
                            .callbackData("tour")
                            .build()))
//                    .keyboardRow(List.of(InlineKeyboardButton.builder()
//                            .text(resortsButtonText)
//                            .callbackData("resorts")
//                            .build()))
//                    .keyboardRow(List.of(InlineKeyboardButton.builder()
//                            .text(hotelsButtonText)
//                            .callbackData("hotels")
//                            .build()))
                    .keyboardRow(List.of(InlineKeyboardButton.builder()
                            .text(customToursButtonText)
                            .callbackData("customTours")
                            .build()
                    ));
        }
        if (!hasPhone) {
            builder.keyboardRow(List.of(InlineKeyboardButton.builder()
                    .text(phoneButtonText)
                    .callbackData("auth_getPhone")
                    .build()));
        }

        return builder.build();
    }

    public InlineKeyboardMarkup getTourConstructorKeyboard() {
        return InlineKeyboardMarkup.builder()
                .keyboardRow(List.of(InlineKeyboardButton.builder()
                        .text(activitiesButtonText)
                        .callbackData("activities")
                        .build()))
                .keyboardRow(List.of(InlineKeyboardButton.builder()
                        .text(resortsButtonText)
                        .callbackData("resorts")
                        .build()))
                .keyboardRow(List.of(InlineKeyboardButton.builder()
                        .text(hotelsButtonText)
                        .callbackData("hotels")
                        .build()))
                .keyboardRow(List.of(InlineKeyboardButton.builder()
                                .text(restartButtonText)
                                .callbackData("restart")
                        .build()))
                .build();
    }

    public InlineKeyboardMarkup getActivitiesKeyboard(List<Activity> selectedActivities,
                                                      List<Activity> allActivities) {
//        List<Activity> allActivities = activityRepo.findAll();
        Set<Activity> activitySet = new HashSet<>(selectedActivities);

        InlineKeyboardMarkup.InlineKeyboardMarkupBuilder builder = InlineKeyboardMarkup.builder();

        for (Activity activity : allActivities) {
            boolean isChosen = activitySet.contains(activity);
            builder.keyboardRow(List.of(
                    InlineKeyboardButton.builder()
                            .text(activity.name + (isChosen ? " (убрать)" : ""))
                            .callbackData("activities_" + (isChosen ? "delete" : "add") + "/" + (activity.getId()))
                            .build()
            ));
        }

        return builder
                .keyboardRow(List.of(
                        InlineKeyboardButton.builder()
                                .text(noMatterButtonText)
                                .callbackData("select_activity/noMatter")
                                .build()
                ))
                .keyboardRow(List.of(
                        InlineKeyboardButton.builder()
                                .text(confirmButtonText)
                                .callbackData("select_activity")
                                .build()
                ))
                .keyboardRow(List.of(
                        InlineKeyboardButton.builder()
                                .text(restartButtonText)
                                .callbackData("restart")
                                .build()
                ))
                .build();
    }

    public InlineKeyboardMarkup getResortCardKeyboard(Integer index, Long resortId, long size) {

        List<InlineKeyboardButton> navigation_row = new ArrayList<>();
        if (index > 0) {
            navigation_row.add(InlineKeyboardButton.builder()
                    .text(leftArrowText)
                    .callbackData("resorts_change/" + (index - 1))
                    .build());
        }
        navigation_row.add(InlineKeyboardButton.builder()
                .text((index + 1) + "/" + size)
                .callbackData("null")
                .build());
        if (index < size - 1) {
            navigation_row.add(InlineKeyboardButton.builder()
                    .text(rightArrowText)
                    .callbackData("resorts_change/" + (index + 1))
                    .build());
        }

        return InlineKeyboardMarkup.builder()
                .keyboardRow(List.of(
                        InlineKeyboardButton.builder()
                                .text(detailsButtonText)
                                .callbackData("resorts_card/" + (resortId))
                                .build(),
                        InlineKeyboardButton.builder()
                                .text(showPhotoButtonText)
                                .callbackData("resorts_media/" + (index) + "/" + (resortId))
                                .build(),
                        InlineKeyboardButton.builder()
                                .text(addToFavorites)
                                .callbackData("favorite_add/"  + (index) + "/" + (resortId)+"/"+"resort")
                                .build()
                ))
                .keyboardRow(navigation_row)
                .keyboardRow(List.of(
                        InlineKeyboardButton.builder()
                                .text(confirmButtonText)
                                .callbackData("select_resort/" + (resortId))
                                .build()
                ))
                .keyboardRow(List.of(
                        InlineKeyboardButton.builder()
                                .text(toListButtonText)
                                .callbackData("resorts")
                                .build(),
                        InlineKeyboardButton.builder()
                                .text(restartButtonText)
                                .callbackData("restart")
                                .build()
                )).build();
    }

    public InlineKeyboardMarkup getResortsKeyboard(List<Resort> resortList) {

        InlineKeyboardMarkup.InlineKeyboardMarkupBuilder builder = InlineKeyboardMarkup.builder();

        for (int i = 0; i < resortList.size(); ++i) {
            Resort resort = resortList.get(i);
            builder.keyboardRow(List.of(
                    InlineKeyboardButton.builder()
                            .text(resort.name)
                            .callbackData("resorts_change/" + i)
                            .build()
            ));
        }

        return builder
                .keyboardRow(List.of(
                        InlineKeyboardButton.builder()
                                .text(noMatterButtonText)
                                .callbackData("select_resort/noMatter")
                                .build()
                ))
                .keyboardRow(List.of(
                        InlineKeyboardButton.builder()
                                .text(restartButtonText)
                                .callbackData("restart")
                                .build()
                ))
                .build();
    }

    public InlineKeyboardMarkup getCustomToursKeyboard(Integer index, Long customTourId) {

        long size = customTourRepo.countByIsDeleted(false);

        List<InlineKeyboardButton> navigation_row = new ArrayList<>();
        if (index > 0) {
            navigation_row.add(InlineKeyboardButton.builder()
                    .text(leftArrowText)
                    .callbackData("customTours_change/" + (index - 1))
                    .build());
        }
        navigation_row.add(InlineKeyboardButton.builder()
                .text((index + 1) + "/" + size)
                .callbackData("null")
                .build());
        if (index < size - 1) {
            navigation_row.add(InlineKeyboardButton.builder()
                    .text(rightArrowText)
                    .callbackData("customTours_change/" + (index + 1))
                    .build());
        }

        return InlineKeyboardMarkup.builder()
                .keyboardRow(List.of(
                        InlineKeyboardButton.builder()
                                .text(detailsButtonText)
                                .callbackData("customTours_card/" + (customTourId))
                                .build(),
                        InlineKeyboardButton.builder()
                                .text(showPhotoButtonText)
                                .callbackData("customTours_media/" + (index) + "/" + (customTourId))
                                .build(),
                        InlineKeyboardButton.builder()
                                .text(addToFavorites)
                                .callbackData("favorite_add/"  + (index) + "/" + (customTourId)+"/"+"customTour")
                                .build()
                ))
                .keyboardRow(navigation_row)
                .keyboardRow(List.of(
                        InlineKeyboardButton.builder()
                                .text(confirmButtonText)
                                .callbackData("customTours_select/" + (customTourId))
                                .build()
                ))
                .keyboardRow(List.of(
                        InlineKeyboardButton.builder()
                                .text(restartButtonText)
                                .callbackData("restart")
                                .build()
                ))
                .build();
    }

    public InlineKeyboardMarkup getHotelsKeyboard(Integer index, Long hotelId, long size) {
        List<InlineKeyboardButton> navigation_row = new ArrayList<>();
        if (index > 0) {
            navigation_row.add(InlineKeyboardButton.builder()
                    .text(leftArrowText)
                    .callbackData("hotels_change/" + (index - 1))
                    .build());
        }
        navigation_row.add(InlineKeyboardButton.builder()
                .text((index + 1) + "/" + size)
                .callbackData("null")
                .build());
        if (index < size - 1) {
            navigation_row.add(InlineKeyboardButton.builder()
                    .text(rightArrowText)
                    .callbackData("hotels_change/" + (index + 1))
                    .build());
        }

        return InlineKeyboardMarkup.builder()
                .keyboardRow(List.of(
                        InlineKeyboardButton.builder()
                                .text(detailsButtonText)
                                .callbackData("hotels_card/" + (hotelId))
                                .build(),
                        InlineKeyboardButton.builder()
                                .text(showPhotoButtonText)
                                .callbackData("hotels_media/" + (index) + "/" + (hotelId))
                                .build(),
                        InlineKeyboardButton.builder()
                                .text(addToFavorites)
                                .callbackData("favorite_add/"  + (index) + "/" + (hotelId)+"/"+"hotel")
                                .build()
                ))
                .keyboardRow(navigation_row)
                .keyboardRow(List.of(
                        InlineKeyboardButton.builder()
                                .text(confirmButtonText)
                                .callbackData("hotels_select/" + (hotelId))
                                .build()
                ))
                .keyboardRow(List.of(
                        InlineKeyboardButton.builder()
                                .text(restartButtonText)
                                .callbackData("restart")
                                .build(),
                        InlineKeyboardButton.builder()
                                .text(toStarsButtonText)
                                .callbackData("hotels")
                                .build()
                ))
                .build();
    }

    public InlineKeyboardMarkup getStarterKeyboard() {
        return InlineKeyboardMarkup.builder()
                .keyboardRow(List.of(
                        InlineKeyboardButton.builder()
                                .text(phoneButtonText)
                                .callbackData("auth_getPhone")
                                .build()))
                .keyboardRow(List.of(
                        InlineKeyboardButton.builder()
                                .text(chooseTourButtonText)
                                .callbackData("restart")
                                .build()))
                .build();
    }

    public ReplyKeyboardMarkup getPhoneKeyboard() {
        return ReplyKeyboardMarkup.builder()
                .resizeKeyboard(true)
                .isPersistent(true)
                .keyboardRow(
                        new KeyboardRow(
                                List.of(
                                        KeyboardButton.builder()
                                                .text(sendPhoneButtonText)
                                                .requestContact(true)
                                                .build())))
                .build();
    }

    public InlineKeyboardMarkup getDeleteKeyboard() {
        return InlineKeyboardMarkup.builder()
                .keyboardRow(List.of(InlineKeyboardButton.builder()
                                .callbackData("delete")
                                .text(goBackButtonText)
                        .build()))
                .build();
    }

}
