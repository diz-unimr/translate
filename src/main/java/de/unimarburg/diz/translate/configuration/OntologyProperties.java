package de.unimarburg.diz.translate.configuration;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@ConfigurationProperties(prefix = "cql.ontology")
public class OntologyProperties implements Serializable {

  private final Pkg pkg = new Pkg();
  private final Local local = new Local();

  @Getter
  @Setter
  public static class Pkg implements Serializable {

    private String version;
    private final Credentials credentials = new Credentials();

    @Getter
    @Setter
    public static class Credentials implements Serializable {

      private String user;
      private String password;
    }
  }

  @Getter
  @Setter
  public static class Local implements Serializable {

    private String ontologyFile;
    private String mappingsFile;
  }
}
