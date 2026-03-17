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


## Ontology

The [MII - CCDL to CQL Translator](https://github.com/medizininformatik-initiative/sq2cql) needs an ontology
(or mapping tree) file and a mapping (file) of concepts (StructuredQuery) to FHIR path in order to translate criteria to CQL.

Alternatively those files can be provided by a GitLab package registry. However, this is currently hard coded to the
Marburg GitLab package registry.

See configuration properties for the respective variables.

## Configuration properties

The following environment variables can be set:

| Variable                              | Default                    | Description                                   |
|---------------------------------------|----------------------------|-----------------------------------------------|
| cql.ontology.local.ontology-file      | ontology/mapping_tree.json | Ontology tree file (local)                    |
| cql.ontology.local.mappings-file      | ontology/mapping_cql.json  | CQL mappings file (local)                     |
| cql.ontology.pkg.version              |                            | Ontology package version (remote)             |
| cql.ontology.pkg.credentials.user     |                            | Ontology package Basic Auth user (remote)     |
| cql.ontology.pkg.credentials.password |                            | Ontology package Basic Auth password (remote) |

Additional application properties can be set by overriding values form the [application.yaml](src/main/resources/application.yaml) with using environment variables.

## License

[AGPL-3.0](https://www.gnu.org/licenses/agpl-3.0.en.html)
