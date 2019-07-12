package uk.gov.moj.cpp.hearing.event.nows.mapper;

import static java.util.Collections.EMPTY_LIST;
import static org.codehaus.groovy.runtime.InvokerHelper.asList;
import static org.junit.Assert.*;
import static uk.gov.moj.cpp.hearing.event.nows.ResultDefinitionsConstant.RD_COMPENSATION;

import uk.gov.justice.core.courts.Prompt;
import uk.gov.justice.json.schemas.staging.Imposition;
import uk.gov.justice.services.test.utils.core.random.RandomGenerator;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class StagingEnforcementImpositionMapperTest {

    private StagingEnforcementImpositionMapper target;

    @Before
    public void setUp() {
        final Map<UUID, List<Prompt>> resultLineIdWithListOfPrompts = new HashMap<>();
        UUID sharedResultLineId = UUID.randomUUID();
        resultLineIdWithListOfPrompts.put(sharedResultLineId, EMPTY_LIST);

        final Map<UUID, UUID> resultLineResultDefinitionIdMap = new HashMap<>();
        resultLineResultDefinitionIdMap.put(sharedResultLineId, RD_COMPENSATION);

        final Map<UUID, String> sharedResultLineOffenceCodeMap = new HashMap<>();
        sharedResultLineOffenceCodeMap.put(sharedResultLineId, RandomGenerator.STRING.next());

        final Set<String> prosecutionAuthorityCodes = new HashSet<>();
        prosecutionAuthorityCodes.add("TFL");

        target = new StagingEnforcementImpositionMapper(EMPTY_LIST, resultLineResultDefinitionIdMap, sharedResultLineOffenceCodeMap, resultLineIdWithListOfPrompts, prosecutionAuthorityCodes);
    }

    @Test
    public void setMajorCreditorFromProsecutionAuthorityCodeWhenNoPromptsIsFound() {

        List<Imposition> actual = target.createImpositions();

        actual.forEach(imposition -> Assert.assertEquals("TFL2", imposition.getMajorCreditor()));
    }
}