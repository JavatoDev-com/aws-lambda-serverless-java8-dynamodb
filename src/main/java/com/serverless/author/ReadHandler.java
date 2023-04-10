package com.serverless.author;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.GetItemRequest;
import com.amazonaws.services.dynamodbv2.model.GetItemResult;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.serverless.ApiGatewayResponse;
import com.serverless.model.AuthorDto;
import com.serverless.model.CommonAPIResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReadHandler implements RequestHandler<APIGatewayProxyRequestEvent, ApiGatewayResponse> {

    private AmazonDynamoDB amazonDynamoDB;
    private String AUTHOR_DB_TABLE = System.getenv("AUTHOR_TABLE");
    private Regions REGION = Regions.fromName(System.getenv("REGION"));


    @Override public ApiGatewayResponse handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        this.initDynamoDbClient();
        Map<String, String> queryParams = input.getQueryStringParameters();

        if (queryParams != null && queryParams.containsKey("findAll") && Boolean.parseBoolean(queryParams.get("findAll"))) {

            //Find All
            Map<String, AttributeValue> lastKeyEvaluated = null;
            List<AuthorDto> authorDtos = new ArrayList<>();
            do {
                ScanRequest scanRequest = new ScanRequest()
                    .withTableName(AUTHOR_DB_TABLE)
                    .withLimit(10)
                    .withExclusiveStartKey(lastKeyEvaluated);
                ScanResult result = amazonDynamoDB.scan(scanRequest);
                for (Map<String, AttributeValue> item : result.getItems()) {
                    authorDtos.add(mapToDto(item));
                }
                lastKeyEvaluated = result.getLastEvaluatedKey();
            } while (lastKeyEvaluated != null);

            return ApiGatewayResponse.builder()
                .setHeaders(Collections.singletonMap("Content-Type", "application/json"))
                .setObjectBody(authorDtos).setStatusCode(200).build();

        } else if (queryParams!= null && queryParams.containsKey("id") && queryParams.get("id") != null) {

            //Find by id
            Map<String, AttributeValue> attributesMap = new HashMap<>();
            attributesMap.put("id", new AttributeValue(queryParams.get("id")));
            GetItemRequest getItemRequest = new GetItemRequest().withTableName(AUTHOR_DB_TABLE)
                .withKey(attributesMap);
            GetItemResult item = amazonDynamoDB.getItem(getItemRequest);

            return ApiGatewayResponse.builder()
                .setHeaders(Collections.singletonMap("Content-Type", "application/json"))
                .setObjectBody(mapToDto(item.getItem())).setStatusCode(200).build();

        }

        return ApiGatewayResponse.builder()
            .setHeaders(Collections.singletonMap("Content-Type", "application/json"))
            .setObjectBody(new CommonAPIResponse("No data found under given query"))
            .setStatusCode(200).build();

    }

    private AuthorDto mapToDto(Map<String, AttributeValue> item) {
        AuthorDto authorDto = new AuthorDto();
        authorDto.setId(item.get("id").getS());
        authorDto.setEmail(item.get("email").getS());
        authorDto.setFirstName(item.get("firstName").getS());
        authorDto.setLastName(item.get("lastName").getS());
        authorDto.setIdentificationNumber(item.get("identification_number").getS());
        return authorDto;
    }

    private void initDynamoDbClient() {
        this.amazonDynamoDB = AmazonDynamoDBClientBuilder.standard()
            .withRegion(REGION)
            .build();
    }

}
