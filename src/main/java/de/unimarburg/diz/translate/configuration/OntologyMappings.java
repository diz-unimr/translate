package de.unimarburg.diz.translate.configuration;


import de.medizininformatikinitiative.cctb.model.Mapping;
import de.medizininformatikinitiative.cctb.model.MappingTreeBase;

public record OntologyMappings(MappingTreeBase conceptTree, Mapping[] mappings) {}
