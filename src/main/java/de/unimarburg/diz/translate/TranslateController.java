package de.unimarburg.diz.translate;

import de.numcodex.sq2cql.Translator;
import de.numcodex.sq2cql.model.structured_query.StructuredQuery;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/translate")
class TranslateController {

  private final Translator translator;

  TranslateController(Translator translator, ObjectMapper mapper) {
    this.translator = translator;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.OK)
  public ResponseEntity<?> translate(@RequestBody StructuredQuery sq) {

    var cql = translator.toCql(sq).print();

    var headers = new HttpHeaders();
    headers.add(HttpHeaders.CONTENT_TYPE, "text/cql;charset=UTF-8");
    return new ResponseEntity<>(cql, headers, HttpStatus.OK);
  }
}
