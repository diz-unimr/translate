package de.unimarburg.diz.translate;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;

import java.nio.file.Paths;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.client.RestTestClient;

@TestPropertySource(
    properties =
        "cql.ontology-file = classpath:ontology/mapping_tree.json,cql.mapping-file = classpath:ontology/mapping_cql.json")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
public class TranslateControllerTests {

  @LocalServerPort private int port;

  @Autowired private RestTestClient restTestClient;

  @DynamicPropertySource
  static void dynamicProperties(DynamicPropertyRegistry registry) {
    var ontoPath = Paths.get("src", "test", "resources", "ontology");

    registry.add(
        "cql.ontology-file",
        () -> ontoPath.resolve("mapping_tree.json").toFile().getAbsolutePath());
    registry.add(
        "cql.mapping-file", () -> ontoPath.resolve("mapping_cql.json").toFile().getAbsolutePath());
  }

  @Test
  void translateShouldReturnCql() {
    var reqBody =
        """
                        {
                          "inclusionCriteria": [
                            [
                              {
                                "termCodes": [
                                  {
                                    "code": "263495000",
                                    "display": "Geschlecht",
                                    "system": "http://snomed.info/sct",
                                    "version": ""
                                  }
                                ],
                                "context": {
                                  "code": "Patient",
                                  "display": "Patient",
                                  "system": "fdpg.mii.cds",
                                  "version": "1.0.0"
                                },
                                "valueFilter": {
                                  "selectedConcepts": [
                                    {
                                      "code": "male",
                                      "display": "Male",
                                      "system": "http://hl7.org/fhir/administrative-gender"
                                    }
                                  ],
                                  "type": "concept"
                                }
                              }
                            ]
                          ]
                        }""";

    restTestClient
        .post()
        .uri("http://localhost:%d/translate".formatted(port))
        .body(reqBody)
        .header(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .exchange()
        .expectBody(String.class)
        .isEqualTo(
            """
                                library Retrieve version '1.0.0'
                                using FHIR version '4.0.0'
                                include FHIRHelpers version '4.0.0'

                                context Patient

                                define Criterion:
                                  Patient.gender = 'male'

                                define InInitialPopulation:
                                  Criterion
                                """);
  }
}
