package de.unimarburg.diz.translate;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.RestTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
public class TranslateControllerTests {

  @LocalServerPort private int port;

  @Autowired private RestTestClient restTestClient;

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
