package computerdatabase;

import static io.gatling.javaapi.core.CoreDsl.ElFileBody;
import static io.gatling.javaapi.core.CoreDsl.csv;
import static io.gatling.javaapi.core.CoreDsl.jsonPath;
import static io.gatling.javaapi.core.CoreDsl.rampUsers;
import static io.gatling.javaapi.core.CoreDsl.scenario;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

import io.gatling.javaapi.core.FeederBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

public class CreateUserSimulation extends Simulation {

    String baseUrl = System.getProperty("baseUrl", "http://localhost:8080");

    HttpProtocolBuilder httpProtocol =
            http.baseUrl(baseUrl)
                    .contentTypeHeader("application/json")
                    .acceptHeader("application/json");

    // Ruta corregida: el archivo CSV debe estar en src/main/resources
    FeederBuilder.Batchable<String> userFeeder = csv("gatling/user_data.csv").random();

    // Scenario to create and read users with dynamic data
    ScenarioBuilder createAndReadUsers =
            scenario("Create and Read Users")
                    .feed(userFeeder)
                    .exec(
                            http("Create User")
                                    // Ruta corregida: el archivo de plantilla JSON debe estar en
                                    // src/main/resources/gatling/bodies
                                    .post("/api/users")
                                    .body(ElFileBody("gatling/create_user_template.json"))
                                    .check(status().in(201))
                                    .check(jsonPath("$.uuid").exists().saveAs("userId"))
                    )
                    .pause(1)
                    .exec(
                            http("Get Created User")
                                    .get("/api/users/#{userId}")
                                    .check(status().is(200))
                                    .check(jsonPath("$.uuid").isEL("#{userId}"))
                    );

    {
        setUp(
                createAndReadUsers.injectOpen(
                        rampUsers(5).during(10)
                )
        ).protocols(httpProtocol);
    }
}