package com.github.skyg0d.skydrinksapi.integration;

import com.github.skyg0d.skydrinksapi.domain.Drink;
import com.github.skyg0d.skydrinksapi.repository.drink.DrinkRepository;
import com.github.skyg0d.skydrinksapi.requests.DrinkPostRequestBody;
import com.github.skyg0d.skydrinksapi.requests.DrinkPutRequestBody;
import com.github.skyg0d.skydrinksapi.util.TokenUtil;
import com.github.skyg0d.skydrinksapi.util.drink.DrinkCreator;
import com.github.skyg0d.skydrinksapi.util.drink.DrinkPostRequestBodyCreator;
import com.github.skyg0d.skydrinksapi.util.drink.DrinkPutRequestBodyCreator;
import com.github.skyg0d.skydrinksapi.wrapper.PageableResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@DisplayName("Integration Tests for DrinkController")
class DrinkControllerIT {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private DrinkRepository drinkRepository;

    @Autowired
    private TokenUtil tokenUtil;

    @Test
    @DisplayName("listAll return list of drinks inside page object when successful")
    void listAll_ReturnListOfDrinksInsidePageObject_WhenSuccessful() {
        Drink drinkSaved = drinkRepository.save(DrinkCreator.createDrinkToBeSave());

        ResponseEntity<PageableResponse<Drink>> entity = testRestTemplate.exchange(
                "/drinks",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                }
        );

        assertThat(entity).isNotNull();

        assertThat(entity.getStatusCode())
                .isNotNull()
                .isEqualTo(HttpStatus.OK);

        assertThat(entity.getBody()).isNotNull();

