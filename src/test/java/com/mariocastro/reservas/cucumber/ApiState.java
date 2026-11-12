package com.mariocastro.reservas.cucumber;

import io.cucumber.spring.ScenarioScope;
import io.restassured.response.Response;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@ScenarioScope
@Getter
@Setter
public class ApiState {

    private Response response;

    private String token;

    private String uuid;

    private Map<String, String> pathParam = new HashMap<>();
}
