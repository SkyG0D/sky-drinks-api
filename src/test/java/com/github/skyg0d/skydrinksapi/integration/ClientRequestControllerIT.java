package com.github.skyg0d.skydrinksapi.integration;

import com.github.skyg0d.skydrinksapi.domain.*;
import com.github.skyg0d.skydrinksapi.enums.ClientRequestStatus;
import com.github.skyg0d.skydrinksapi.exception.details.BadRequestExceptionDetails;
import com.github.skyg0d.skydrinksapi.repository.drink.DrinkRepository;
import com.github.skyg0d.skydrinksapi.repository.request.ClientRequestRepository;
import com.github.skyg0d.skydrinksapi.repository.table.TableRepository;
import com.github.skyg0d.skydrinksapi.repository.user.ApplicationUserRepository;
import com.github.skyg0d.skydrinksapi.requests.ClientRequestPostRequestBody;
import com.github.skyg0d.skydrinksapi.requests.ClientRequestPutRequestBody;
import com.github.skyg0d.skydrinksapi.util.TokenUtil;
import com.github.skyg0d.skydrinksapi.util.drink.DrinkCreator;
import com.github.skyg0d.skydrinksapi.util.request.*;
import com.github.skyg0d.skydrinksapi.util.table.TableCreator;
import com.github.skyg0d.skydrinksapi.util.user.ApplicationUserCreator;
import com.github.skyg0d.skydrinksapi.wrapper.PageableResponse;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@DisplayName("Integration Tests for ClientRequestController")
@Log4j2
class ClientRequestControllerIT {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private ClientRequestRepository clientRequestRepository;

    @Autowired
    private ApplicationUserRepository applicationUserRepository;

    @Autowired
    private DrinkRepository drinkRepository;

    @Autowired
    private TableRepository tableRepository;

    @Autowired
    private TokenUtil tokenUtil;

    @Test
    @DisplayName("listAll return list of client requests inside page object when successful")
    void listAll_ReturnListOfClientRequestsInsidePageObject_WhenSuccessful() {
        ClientRequest clientRequestSaved = persistClientRequest();

        ResponseEntity<PageableResponse<ClientRequest>> entity = testRestTemplate.exchange(
                "/requests/staff",
                HttpMethod.GET,
                tokenUtil.createWaiterAuthEntity(null),
                new ParameterizedTypeReference<>() {
                }
        );

        assertThat(entity).isNotNull();

        assertThat(entity.getStatusCode())
                .isNotNull()
                .isEqualTo(HttpStatus.OK);

        assertThat(entity.getBody())
                .isNotEmpty()
                .hasSize(1)
                .contains(clientRequestSaved);
    }

