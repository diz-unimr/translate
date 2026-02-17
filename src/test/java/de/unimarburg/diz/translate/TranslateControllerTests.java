package de.unimarburg.diz.translate;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;

import java.io.File;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.client.RestTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
public class TranslateControllerTests {

  @LocalServerPort private int port;

  @Autowired private RestTestClient restTestClient;

  @DynamicPropertySource
  static void dynamicProperties(DynamicPropertyRegistry registry) {
    var classLoader = TranslateControllerTests.class.getClassLoader();

    var onto_file =
        new File(classLoader.getResource("ontology/mapping_tree.json").getFile()).getAbsolutePath();
    var mapping_file =
        new File(classLoader.getResource("ontology/mapping_cql.json").getFile()).getAbsolutePath();

    registry.add("cql.ontology-file", () -> onto_file);
    registry.add("cql.mapping-file", () -> mapping_file);
  }

  @TestConfiguration
  static class TranslateConfig {

    @Value("${cql.mappings-file}")
    private String mappingsFile;

    @Value("${cql.ontology-file}")
    private String ontologyFile;
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