        assertThat(entity.getBody().toList())
                .isNotEmpty()
                .hasSize(1)
                .contains(drinkSaved);
    }

    @Test
    @DisplayName("listAll return empty page when there are no drinks")
    void listAll_ReturnListOfDrinksInsidePageObject_WhenThereAreNoDrinks() {
        ResponseEntity<PageableResponse<Drink>> entity = testRestTemplate.exchange(
                "/drinks",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                }
        );

        assertThat(entity).isNotNull();

        assertThat(entity.getStatusCode())
                .isNotNull()
                .isEqualTo(HttpStatus.OK);

        assertThat(entity.getBody()).isEmpty();
    }

    @Test
    @DisplayName("findById return list of drinks inside page object when successful")
    void findById_ReturnListOfDrinksInsidePageObject_WhenSuccessful() {
        Drink drinkSaved = drinkRepository.save(DrinkCreator.createDrinkToBeSave());

        ResponseEntity<Drink> entity = testRestTemplate.getForEntity("/drinks/{uuid}", Drink.class, drinkSaved.getUuid());

        assertThat(entity).isNotNull();

        assertThat(entity.getStatusCode())
                .isNotNull()
                .isEqualTo(HttpStatus.OK);

        assertThat(entity.getBody())
                .isNotNull()
                .isEqualTo(drinkSaved);
    }

    @Test
    @DisplayName("findById returns 400 BadRequest when drink not exists")
    void findById_Returns400BadRequest_WhenDrinkNotExists() {
        ResponseEntity<Drink> entity = testRestTemplate.getForEntity("/drinks/{uuid}", Drink.class, UUID.randomUUID());

        assertThat(entity).isNotNull();

        assertThat(entity.getStatusCode())
                .isNotNull()
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("search return list of drinks inside page object when successful")
    void search_ReturnListOfDrinksInsidePageObject_WhenSuccessful() {
        Drink drinkSaved = drinkRepository.save(DrinkCreator.createDrinkToBeSave());

        String url = String.format("/drinks/search?name=%s", drinkSaved.getName());

        ResponseEntity<PageableResponse<Drink>> entity = testRestTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                }
        );

        assertThat(entity).isNotNull();

        assertThat(entity.getStatusCode())
                .isNotNull()
                .isEqualTo(HttpStatus.OK);

        assertThat(entity.getBody()).isNotNull();

        assertThat(entity.getBody().toList())
                .isNotEmpty()
                .hasSize(1)
                .contains(drinkSaved);
    }

    @Test
    @DisplayName("search return empty page object when does not match")
    void search_ReturnEmptyPage_WhenDoesNotMatch() {
        drinkRepository.save(DrinkCreator.createDrinkToBeSave());

        String url = String.format("/drinks/search?name=%s", "fqfqfqgqg");

        ResponseEntity<PageableResponse<Drink>> entity = testRestTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                }
        );

        assertThat(entity).isNotNull();

        assertThat(entity.getStatusCode())
                .isNotNull()
                .isEqualTo(HttpStatus.OK);

        assertThat(entity.getBody()).isEmpty();

    }

    @Test
    @DisplayName("findByPicture return list of drinks when successful")
    void findByPicture_ReturnListOfDrinks_WhenSuccessful() {
        Drink drinkSaved = drinkRepository.save(DrinkCreator.createDrinkToBeSave());

        ResponseEntity<List<Drink>> entity = testRestTemplate.exchange(
                "/drinks/find-by-picture/{picture}",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                },
                drinkSaved.getPicture()
        );

        assertThat(entity).isNotNull();

        assertThat(entity.getStatusCode())
                .isNotNull()
                .isEqualTo(HttpStatus.OK);

        assertThat(entity.getBody())
                .isNotEmpty()
                .hasSize(1)
                .contains(drinkSaved);
    }

    @Test
    @DisplayName("findByPicture return empty list when no picture found")
    void findByPicture_ReturnEmptyList_WhenNoPictureFound() {
        drinkRepository.save(DrinkCreator.createDrinkToBeSave());

        ResponseEntity<List<Drink>> entity = testRestTemplate.exchange(
                "/drinks/find-by-picture/{picture}",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                },
                "drink.png"
        );

        assertThat(entity).isNotNull();

        assertThat(entity.getStatusCode())
                .isNotNull()
                .isEqualTo(HttpStatus.OK);

        assertThat(entity.getBody()).isEmpty();
    }

    @Test
    @DisplayName("save creates drink when successful")
    void save_CreatesDrink_WhenSuccessful() {
        DrinkPostRequestBody drinkValid = DrinkPostRequestBodyCreator.createDrinkPostRequestBodyToBeSave();

        ResponseEntity<Drink> entity = testRestTemplate.postForEntity(
                "/drinks/barmen",
                tokenUtil.createBarmenAuthEntity(drinkValid),
                Drink.class
        );

        assertThat(entity).isNotNull();

        assertThat(entity.getStatusCode())
                .isNotNull()
                .isEqualTo(HttpStatus.CREATED);

        assertThat(entity.getBody()).isNotNull();

        assertThat(entity.getBody().getUuid()).isNotNull();

        assertThat(entity.getBody().getName())
                .isNotNull()
                .isEqualTo(drinkValid.getName());
    }

    @Test
    @DisplayName("save returns 403 Forbidden when user does not have ROLE_BARMEN")
    void save_Returns403Forbidden_WhenUserDoesNotHaveROLE_BARMEN() {
        DrinkPostRequestBody drinkValid = DrinkPostRequestBodyCreator.createDrinkPostRequestBodyToBeSave();

        ResponseEntity<Drink> entity = testRestTemplate.postForEntity(
                "/drinks/barmen",
                tokenUtil.createUserAuthEntity(drinkValid),
                Drink.class
        );

        assertThat(entity).isNotNull();

        assertThat(entity.getStatusCode())
                .isNotNull()
                .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("replace updates drink when successful")
    void replace_UpdatedDrink_WhenSuccessful() {
        Drink drinkSaved = drinkRepository.save(DrinkCreator.createDrinkToBeSave());

        DrinkPutRequestBody drinkToUpdate = DrinkPutRequestBodyCreator.createDrinkPutRequestBodyToBeUpdate();

        drinkToUpdate.setUuid(drinkSaved.getUuid());

        ResponseEntity<Void> entity = testRestTemplate.exchange(
                "/drinks/barmen",
                HttpMethod.PUT,
                tokenUtil.createBarmenAuthEntity(drinkToUpdate),
                Void.class
        );

        assertThat(entity).isNotNull();

        assertThat(entity.getStatusCode())
                .isNotNull()
                .isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    @DisplayName("replace returns 400 BadRequest when drink not exists")
    void replace_Returns400BadRequest_WhenDrinkNotExists() {
        DrinkPutRequestBody drinkToUpdate = DrinkPutRequestBodyCreator.createDrinkPutRequestBodyToBeUpdate();

        ResponseEntity<Void> entity = testRestTemplate.exchange(
                "/drinks/barmen",
                HttpMethod.PUT,
                tokenUtil.createBarmenAuthEntity(drinkToUpdate),
                Void.class
        );

        assertThat(entity).isNotNull();

        assertThat(entity.getStatusCode())
                .isNotNull()
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("replace returns 403 Forbidden when user does not have ROLE_BARMEN")
    void replace_Returns403Forbidden_WhenUserDoesNotHaveROLE_BARMEN() {
        Drink drinkSaved = drinkRepository.save(DrinkCreator.createDrinkToBeSave());

        DrinkPutRequestBody drinkToUpdate = DrinkPutRequestBodyCreator.createDrinkPutRequestBodyToBeUpdate();

        drinkToUpdate.setUuid(drinkSaved.getUuid());

        ResponseEntity<Void> entity = testRestTemplate.exchange(
                "/drinks/barmen",
                HttpMethod.PUT,
                tokenUtil.createUserAuthEntity(drinkToUpdate),
                Void.class
        );

        assertThat(entity).isNotNull();

        assertThat(entity.getStatusCode())
                .isNotNull()
                .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("delete removes drink when successful")
    void delete_RemovesDrink_WhenSuccessful() {
        Drink drinkSaved = drinkRepository.save(DrinkCreator.createDrinkToBeSave());

        ResponseEntity<Void> entity = testRestTemplate.exchange(
                "/drinks/barmen/{uuid}",
                HttpMethod.DELETE,
                tokenUtil.createBarmenAuthEntity(null),
                Void.class,
                drinkSaved.getUuid()
        );

        assertThat(entity).isNotNull();

        assertThat(entity.getStatusCode())
                .isNotNull()
                .isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    @DisplayName("delete returns 400 BadRequest when drink not exists")
    void delete_Returns400BadRequest_WhenDrinkNotExists() {
        ResponseEntity<Void> entity = testRestTemplate.exchange(
                "/drinks/barmen/{uuid}",
                HttpMethod.DELETE,
                tokenUtil.createBarmenAuthEntity(null),
                Void.class,
                UUID.randomUUID()
        );

        assertThat(entity).isNotNull();

        assertThat(entity.getStatusCode())
                .isNotNull()
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("delete returns 403 Forbidden when user does not have ROLE_BARMEN")
    void delete_Returns403Forbidden_WhenUserDoesNotHaveROLE_BARMEN() {
        Drink drinkSaved = drinkRepository.save(DrinkCreator.createDrinkToBeSave());

        ResponseEntity<Void> entity = testRestTemplate.exchange(
                "/drinks/barmen/{uuid}",
                HttpMethod.DELETE,
                tokenUtil.createUserAuthEntity(null),
                Void.class,
                drinkSaved.getUuid()
        );

        assertThat(entity).isNotNull();

        assertThat(entity.getStatusCode())
                .isNotNull()
                .isEqualTo(HttpStatus.FORBIDDEN);
    }

}
