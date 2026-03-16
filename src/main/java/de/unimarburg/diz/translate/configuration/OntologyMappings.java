package de.unimarburg.diz.translate.configuration;

import de.numcodex.sq2cql.model.Mapping;
import de.numcodex.sq2cql.model.TermCodeNode;

public record OntologyMappings(TermCodeNode conceptTree, Mapping[] mappings) {}
