package com.serverless.author;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.DeleteItemRequest;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.serverless.ApiGatewayResponse;
import com.serverless.model.CommonAPIResponse;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class DeleteHandler implements RequestHandler<APIGatewayProxyRequestEvent, ApiGatewayResponse> {
    private AmazonDynamoDB amazonDynamoDB;
    private String AUTHOR_DB_TABLE = System.getenv("AUTHOR_TABLE");
    private Regions REGION = Regions.fromName(System.getenv("REGION"));

    @Override public ApiGatewayResponse handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        initDynamoDbClient();

        Map<String, AttributeValue> keyMap = new HashMap<>();
        keyMap.put("id", new AttributeValue(input.getPathParameters().get("id")));

        DeleteItemRequest request = new DeleteItemRequest()
            .withTableName(AUTHOR_DB_TABLE)
            .withKey(keyMap);
        amazonDynamoDB.deleteItem(request);

        return ApiGatewayResponse.builder()
            .setHeaders(Collections.singletonMap("Content-Type", "application/json"))
            .setObjectBody(new CommonAPIResponse("Author deletion successfully completed")).build();

    }

    private void initDynamoDbClient() {
        this.amazonDynamoDB = AmazonDynamoDBClientBuilder.standard()
            .withRegion(REGION)
            .build();
    }
}
