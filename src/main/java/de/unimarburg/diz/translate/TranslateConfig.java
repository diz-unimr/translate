package de.unimarburg.diz.translate;

import static java.util.Map.entry;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import de.numcodex.sq2cql.Translator;
import de.numcodex.sq2cql.model.Mapping;
import de.numcodex.sq2cql.model.MappingContext;
import de.numcodex.sq2cql.model.TermCodeNode;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration
public class TranslateConfig {

  @Value("${cql.mappings-file}")
  private String mappingsFile;

  @Value("${cql.ontology-file}")
  private String ontologyFile;

  @Qualifier("translation")
  @Bean
  ObjectMapper mapper() {
    return new ObjectMapper().disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
  }

  @Lazy
  @Bean
  Translator createCqlTranslator(@Qualifier("translation") ObjectMapper mapper) throws IOException {
    var mappings = mapper.readValue(new File(mappingsFile), Mapping[].class);
    var conceptTree = mapper.readValue(new File(ontologyFile), TermCodeNode.class);

    return Translator.of(
        MappingContext.of(
            Stream.of(mappings)
                .collect(Collectors.toMap(Mapping::key, Function.identity(), (a, b) -> a)),
            conceptTree,
            Map.ofEntries(
                entry("http://fhir.de/CodeSystem/bfarm/icd-10-gm", "icd10"),
                entry("mii.abide", "abide"),
                entry("http://fhir.de/CodeSystem/bfarm/ops", "ops"),
                entry("http://dicom.nema.org/resources/ontology/DCM", "dcm"),
                entry(
                    "https://www.medizininformatik-initiative.de/fhir/core/modul-person/CodeSystem/Vitalstatus",
                    "vitalstatus"),
                entry("http://loinc.org", "loinc"),
                entry("https://fhir.bbmri.de/CodeSystem/SampleMaterialType", "sample"),
                entry("http://fhir.de/CodeSystem/bfarm/atc", "atc"),
                entry("http://snomed.info/sct", "snomed"),
                entry("http://terminology.hl7.org/CodeSystem/condition-ver-status", "cvs"),
                entry("http://hl7.org/fhir/administrative-gender", "gender"),
                entry("urn:oid:1.2.276.0.76.5.409", "urn409"),
                entry(
                    "https://www.netzwerk-universitaetsmedizin.de/fhir/CodeSystem/ecrf-parameter-codes",
                    "numecrf"),
                entry("urn:iso:std:iso:3166", "iso3166"),
                entry(
                    "https://www.netzwerk-universitaetsmedizin.de/fhir/CodeSystem/frailty-score",
                    "frailtyscore"),
                entry(
                    "http://terminology.hl7.org/CodeSystem/consentcategorycodes",
                    "consentcategory"),
                entry("urn:oid:2.16.840.1.113883.3.1937.777.24.5.3", "consent"),
                entry("http://hl7.org/fhir/sid/icd-o-3", "icdo3"),
                entry("fdpg.mii.cds", "fdpgmiicds"),
                entry("http://fhir.de/CodeSystem/bfarm/alpha-id", "alphaid"),
                entry("urn:iso:std:iso:11073:10101", "ISO11073"),
                entry("http://terminology.hl7.org/CodeSystem/icd-o-3", "icdo3"),
                entry(
                    "http://fhir.de/CodeSystem/dkgev/Fachabteilungsschluessel",
                    "fachabteilungsschluessel"),
                entry("http://terminology.hl7.org/CodeSystem/v3-ActCode", "v3actcode"),
                entry(
                    "http://fhir.de/CodeSystem/dkgev/Fachabteilungsschluessel-erweitert",
                    "fachabteilungsschluesselerweitert"),
                entry("http://fhir.de/CodeSystem/kontaktart-de", "kontaktart"),
                entry("http://hl7.org/fhir/sid/icd-10", "sidicd10"),
                entry("http://fhir.de/CodeSystem/Kontaktebene", "kontaktebene"),
                entry("http://www.orpha.net", "orphanet"),
                entry("fdpg.consent.combined", "fdpgcombinedconsent"),
                entry("http://hl7.org/fhir/consent-provision-type", "provisiontype"))));
  }
}
