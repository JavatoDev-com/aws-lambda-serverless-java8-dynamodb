package com.serverless.author;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeAction;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.AttributeValueUpdate;
import com.amazonaws.services.dynamodbv2.model.UpdateItemRequest;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.serverless.ApiGatewayResponse;
import com.serverless.model.AuthorDto;
import com.serverless.model.CommonAPIResponse;
import com.serverless.util.RequestConversionUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class UpdateHandler implements RequestHandler<APIGatewayProxyRequestEvent, ApiGatewayResponse> {
    private AmazonDynamoDB amazonDynamoDB;
    private String AUTHOR_DB_TABLE = System.getenv("AUTHOR_TABLE");
    private Regions REGION = Regions.fromName(System.getenv("REGION"));

    @Override public ApiGatewayResponse handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        initDynamoDbClient();
        RequestConversionUtil requestConversionUtil = new RequestConversionUtil();
        AuthorDto request = requestConversionUtil.parseRequestBody(input.getBody(), AuthorDto.class);

        Map<String, AttributeValue> keyMap = new HashMap<>();
        keyMap.put("id", new AttributeValue(input.getPathParameters().get("id")));

        UpdateItemRequest updateItemRequest = new UpdateItemRequest()
            .withTableName(AUTHOR_DB_TABLE)
            .addKeyEntry("id", new AttributeValue(input.getPathParameters().get("id")))
            .addAttributeUpdatesEntry("firstName",
                new AttributeValueUpdate(
                    new AttributeValue(request.getFirstName()),
                    AttributeAction.PUT))
            .addAttributeUpdatesEntry("lastName",
                new AttributeValueUpdate(
                    new AttributeValue(request.getLastName()),
                    AttributeAction.PUT))
            .addAttributeUpdatesEntry("email",
                new AttributeValueUpdate(
                    new AttributeValue(request.getEmail()),
                    AttributeAction.PUT))
            .addAttributeUpdatesEntry("identification_number",
                new AttributeValueUpdate(
                    new AttributeValue(request.getIdentificationNumber()),
                    AttributeAction.PUT));

        amazonDynamoDB.updateItem(updateItemRequest);

        return ApiGatewayResponse.builder()
            .setHeaders(Collections.singletonMap("Content-Type", "application/json"))
            .setObjectBody(new CommonAPIResponse("Author update successfully completed")).build();
    }

    private void initDynamoDbClient() {
        this.amazonDynamoDB = AmazonDynamoDBClientBuilder.standard()
            .withRegion(REGION)
            .build();
    }
}
