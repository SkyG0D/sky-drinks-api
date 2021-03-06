package com.github.skyg0d.skydrinksapi.parameters;

import com.github.skyg0d.skydrinksapi.enums.ClientRequestStatus;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class ClientRequestParameters {

    @Parameter(description = "UUID do drink para pesquisa, irá procurar todos os pedidos que possuem esse drink", example = "91e3d7a9-83cd-4e28-ae3e-c8e9f75f8b18", allowEmptyValue = true)
    private UUID drinkUUID;

    @Parameter(description = "Nome do drink para pesquisa, irá procurar todos os pedidos que possuem o nome desse drink", example = "Blood Mary", allowEmptyValue = true)
    private String drinkName;

    @Parameter(description = "Descrição do drink para pesquisa, irá procurar todos os pedidos que possuem a descrição desse drink", example = "Drink Refrescante", allowEmptyValue = true)
    private String drinkDescription;

    @Parameter(description = "UUID do usuário para pesquisa, irá procurar todos os pedidos que possuem esse usuário", example = "a5e0f154-73ce-4560-b3da-ea51e11d9809", allowEmptyValue = true)
    private UUID userUUID;

    @Parameter(description = "Nome do usuário para pesquisa, irá procurar todos os pedidos que possuem o nome desse usuário", example = "Roger", allowEmptyValue = true)
    private String userName;

    @Parameter(description = "Email do usuário para pesquisa, irá procurar todos os pedidos que possuem o email desse usuário", example = "roger@mail.com", allowEmptyValue = true)
    private String userEmail;

    @Parameter(description = "CPF do usuário para pesquisa, irá procurar todos os pedidos que possuem o CPF desse usuário", example = "359.425.366-00", allowEmptyValue = true)
    private String userCpf;

    @Parameter(description = "UUID da mesa para pesquisa", example = "b6ffe300-cfd7-46c0-80e4-070998096cbf", allowEmptyValue = true)
    private String tableUUID = "";

    @Parameter(description = "Data que o pedido foi criado", example = "2004-04-04T10:16:28.043216", allowEmptyValue = true)
    private String createdAt;

    @Parameter(description = "Data que o pedido foi criado ou depois disso", example = "2004-04-04", allowEmptyValue = true)
    private String createdInDateOrAfter;

    @Parameter(description = "Data que o pedido foi criado ou antes disso", example = "2004-04-04", allowEmptyValue = true)
    private String createdInDateOrBefore;

    @Parameter(
            description = "Filtra pelo status do pedido",
            example = "PROCESSING",
            allowEmptyValue = true
    )
    private ClientRequestStatus status;

    @Parameter(description = "Preço total do pedido", example = "25.45", allowEmptyValue = true)
    private double totalPrice = -1;

    @Parameter(description = "Preço total ou maior que isso do pedido", example = "25.45", allowEmptyValue = true)
    private double greaterThanTotalPrice = -1;

    @Parameter(description = "Preço total ou menor que isso do pedido", example = "25.45", allowEmptyValue = true)
    private double lessThanTotalPrice = -1;

    @Parameter(description = "Preço total, maior que isso ou igual a isso do pedido", example = "25.45", allowEmptyValue = true)
    private double greaterThanOrEqualToTotalPrice = -1;

    @Parameter(description = "Preço total, menor que isso ou igual a isso do pedido", example = "25.45", allowEmptyValue = true)
    private double lessThanOrEqualToTotalPrice = -1;

    @Parameter(
            description = "Se o valor for igual a um, pesquisará todas os pedidos recebidos, caso seja zero, pesquisa todas os pedidos não recebidos, e caso seja menos um, pesquisa ambos",
            example = "-1",
            allowEmptyValue = true
    )
    private int delivered = -1;

}
