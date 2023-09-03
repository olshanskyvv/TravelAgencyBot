package bcd.solution.dvgKiprBot.core.services;

import bcd.solution.dvgKiprBot.core.models.*;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CardService {

    public String getFavoriteCard(Hotel hotel){
        return  getHotelCard(hotel, true);
    };
    public String getFavoriteCard(Resort resort){
        return  getResortCard(resort, true);
    };
    public String getFavoriteCard(CustomTour customTour){
        return  getCustomTourCard(customTour, true);
    };
    @SneakyThrows
    public String getHotelCard(Hotel hotel, boolean isLong) {
        StringBuilder card = new StringBuilder(hotel.name + " " + hotel.stars.toString() + "\n\n" +
                "*" + "Курорт: " +  hotel.resort.name + "*" + "\n\n" +
                hotel.description);

        if (isLong) {
//            TODO: find solution
//            card.append("\n\n_/*Активности_:\n");
//            for (Activity activity : hotel.activities) {
//                card.append("- ").append(activity.name).append("\n");
//            }
//            card.append("\n_Особенности_:\n");
//            for (HotelFeature feature : hotel.features) {
//                card.append("- ").append(feature.name).append("\n");
//            }
//            card.append("\n_Питание_:");
//            for (Food food : hotel.food) {
//                card.append("- ").append(food).append("\n");
//            }
        } else {
            if (card.length() > 450) {
                card.setLength(450);
                int index = card.lastIndexOf("\n");
                card.setLength(index + 1);
                card.append("...");
            }
            card.append("\n\n_Подробнее по кнопке_");
        }

        return card.toString();
    }

    @SneakyThrows
    public String getResortCard(Resort resort, boolean isLong) {
//        StringBuilder activity_list = new StringBuilder();
//        for (Activity activity : resort.activities) {
//            activity_list.append("- ").append(activity.name).append("\n");
//        }

        StringBuilder card = new StringBuilder(
              "*" +  resort.name + "*" + "\n\n");
            card.append(resort.description).append("\n");

        if (isLong) {
//            card.append("\n\n_Адрес_: ").append(resort.geo).append("\n");
//            card.append("\n\n_Доступные активности_:\n" ).append(activity_list).append("\n");

        } else {
            if (card.length() > 450) {
                card.setLength(450);
                card.append("...");
            }
            card.append("\n\n_Подробнее по кнопке_");
        }

        return card.toString();
    }

    @SneakyThrows
    public String getCustomTourCard(CustomTour customTour, boolean isLong) {
        StringBuilder card = new StringBuilder(customTour.name + "\n\n" +
                customTour.description);

        if (!isLong) {
            if (card.length() > 450) {
                card.setLength(450);
                card.append("...");
            }
            card.append("\n\n_Подробнее по кнопке_");
        }

        return card.toString();
    }

    @SneakyThrows
    public String getManagerCard(StateMachine stateMachine) {
        StringBuilder card = new StringBuilder(
                "Пользователь подобрал тур\n\n" +
                        "Тег: @" + stateMachine.user.getLogin() + "\n");
        if (stateMachine.user.getPhone() != null) {
            card.append("Номер телефона: ").append(stateMachine.user.getPhone()).append("\n\n");
        }

        fillTourInfo(card, stateMachine);

        card.append("Вскоре он с Вами свяжется для завершения оформления тура.");
        return card.toString();
    }

    @SneakyThrows
    public String getUserCard(StateMachine stateMachine,
                              String managerUsername,
                              List<String> contactPhones) {
        StringBuilder card = new StringBuilder("Спасибо, что выбрали нас!\n\nВаш выбор:\n");

        fillTourInfo(card, stateMachine);

        card.append("Обратитесь к менеджеру (@")
                .append(managerUsername)
                .append(") для расчёта выбранного тура или свяжитесь с нами по номерам:");
        if (!contactPhones.isEmpty()) {
            for (String phone : contactPhones) {
                card.append("\n ").append(phone);
            }
        }
        return card.toString();
    }


    private void fillTourInfo(StringBuilder card, StateMachine stateMachine) {
        if (stateMachine.customTour != null) {
            card.append("Авторский тур: ").append(stateMachine.customTour.name).append("\n\n");
        } else {
            if (stateMachine.resort != null) {
                card.append("Курорт: ").append(stateMachine.resort.name).append("\n");
            }
            card.append("Отель: ").append(stateMachine.hotel.name).append("\n\n");
        }
    }


}
