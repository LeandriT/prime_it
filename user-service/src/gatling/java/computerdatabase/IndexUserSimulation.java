package computerdatabase;

import static io.gatling.javaapi.core.CoreDsl.rampUsers;
import static io.gatling.javaapi.core.CoreDsl.scenario;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

public class IndexUserSimulation extends Simulation {

    String baseUrl = System.getProperty("baseUrl", "http://localhost:8080");

    HttpProtocolBuilder httpProtocol =
            http.baseUrl(baseUrl)
                    .contentTypeHeader("application/json")
                    .acceptHeader("application/json");

    ScenarioBuilder scn =
            scenario("List_Users_Paginated")
                    .exec(
                            http("GET /api/users?page=0&size=10")
                                    .get("/api/users")
                                    .queryParam("page", "0")
                                    .queryParam("size", "10")
                                    .check(status().is(200))
                    );

    // Constructor donde se configura la simulación
    {
        setUp(
                scn.injectOpen(
                        rampUsers(50).during(30) // 50 usuarios en 30 segundos
                )
        ).protocols(httpProtocol);
    }
}