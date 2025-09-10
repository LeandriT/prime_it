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

public class PartialUpdateUserSimulation extends Simulation {

    String baseUrl = System.getProperty("baseUrl", "http://localhost:8080");

    HttpProtocolBuilder httpProtocol =
            http.baseUrl(baseUrl)
                    .contentTypeHeader("application/json")
                    .acceptHeader("application/json");

    FeederBuilder.Batchable<String> userFeeder = csv("gatling/user_data_partial.csv").circular();

    ScenarioBuilder createAndPartiallyUpdateUser =
            scenario("Create and Partially Update User")
                    .feed(userFeeder)
                    .exec(session -> {
                        // Generar email aleatorio válido
                        String baseEmail = session.getString("email");
                        String[] parts = baseEmail.split("@");
                        String randomEmail = parts[0] + System.currentTimeMillis() + "@" + parts[1];
                        return session.set("email", randomEmail);
                    })
                    .exec(
                            http("Create User for Update")
                                    .post("/api/users")
                                    .body(ElFileBody("gatling/create_user_template.json"))
                                    .check(status().in(201))
                                    .check(jsonPath("$.uuid").exists().saveAs("userId"))
                    )
                    .pause(1)
                    .exec(
                            http("Partial Update User")
                                    .patch("/api/users/#{userId}")
                                    .body(ElFileBody("gatling/partial_update_user_template.json"))
                                    .check(status().is(200))
                                    .check(jsonPath("$.name").isEL("#{name}"))
                    );

    {
        setUp(
                createAndPartiallyUpdateUser.injectOpen(
                        rampUsers(5).during(10)
                )
        ).protocols(httpProtocol);
    }
}