    @Test
    @DisplayName("listAll return empty page when there are no client requests")
    void listAll_ReturnListOfClientRequestsInsidePageObject_WhenThereAreNoClientRequests() {
        ResponseEntity<PageableResponse<ClientRequest>> entity = testRestTemplate.exchange(
                "/requests/staff",
                HttpMethod.GET,
                tokenUtil.createWaiterAuthEntity(null),
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
    @DisplayName("listAll returns 403 Forbidden when user does not have ROLE_WAITER or ROLE_BARMEN7")
    void listAll_Returns403Forbidden_WhenUserDoesNotHaveROLE_WAITERorROLE_BARMEN() {
        ResponseEntity<Void> entity = testRestTemplate.exchange(
                "/requests/staff",
                HttpMethod.GET,
                tokenUtil.createUserAuthEntity(null),
                Void.class
        );

        assertThat(entity).isNotNull();

        assertThat(entity.getStatusCode())
                .isNotNull()
                .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("findById returns an client request object when successful")
    void findById_ReturnsClientRequestObject_WhenSuccessful() {
        ClientRequest clientRequestSaved = persistClientRequest();

        ResponseEntity<ClientRequest> entity = testRestTemplate.exchange(
                "/requests/{uuid}",
                HttpMethod.GET,
                null,
                ClientRequest.class,
                clientRequestSaved.getUuid()
        );

        log.info(entity);

        assertThat(entity).isNotNull();

        assertThat(entity.getStatusCode())
                .isNotNull()
                .isEqualTo(HttpStatus.OK);

        assertThat(entity.getBody())
                .isNotNull()
                .isEqualTo(clientRequestSaved);
    }

    @Test
    @DisplayName("findById returns 400 BadRequest when client request not exists")
    void findById_Returns400BadRequest_WhenClientRequestNotExists() {
        ResponseEntity<BadRequestExceptionDetails> entity = testRestTemplate.exchange(
                "/requests/{uuid}",
                HttpMethod.GET,
                null,
                BadRequestExceptionDetails.class,
                UUID.randomUUID()
        );

        assertThat(entity).isNotNull();

        assertThat(entity.getStatusCode())
                .isNotNull()
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("search return list of client requests inside page object when successful")
    void search_ReturnListOfClientRequestsInsidePageObject_WhenSuccessful() {
        ClientRequest clientRequestSaved = persistClientRequest();

        String url = String.format("/requests/staff/search?drinkUUID=%s", clientRequestSaved.getDrinks().get(0).getUuid());

        ResponseEntity<PageableResponse<ClientRequest>> entity = testRestTemplate.exchange(
                url,
                HttpMethod.GET,
                tokenUtil.createWaiterAuthEntity(null),
                new ParameterizedTypeReference<>() {
                }
        );

        assertThat(entity).isNotNull();

        assertThat(entity.getStatusCode())
                .isNotNull()
                .isEqualTo(HttpStatus.OK);

        assertThat(entity.getBody())
                .isNotEmpty()
                .hasSize(1)
                .contains(clientRequestSaved);
    }

    @Test
    @DisplayName("search return empty page object when does not match")
    void search_ReturnEmptyPage_WhenDoesNotMatch() {
        String url = String.format("/requests/staff/search?drinkUUID=%s", UUID.randomUUID());

        ResponseEntity<PageableResponse<ClientRequest>> entity = testRestTemplate.exchange(
                url,
                HttpMethod.GET,
                tokenUtil.createWaiterAuthEntity(null),
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
    @DisplayName("searchMyRequests return list of client requests inside page object when successful")
    void searchMyRequests_ReturnListOfClientRequestsInsidePageObject_WhenSuccessful() {
        ClientRequest clientRequestSaved = persistClientRequest(applicationUserRepository.findByEmail(ApplicationUserCreator.createApplicationUser().getEmail()).get());

        String url = String.format("/requests/user/my-requests?drinkUUID=%s", clientRequestSaved.getDrinks().get(0).getUuid());

        ResponseEntity<PageableResponse<ClientRequest>> entity = testRestTemplate.exchange(
                url,
                HttpMethod.GET,
                tokenUtil.createUserAuthEntity(null),
                new ParameterizedTypeReference<>() {
                }
        );

        assertThat(entity).isNotNull();

        assertThat(entity.getStatusCode())
                .isNotNull()
                .isEqualTo(HttpStatus.OK);

        assertThat(entity.getBody())
                .isNotEmpty()
                .hasSize(1)
                .contains(clientRequestSaved);
    }

    @Test
    @DisplayName("searchMyRequests return empty page object when does not match")
    void searchMyRequests_ReturnEmptyPage_WhenDoesNotMatch() {
        String url = String.format("/requests/user/my-requests?drinkUUID=%s", UUID.randomUUID());

        ResponseEntity<PageableResponse<ClientRequest>> entity = testRestTemplate.exchange(
                url,
                HttpMethod.GET,
                tokenUtil.createUserAuthEntity(null),
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
    @DisplayName("getMyTopFiveDrinks returns client request drinks count when successful")
    void getMyTopFiveDrinks_ReturnsClientRequestDrinksCount_WhenSuccessful() {
        ClientRequest clientRequestSaved = persistClientRequest(applicationUserRepository.findByEmail(ApplicationUserCreator.createApplicationUser().getEmail()).get());

        ResponseEntity<List<ClientRequestDrinkCount>> entity = testRestTemplate.exchange(
                "/requests/user/top-five-drinks",
                HttpMethod.GET,
                tokenUtil.createUserAuthEntity(null),
                new ParameterizedTypeReference<>() {
                }
        );

        assertThat(entity).isNotNull();

        assertThat(entity.getStatusCode())
                .isNotNull()
                .isEqualTo(HttpStatus.OK);

        assertThat(entity.getBody())
                .isNotEmpty()
                .hasSize(1);

        assertThat(entity.getBody().get(0)).isNotNull();

        assertThat(entity.getBody().get(0).getDrinkUUID())
                .isNotNull()
                .isEqualTo(clientRequestSaved.getDrinks().get(0).getUuid());
    }

    @Test
    @DisplayName("getTopFiveDrinks returns client request drinks count when successful")
    void getTopFiveDrinks_ReturnsClientRequestDrinksCount_WhenSuccessful() {
        ApplicationUser applicationUser = applicationUserRepository.findByEmail(ApplicationUserCreator.createApplicationUser().getEmail()).get();

        ClientRequest clientRequestSaved = persistClientRequest(applicationUser);

        ResponseEntity<List<ClientRequestDrinkCount>> entity = testRestTemplate.exchange(
                "/requests/admin/top-five-drinks/{uuid}",
                HttpMethod.GET,
                tokenUtil.createAdminAuthEntity(null),
                new ParameterizedTypeReference<>() {
                },
                applicationUser.getUuid()
        );

        assertThat(entity).isNotNull();

        assertThat(entity.getStatusCode())
                .isNotNull()
                .isEqualTo(HttpStatus.OK);

        assertThat(entity.getBody())
                .isNotEmpty()
                .hasSize(1);

        assertThat(entity.getBody().get(0)).isNotNull();

        assertThat(entity.getBody().get(0).getDrinkUUID())
                .isNotNull()
                .isEqualTo(clientRequestSaved.getDrinks().get(0).getUuid());
    }

    @Test
    @DisplayName("getTotalOfDrinksGroupedByAlcoholic returns total of client requests grouped by alcoholic when successful")
    void getTotalOfDrinksGroupedByAlcoholic_ReturnsTotalOfClientRequestsGroupedByAlcoholic_WhenSuccessful() {
        ClientRequest clientRequestSaved = persistClientRequest(applicationUserRepository.findByEmail(ApplicationUserCreator.createApplicationUser().getEmail()).get());

        ResponseEntity<List<ClientRequestAlcoholicDrinkCount>> entity = testRestTemplate.exchange(
                "/requests/user/total-of-drinks-alcoholic",
                HttpMethod.GET,
                tokenUtil.createUserAuthEntity(null),
                new ParameterizedTypeReference<>() {
                }
        );

        assertThat(entity).isNotNull();

        assertThat(entity.getStatusCode())
                .isNotNull()
                .isEqualTo(HttpStatus.OK);

        assertThat(entity.getBody())
                .isNotEmpty()
                .hasSize(1);

        assertThat(entity.getBody().get(0)).isNotNull();

        assertThat(entity.getBody().get(0).isAlcoholic()).isEqualTo(clientRequestSaved.getDrinks().get(0).isAlcoholic());
    }

    @Test
    @DisplayName("getTopDrinksInRequests returns client request drinks count of all users when successful")
    void getTopDrinksInRequests_ReturnsClientRequestDrinksCountOfAllUsers_WhenSuccessful() {
        ClientRequest clientRequestSaved = persistClientRequest();

        ResponseEntity<List<ClientRequestDrinkCount>> entity = testRestTemplate.exchange(
                "/requests/top-drinks",
                HttpMethod.GET,
                tokenUtil.createAdminAuthEntity(null),
                new ParameterizedTypeReference<>() {
                }
        );

        assertThat(entity).isNotNull();

        assertThat(entity.getStatusCode())
                .isNotNull()
                .isEqualTo(HttpStatus.OK);

        assertThat(entity.getBody())
                .isNotEmpty()
                .hasSize(1);

        assertThat(entity.getBody().get(0)).isNotNull();

        assertThat(entity.getBody().get(0).getDrinkUUID())
                .isNotNull()
                .isEqualTo(clientRequestSaved.getDrinks().get(0).getUuid());
    }

    @Test
    @DisplayName("mostCanceledDrinks returns client request drinks count of most canceled requests when successful")
    void mostCanceledDrinks_ReturnsClientRequestDrinksCountOfMostCanceledRequests_WhenSuccessful() {
        ClientRequest clientRequestSaved = persistClientRequest();

        clientRequestSaved.setStatus(ClientRequestStatus.CANCELED);

        clientRequestSaved = clientRequestRepository.save(clientRequestSaved);

        ResponseEntity<List<ClientRequestDrinkCount>> entity = testRestTemplate.exchange(
                "/requests/admin/most-canceled",
                HttpMethod.GET,
                tokenUtil.createAdminAuthEntity(null),
                new ParameterizedTypeReference<>() {
                }
        );

        assertThat(entity).isNotNull();

        assertThat(entity.getStatusCode())
                .isNotNull()
                .isEqualTo(HttpStatus.OK);


        assertThat(entity.getBody())
                .isNotEmpty()
                .hasSize(1);

        assertThat(entity.getBody().get(0)).isNotNull();

        assertThat(entity.getBody().get(0).getDrinkUUID())
                .isNotNull()
                .isEqualTo(clientRequestSaved.getDrinks().get(0).getUuid());
    }

    @Test
    @DisplayName("getAllDatesInRequests returns all dates in requests when successful")
    void getAllDatesInRequests_ReturnsAllDatesInRequests_WhenSuccessful() {
        ClientRequest clientRequestSaved = persistClientRequest(applicationUserRepository.findByEmail(ApplicationUserCreator.createApplicationUser().getEmail()).get());

        ResponseEntity<List<ClientRequestDate>> entity = testRestTemplate.exchange(
                "/requests/admin/all-dates",
                HttpMethod.GET,
                tokenUtil.createAdminAuthEntity(null),
                new ParameterizedTypeReference<>() {
                }
        );

        assertThat(entity).isNotNull();

        assertThat(entity.getStatusCode())
                .isNotNull()
                .isEqualTo(HttpStatus.OK);

        assertThat(entity.getBody())
                .isNotEmpty()
                .hasSize(1);

        assertThat(entity.getBody().get(0)).isNotNull();

        assertThat(entity.getBody().get(0).getDate())
                .isNotNull()
                .isEqualTo(clientRequestSaved.getCreatedAt().toLocalDate());
    }

    @Test
    @DisplayName("toggleBlockAllRequests set blockAllRequests to true value when value is false")
    void toggleBlockAllRequests_SetBlockAllRequestsToTrue_WhenValueIsFalse() {
        ResponseEntity<Boolean> entity = testRestTemplate.exchange(
                "/requests/admin/toggle-all-blocked",
                HttpMethod.PATCH,
                tokenUtil.createAdminAuthEntity(null),
                Boolean.class
        );

        assertThat(entity).isNotNull();

        assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.OK);

        assertThat(entity.getBody()).isTrue();
    }

    @Test
    @DisplayName("toggleBlockAllRequests set blockAllRequests to false value when value is true")
    void toggleBlockAllRequests_SetBlockAllRequestsToFalse_WhenValueIsTrue() {
        testRestTemplate.exchange(
                "/requests/admin/toggle-all-blocked",
                HttpMethod.PATCH,
                tokenUtil.createAdminAuthEntity(null),
                Boolean.class
        );

        ResponseEntity<Boolean> entity = testRestTemplate.exchange(
                "/requests/admin/toggle-all-blocked",
                HttpMethod.PATCH,
                tokenUtil.createAdminAuthEntity(null),
                Boolean.class
        );

        assertThat(entity).isNotNull();

        assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.OK);

        assertThat(entity.getBody()).isFalse();
    }

    @Test
    @DisplayName("getAllBlocked returns boolean of all users is blocked when successful")
    void getAllBlocked_ReturnsBooleanOfAllUsersIsBlocked_WhenSuccessful() {
        ResponseEntity<Boolean> entity = testRestTemplate.exchange(
                "/requests/all/all-blocked",
                HttpMethod.GET,
                tokenUtil.createUserAuthEntity(null),
                Boolean.class
        );

        assertThat(entity).isNotNull();

        assertThat(entity.getStatusCode())
                .isNotNull()
                .isEqualTo(HttpStatus.OK);

        assertThat(entity.getBody()).isFalse();
    }

    @Test
    @DisplayName("save creates client request when successful")
    void save_CreatesClientRequest_WhenSuccessful() {
        Drink drinkSaved = drinkRepository.save(DrinkCreator.createDrinkToBeSave());

        Table tableSaved = tableRepository.save(TableCreator.createTableToBeSave());

        ClientRequestPostRequestBody clientRequestValid = ClientRequestPostRequestBodyCreator.createClientRequestPostRequestBodyToBeSave();

        clientRequestValid.setDrinks(new ArrayList<>(List.of(drinkSaved)));

        clientRequestValid.setTable(tableSaved);

        ResponseEntity<ClientRequest> entity = testRestTemplate.postForEntity(
                "/requests/user",
                tokenUtil.createUserAuthEntity(clientRequestValid),
                ClientRequest.class
        );

        assertThat(entity).isNotNull();

        assertThat(entity.getStatusCode())
                .isNotNull()
                .isEqualTo(HttpStatus.CREATED);

        assertThat(entity.getBody()).isNotNull();

        assertThat(entity.getBody().getUuid()).isNotNull();

        assertThat(entity.getBody().getDrinks()).isNotEmpty();

        assertThat(entity.getBody().getDrinks().get(0)).isEqualTo(clientRequestValid.getDrinks().get(0));
    }

    @Test
    @DisplayName("save returns 400 BadRequest when user is minor and tries to buy an alcoholic drink")
    void save_Returns400BadRequest_WhenUserIsMinorAndTriesToBuyAnAlcoholicDrink() {
        Drink drinkToBeSave = DrinkCreator.createDrinkToBeSave();

        drinkToBeSave.setAlcoholic(true);

        Drink drinkSaved = drinkRepository.save(drinkToBeSave);

        Table tableSaved = tableRepository.save(TableCreator.createTableToBeSave());

        ClientRequestPostRequestBody clientRequestValid = ClientRequestPostRequestBodyCreator.createClientRequestPostRequestBodyToBeSave();

        clientRequestValid.setDrinks(new ArrayList<>(List.of(drinkSaved)));

        clientRequestValid.setTable(tableSaved);

        ResponseEntity<BadRequestExceptionDetails> entity = testRestTemplate.postForEntity(
                "/requests/user",
                tokenUtil.createUserMinorAuthEntity(clientRequestValid),
                BadRequestExceptionDetails.class
        );

        assertThat(entity).isNotNull();

        assertThat(entity.getStatusCode())
                .isNotNull()
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("save returns 400 BadRequest when requests is locked")
    void save_Returns400BadRequest_WhenRequestsIsLocked() {
        ClientRequest clientRequestValid = persistClientRequest(applicationUserRepository.save(ApplicationUserCreator.createApplicationUserWithRequestsLocked()));

        ResponseEntity<BadRequestExceptionDetails> entity = testRestTemplate.postForEntity(
                "/requests/user",
                tokenUtil.createAdminAuthEntity(clientRequestValid),
                BadRequestExceptionDetails.class
        );

        assertThat(entity).isNotNull();

        assertThat(entity.getStatusCode())
                .isNotNull()
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("save returns 400 BadRequest when all requests is blocked")
    void save_Returns400BadRequest_WhenAllRequestsIsBlocked() {
        ClientRequest clientRequestValid = persistClientRequest(applicationUserRepository.save(ApplicationUserCreator.createApplicationUserWithRequestsLocked()));

        testRestTemplate.patchForObject(
                "/requests/admin/toggle-all-blocked",
                tokenUtil.createAdminAuthEntity(null),
                Boolean.class
        );

        ResponseEntity<BadRequestExceptionDetails> entity = testRestTemplate.postForEntity(
                "/requests/user",
                tokenUtil.createAdminAuthEntity(clientRequestValid),
                BadRequestExceptionDetails.class
        );

        assertThat(entity).isNotNull();

        assertThat(entity.getStatusCode())
                .isNotNull()
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("save returns 403 Forbidden when user does not have ROLE_USER")
    void save_Returns403Forbidden_WhenUserDoesNotHaveROLE_USER() {
        Drink drinkSaved = drinkRepository.save(DrinkCreator.createDrinkToBeSave());

        Table tableSaved = tableRepository.save(TableCreator.createTableToBeSave());

        ClientRequestPostRequestBody clientRequestValid = ClientRequestPostRequestBodyCreator.createClientRequestPostRequestBodyToBeSave();

        clientRequestValid.setDrinks(new ArrayList<>(List.of(drinkSaved)));

        clientRequestValid.setTable(tableSaved);

        ResponseEntity<Object> entity = testRestTemplate.postForEntity(
                "/requests/user",
                tokenUtil.createBarmenAuthEntity(clientRequestValid),
                Object.class
        );

        assertThat(entity).isNotNull();

        assertThat(entity.getStatusCode())
                .isNotNull()
                .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("replace updates client request when successful")
    void replace_UpdatedClientRequest_WhenSuccessful() {
        ClientRequest clientRequestSaved = persistClientRequest();

        ClientRequestPutRequestBody clientToUpdate = ClientRequestPutRequestBodyCreator.createClientRequestPutRequestBodyCreatorToBeUpdate();

        clientToUpdate.setUuid(clientRequestSaved.getUuid());

        clientToUpdate.setDrinks(clientRequestSaved.getDrinks());

        clientToUpdate.setTable(clientRequestSaved.getTable());

        ResponseEntity<Void> entity = testRestTemplate.exchange(
                "/requests/admin",
                HttpMethod.PUT,
                tokenUtil.createAdminAuthEntity(clientRequestSaved),
                Void.class
        );

        assertThat(entity).isNotNull();

        assertThat(entity.getStatusCode())
                .isNotNull()
                .isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    @DisplayName("replace returns 400 BadRequest when client request not exists")
    void replace_Returns400BadRequest_WhenClientRequestNotExists() {
        ClientRequestPutRequestBody clientToUpdate = ClientRequestPutRequestBodyCreator.createClientRequestPutRequestBodyCreatorToBeUpdate();

        ResponseEntity<Void> entity = testRestTemplate.exchange(
                "/requests/admin",
                HttpMethod.PUT,
                tokenUtil.createAdminAuthEntity(clientToUpdate),
                Void.class
        );

        assertThat(entity).isNotNull();

        assertThat(entity.getStatusCode())
                .isNotNull()
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("startRequest start client request when successful")
    void startRequest_StartClientRequest_WhenSuccessful() {
        ClientRequest clientRequestSaved = persistClientRequest();

        ResponseEntity<ClientRequest> entity = testRestTemplate.exchange(
                "/requests/staff/start/{uuid}",
                HttpMethod.PATCH,
                tokenUtil.createWaiterAuthEntity(null),
                ClientRequest.class,
                clientRequestSaved.getUuid()
        );

        assertThat(entity).isNotNull();

        assertThat(entity.getStatusCode())
                .isNotNull()
                .isEqualTo(HttpStatus.OK);

        assertThat(entity.getBody()).isNotNull();

        assertThat(entity.getBody().getUuid())
                .isNotNull()
                .isEqualTo(clientRequestSaved.getUuid());

        assertThat(entity.getBody().getStatus()).isEqualTo(ClientRequestStatus.STARTED);
    }

    @Test
    @DisplayName("startRequest returns 400 BadRequest when client request is not processing")
    void startRequest_Returns400BadRequest_WhenClientRequestIsNotProcessing() {
        ClientRequest clientRequestSaved = persistClientRequest();

        clientRequestSaved.setStatus(ClientRequestStatus.STARTED);

        ClientRequest clientRequestStarted = clientRequestRepository.save(clientRequestSaved);

        ResponseEntity<BadRequestExceptionDetails> entity = testRestTemplate.exchange(
                "/requests/staff/start/{uuid}",
                HttpMethod.PATCH,
                tokenUtil.createWaiterAuthEntity(null),
                BadRequestExceptionDetails.class,
                clientRequestStarted.getUuid()
        );

        assertThat(entity).isNotNull();

        assertThat(entity.getStatusCode())
                .isNotNull()
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("startRequest returns 400 BadRequest when client request not exists")
    void startRequest_Returns400BadRequest_WhenClientRequestNotExists() {
        ResponseEntity<BadRequestExceptionDetails> entity = testRestTemplate.exchange(
                "/requests/staff/start/{uuid}",
                HttpMethod.PATCH,
                tokenUtil.createWaiterAuthEntity(null),
                BadRequestExceptionDetails.class,
                UUID.randomUUID()
        );

        assertThat(entity).isNotNull();

        assertThat(entity.getStatusCode())
                .isNotNull()
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("finishRequest finish client request when successful")
    void finishRequest_FinishClientRequest_WhenSuccessful() {
        ClientRequest clientRequestSaved = persistClientRequest();

        ResponseEntity<ClientRequest> entity = testRestTemplate.exchange(
                "/requests/staff/finish/{uuid}",
                HttpMethod.PATCH,
                tokenUtil.createWaiterAuthEntity(null),
                ClientRequest.class,
                clientRequestSaved.getUuid()
        );

        assertThat(entity).isNotNull();

        assertThat(entity.getStatusCode())
                .isNotNull()
                .isEqualTo(HttpStatus.OK);

        assertThat(entity.getBody()).isNotNull();

        assertThat(entity.getBody().getUuid())
                .isNotNull()
                .isEqualTo(clientRequestSaved.getUuid());

        assertThat(entity.getBody().getStatus()).isEqualTo(ClientRequestStatus.FINISHED);
    }

    @Test
    @DisplayName("finishRequest returns 400 BadRequest when client request already finished")
    void finishRequest_Returns400BadRequest_WhenClientRequestAlreadyFinished() {
        ClientRequest clientRequestSaved = persistClientRequest();

        clientRequestSaved.setStatus(ClientRequestStatus.FINISHED);

        ClientRequest clientRequestFinished = clientRequestRepository.save(clientRequestSaved);

        ResponseEntity<BadRequestExceptionDetails> entity = testRestTemplate.exchange(
                "/requests/staff/finish/{uuid}",
                HttpMethod.PATCH,
                tokenUtil.createWaiterAuthEntity(null),
                BadRequestExceptionDetails.class,
                clientRequestFinished.getUuid()
        );

        assertThat(entity).isNotNull();

        assertThat(entity.getStatusCode())
                .isNotNull()
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("finishRequest returns 400 BadRequest when client request is canceled")
    void finishRequest_Returns400BadRequest_WhenClientRequestIsCanceled() {
        ClientRequest clientRequestSaved = persistClientRequest();

        clientRequestSaved.setStatus(ClientRequestStatus.CANCELED);

        ClientRequest clientRequestFinished = clientRequestRepository.save(clientRequestSaved);

        ResponseEntity<BadRequestExceptionDetails> entity = testRestTemplate.exchange(
                "/requests/staff/finish/{uuid}",
                HttpMethod.PATCH,
                tokenUtil.createWaiterAuthEntity(null),
                BadRequestExceptionDetails.class,
                clientRequestFinished.getUuid()
        );

        assertThat(entity).isNotNull();

        assertThat(entity.getStatusCode())
                .isNotNull()
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("finishRequest returns 400 BadRequest when client request not exists")
    void finishRequest_Returns400BadRequest_WhenClientRequestNotExists() {
        ResponseEntity<BadRequestExceptionDetails> entity = testRestTemplate.exchange(
                "/requests/staff/finish/{uuid}",
                HttpMethod.PATCH,
                tokenUtil.createWaiterAuthEntity(null),
                BadRequestExceptionDetails.class,
                UUID.randomUUID()
        );

        assertThat(entity).isNotNull();

        assertThat(entity.getStatusCode())
                .isNotNull()
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("cancelRequest cancel client request when successful")
    void cancelRequest_CancelClientRequest_WhenSuccessful() {
        ClientRequest clientRequestSaved = persistClientRequest();

        ResponseEntity<ClientRequest> entity = testRestTemplate.exchange(
                "/requests/all/cancel/{uuid}",
                HttpMethod.PATCH,
                tokenUtil.createWaiterAuthEntity(null),
                ClientRequest.class,
                clientRequestSaved.getUuid()
        );

        assertThat(entity).isNotNull();

        assertThat(entity.getStatusCode())
                .isNotNull()
                .isEqualTo(HttpStatus.OK);

        assertThat(entity.getBody()).isNotNull();

        assertThat(entity.getBody().getUuid())
                .isNotNull()
                .isEqualTo(clientRequestSaved.getUuid());

        assertThat(entity.getBody().getStatus()).isEqualTo(ClientRequestStatus.CANCELED);
    }

    @Test
    @DisplayName("cancelRequest returns 400 BadRequest when client request already canceled")
    void cancelRequest_Returns400BadRequest_WhenClientRequestAlreadyCanceled() {
        ClientRequest clientRequestSaved = persistClientRequest();

        clientRequestSaved.setStatus(ClientRequestStatus.CANCELED);

        ClientRequest clientRequestFinished = clientRequestRepository.save(clientRequestSaved);

        ResponseEntity<BadRequestExceptionDetails> entity = testRestTemplate.exchange(
                "/requests/all/cancel/{uuid}",
                HttpMethod.PATCH,
                tokenUtil.createWaiterAuthEntity(null),
                BadRequestExceptionDetails.class,
                clientRequestFinished.getUuid()
        );

        assertThat(entity).isNotNull();

        assertThat(entity.getStatusCode())
                .isNotNull()
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("cancelRequest returns 400 BadRequest when client request is finished and delivered")
    void cancelRequest_Returns400BadRequest_WhenClientRequestIsFinishedAndDelivered() {
        ClientRequest clientRequestSaved = persistClientRequest();

        clientRequestSaved.setStatus(ClientRequestStatus.FINISHED);
        clientRequestSaved.setDelivered(true);

        ClientRequest clientRequestFinished = clientRequestRepository.save(clientRequestSaved);

        ResponseEntity<BadRequestExceptionDetails> entity = testRestTemplate.exchange(
                "/requests/all/cancel/{uuid}",
                HttpMethod.PATCH,
                tokenUtil.createWaiterAuthEntity(null),
                BadRequestExceptionDetails.class,
                clientRequestFinished.getUuid()
        );

        assertThat(entity).isNotNull();

        assertThat(entity.getStatusCode())
                .isNotNull()
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("cancelRequest returns 400 BadRequest when client request not exists")
    void cancelRequest_Returns400BadRequest_WhenClientRequestNotExists() {
        ResponseEntity<BadRequestExceptionDetails> entity = testRestTemplate.exchange(
                "/requests/all/cancel/{uuid}",
                HttpMethod.PATCH,
                tokenUtil.createWaiterAuthEntity(null),
                BadRequestExceptionDetails.class,
                UUID.randomUUID()
        );

        assertThat(entity).isNotNull();

        assertThat(entity.getStatusCode())
                .isNotNull()
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("cancelRequest returns 400 BadRequest when user is not staff or owner of request")
    void cancelRequest_Returns400BadRequest_WhenUserIsNotStaffOrOwnerOfRequest() {
        ClientRequest clientRequestSaved = persistClientRequest();

        ClientRequest clientRequestFinished = clientRequestRepository.save(clientRequestSaved);

        ResponseEntity<BadRequestExceptionDetails> entity = testRestTemplate.exchange(
                "/requests/all/cancel/{uuid}",
                HttpMethod.PATCH,
                tokenUtil.createUserAuthEntity(null),
                BadRequestExceptionDetails.class,
                clientRequestFinished.getUuid()
        );

        assertThat(entity).isNotNull();

        assertThat(entity.getStatusCode())
                .isNotNull()
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("deliverRequest deliver client request when successful")
    void deliverRequest_DeliverClientRequest_WhenSuccessful() {
        ClientRequest clientRequestSaved = persistClientRequest();

        clientRequestSaved.setStatus(ClientRequestStatus.FINISHED);

        ClientRequest clientRequestFinished = clientRequestRepository.save(clientRequestSaved);

        ResponseEntity<ClientRequest> entity = testRestTemplate.exchange(
                "/requests/staff/deliver/{uuid}",
                HttpMethod.PATCH,
                tokenUtil.createWaiterAuthEntity(null),
                ClientRequest.class,
                clientRequestFinished.getUuid()
        );

        assertThat(entity).isNotNull();

        assertThat(entity.getStatusCode())
                .isNotNull()
                .isEqualTo(HttpStatus.OK);

        assertThat(entity.getBody())
                .isNotNull()
                .isEqualTo(clientRequestSaved);

        assertThat(entity.getBody().isDelivered()).isTrue();
    }

    @Test
    @DisplayName("deliverRequest returns 400 BadRequest when client request is not finished")
    void deliverRequest_Returns400BadRequest_WhenClientRequestIsFinished() {
        ClientRequest clientRequestSaved = persistClientRequest();

        ResponseEntity<BadRequestExceptionDetails> entity = testRestTemplate.exchange(
                "/requests/staff/deliver/{uuid}",
                HttpMethod.PATCH,
                tokenUtil.createWaiterAuthEntity(null),
                BadRequestExceptionDetails.class,
                clientRequestSaved.getUuid()
        );

        assertThat(entity).isNotNull();

        assertThat(entity.getStatusCode())
                .isNotNull()
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("deliverRequest returns 400 BadRequest when client request not exists")
    void deliverRequest_Returns400BadRequest_WhenClientRequestNotExists() {
        ResponseEntity<BadRequestExceptionDetails> entity = testRestTemplate.exchange(
                "/requests/staff/deliver/{uuid}",
                HttpMethod.PATCH,
                tokenUtil.createWaiterAuthEntity(null),
                BadRequestExceptionDetails.class,
                UUID.randomUUID()
        );

        assertThat(entity).isNotNull();

        assertThat(entity.getStatusCode())
                .isNotNull()
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("delete removes client request when successful")
    void delete_RemovesClientRequest_WhenSuccessful() {
        ClientRequest clientRequestSaved = persistClientRequest();

        ResponseEntity<Void> entity = testRestTemplate.exchange(
                "/requests/admin/{uuid}",
                HttpMethod.DELETE,
                tokenUtil.createAdminAuthEntity(null),
                Void.class,
                clientRequestSaved.getUuid()
        );

        assertThat(entity).isNotNull();

        assertThat(entity.getStatusCode())
                .isNotNull()
                .isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    @DisplayName("delete returns 400 BadRequest when client request not exists")
    void delete_Returns400BadRequest_WhenClientRequestNotExists() {
        ResponseEntity<Void> entity = testRestTemplate.exchange(
                "/requests/admin/{uuid}",
                HttpMethod.DELETE,
                tokenUtil.createAdminAuthEntity(null),
                Void.class,
                UUID.randomUUID()
        );

        assertThat(entity).isNotNull();

        assertThat(entity.getStatusCode())
                .isNotNull()
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    private ClientRequest persistClientRequest() {
        return persistClientRequest(applicationUserRepository.save(ApplicationUserCreator.createApplicationUserToBeSave()));
    }

    private ClientRequest persistClientRequest(ApplicationUser userSaved) {
        Drink drinkSaved = drinkRepository.save(DrinkCreator.createDrinkToBeSave());

        Table tableSaved = tableRepository.save(TableCreator.createTableToBeSave());

        ClientRequest clientRequestValid = ClientRequestCreator.createClientRequestToBeSave();

        clientRequestValid.setDrinks(new ArrayList<>(List.of(drinkSaved)));

        clientRequestValid.setUser(userSaved);

        clientRequestValid.setTable(tableSaved);

        return clientRequestRepository.save(clientRequestValid);
    }

}
