package com.ezbookkeeping.qa.api.client;

import com.ezbookkeeping.qa.config.AppConfig;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

import static io.restassured.RestAssured.given;

public abstract class ClientBase {

    protected RequestSpecification baseSpec() {
        return given()
                .contentType(ContentType.JSON)
                .header("X-Timezone-Name", AppConfig.DEFAULT_TIMEZONE)
                .auth().none();
    }
}