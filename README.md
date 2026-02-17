# 🌎 translate
[![MegaLinter](https://github.com/diz-unimr/translate/actions/workflows/mega-linter.yaml/badge.svg)](https://github.com/diz-unimr/translate/actions/workflows/mega-linter.yaml)
[![CodeQL](https://github.com/diz-unimr/translate/actions/workflows/codeql.yaml/badge.svg)](https://github.com/diz-unimr/translate/actions/workflows/codeql.yaml)
[![build](https://github.com/diz-unimr/translate/actions/workflows/build.yaml/badge.svg)](https://github.com/diz-unimr/translate/actions/workflows/build.yaml)
[![release](https://github.com/diz-unimr/translate/actions/workflows/release.yaml/badge.svg)](https://github.com/diz-unimr/translate/actions/workflows/release.yaml)
[![codecov](https://codecov.io/gh/diz-unimr/translate/graph/badge.svg?token=iO1O2rIcX0)](https://codecov.io/gh/diz-unimr/translate)

> Feasibility translation service

This service provides `StructuredQuery` to [CQL (Clinical Quality Language)](https://build.fhir.org/ig/HL7/cql/) translation by using the [MII - CCDL to CQL Translator](https://github.com/medizininformatik-initiative/sq2cql).

## API

The RESTful API comprises a single endpoint to translate a feasibility query in the StructuredQuery format to CQL:

### <code>POST</code> <code><b>/translate</b></code> <code>(translate query to CQL)</code>

#### Request

##### Body

> | content-type          | value             | description     |
> |-----------------------|-------------------|-----------------|
> | `application/sq+json` | `StructuredQuery` | The input query |

#### Responses

> | http code                   | content-type               | response      |
> |-----------------------------|----------------------------|---------------|
> | `200` Ok                    | `text/cql;charset=UTF-8`   | The CQL code  |
> | `500` Internal Server Error | `text/plain;charset=UTF-8` | Error message |


## Configuration properties

The following environment variables can be set:

| Variable          | Default                    | Description        |
|-------------------|----------------------------|--------------------|
| cql.ontology-file | ontology/mapping_tree.json | Ontology tree file |
| cql.mappings-file | ontology/mapping_cql.json  | CQL mappings file  |

Additional application properties can be set by overriding values form the [application.yaml](src/main/resources/application.yaml) with using environment variables.

## License

[AGPL-3.0](https://www.gnu.org/licenses/agpl-3.0.en.html)
