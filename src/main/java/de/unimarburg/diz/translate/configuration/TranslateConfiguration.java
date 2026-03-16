package de.unimarburg.diz.translate.configuration;

import static java.util.Map.entry;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import de.numcodex.sq2cql.Translator;
import de.numcodex.sq2cql.model.Mapping;
import de.numcodex.sq2cql.model.MappingContext;
import de.numcodex.sq2cql.model.TermCodeNode;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipFile;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatusCode;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

@Configuration
@EnableConfigurationProperties
@Slf4j
public class TranslateConfiguration {

  @Bean
  public OntologyProperties mappingProperties() {
    return new OntologyProperties();
  }

  @Qualifier("translation")
  @Bean
  ObjectMapper mapper() {
    return new ObjectMapper().disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
  }

  @Lazy
  @Bean
  OntologyMappings getOntology(
      OntologyProperties props, @Qualifier("translation") ObjectMapper mapper)
      throws IOException, URISyntaxException {
    var version = props.getPkg().getVersion();

    if (version.isBlank()) {
      // local
      return new OntologyMappings(
          mapper.readValue(new File(props.getLocal().getOntologyFile()), TermCodeNode.class),
          mapper.readValue(new File(props.getLocal().getMappingsFile()), Mapping[].class));
    } else {
      // remote
      var creds = props.getPkg().getCredentials();
      var pkg = getPackage(version, creds.getUser(), creds.getPassword());
      return buildFromPackage(pkg, mapper);
    }
  }

  OntologyMappings buildFromPackage(Resource pkg, ObjectMapper mapper) throws IOException {

    try (var zipFile = new ZipFile(pkg.getFile())) {

      // ontology file
      var ontologyResource = zipFile.getEntry("ontology/mapping/mapping_tree.json");
      if (ontologyResource == null) {
        log.error("Error locating mapping_tree.json in zip file: {}", zipFile.getName());
        throw new IllegalArgumentException();
      }
      // mapping file
      var mappingsResource = zipFile.getEntry("ontology/mapping/mapping_cql.json");
      if (mappingsResource == null) {
        log.error("Error locating mapping_tree.json in zip file: {}", zipFile.getName());
        throw new IllegalArgumentException();
      }

      try (var conceptTreeStream = zipFile.getInputStream(ontologyResource);
          var mappingsStream = zipFile.getInputStream(mappingsResource)) {
        return new OntologyMappings(
            mapper.readValue(conceptTreeStream, TermCodeNode.class),
            mapper.readValue(mappingsStream, Mapping[].class));
      }
    }
  }

  Resource getPackage(String version, String user, String password)
      throws RestClientResponseException, IOException, URISyntaxException {

    // load from remote location
    var packageUri =
        new URI(
            String.format(
                "https://gitlab.diz.uni-marburg.de/api/v4/projects/308/packages/generic/ontology/%s/ontology.zip",
                version));
    log.info("Using ontology mappings from remote package: {}", packageUri);

    var provider = new BasicCredentialsProvider();
    var targetHost = HttpHost.create(packageUri);
    var authScope = new AuthScope(targetHost);
    provider.setCredentials(
        authScope, new UsernamePasswordCredentials(user, password.toCharArray()));

    var tmpFile = File.createTempFile("download", ".zip");
    try (var client = HttpClientBuilder.create().setDefaultCredentialsProvider(provider).build()) {
      client.execute(
          new HttpGet(packageUri),
          response -> {
            var status = response.getCode();
            log.info("Package registry responded with status {}", status);
            if (status == HttpStatus.SC_OK) {
              var entity = response.getEntity();
              if (entity != null) {
                StreamUtils.copy(response.getEntity().getContent(), new FileOutputStream(tmpFile));
              }

              EntityUtils.consume(entity);

            } else {
              throw new ResponseStatusException(HttpStatusCode.valueOf(status));
            }
            return response;
          });
    }

    return new FileSystemResource(tmpFile);
  }

  @Lazy
  @Bean
  Translator createCqlTranslator(OntologyMappings ontology) {

    return Translator.of(
        MappingContext.of(
            Stream.of(ontology.mappings())
                .collect(Collectors.toMap(Mapping::key, Function.identity(), (a, b) -> a)),
            ontology.conceptTree(),
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
                entry("http://hl7.org/fhir/consent-provision-type", "provisiontype"),
                entry("https://fhir.diz.uni-marburg.de/CodeSystem/swisslab-code", "swisslab"))));
  }
}
