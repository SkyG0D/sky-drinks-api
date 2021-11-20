package com.github.skyg0d.skydrinksapi.util.drink;

import com.github.skyg0d.skydrinksapi.domain.Drink;
import com.github.skyg0d.skydrinksapi.requests.DrinkPostRequestBody;

public class DrinkPostRequestBodyCreator {

    public static DrinkPostRequestBody createDrinkPostRequestBodyToBeSave() {
        Drink drink = DrinkCreator.createDrinkToBeSave();

        return DrinkPostRequestBody
                .builder()
                .name(drink.getName())
                .additional(String.join(Drink.ADDITIONAL_SEPARATOR, drink.getAdditional()))
                .alcoholic(drink.isAlcoholic())
                .price(drink.getPrice())
                .picture(drink.getPicture())
                .build();
    }

}