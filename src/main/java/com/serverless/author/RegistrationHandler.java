package com.serverless.author;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.serverless.ApiGatewayResponse;
import com.serverless.model.AuthorDto;
import com.serverless.model.CommonAPIResponse;
import com.serverless.util.RequestConversionUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RegistrationHandler implements RequestHandler<Map<String, Object>, ApiGatewayResponse> {

    private AmazonDynamoDB amazonDynamoDB;

    private String AUTHOR_DB_TABLE = System.getenv("AUTHOR_TABLE");
    private Regions REGION = Regions.fromName(System.getenv("REGION"));

    @Override public ApiGatewayResponse handleRequest(Map<String, Object> input, Context context) {
        RequestConversionUtil requestConversionUtil = new RequestConversionUtil();
        AuthorDto request = requestConversionUtil.parseRequestBody(input.get("body").toString(), AuthorDto.class);
        this.initDynamoDbClient();
        persistData(request);
        return ApiGatewayResponse.builder()
            .setStatusCode(201)
            .setHeaders(Collections.singletonMap("Content-Type", "application/json"))
            .setObjectBody(new CommonAPIResponse("Author registration successfully completed."))
            .build();
    }

    private String persistData(AuthorDto request) throws ConditionalCheckFailedException {
        String user_id = UUID.randomUUID().toString();
        Map<String, AttributeValue> attributesMap = new HashMap<>();
        attributesMap.put("id", new AttributeValue(user_id));
        attributesMap.put("firstName", new AttributeValue(request.getFirstName()));
        attributesMap.put("lastName", new AttributeValue(request.getLastName()));
        attributesMap.put("email", new AttributeValue(request.getEmail()));
        attributesMap.put("identification_number", new AttributeValue(request.getIdentificationNumber()));
        amazonDynamoDB.putItem(AUTHOR_DB_TABLE, attributesMap);
        return user_id;
    }

    private void initDynamoDbClient() {
        this.amazonDynamoDB = AmazonDynamoDBClientBuilder.standard()
            .withRegion(REGION)
            .build();
    }

}
