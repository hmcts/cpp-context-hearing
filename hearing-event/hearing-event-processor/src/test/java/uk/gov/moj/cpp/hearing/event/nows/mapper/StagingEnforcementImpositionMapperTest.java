package uk.gov.moj.cpp.hearing.event.nows.mapper;

import static java.util.Collections.EMPTY_LIST;
import static org.codehaus.groovy.runtime.InvokerHelper.asList;
import static org.junit.Assert.assertNull;
import static uk.gov.moj.cpp.hearing.event.nows.PromptTypesConstant.P_CREDITOR_NAME;
import static uk.gov.moj.cpp.hearing.event.nows.ResultDefinitionsConstant.RD_COMPENSATION;
import static uk.gov.moj.cpp.hearing.event.nows.ResultDefinitionsConstant.RD_COSTS;
import static uk.gov.moj.cpp.hearing.event.nows.ResultDefinitionsConstant.RD_SURCHARGE;

import uk.gov.justice.core.courts.Prompt;
import uk.gov.justice.core.courts.ResultPrompt;
import uk.gov.justice.core.courts.SharedResultLine;
import uk.gov.justice.json.schemas.staging.Imposition;
import uk.gov.justice.services.test.utils.core.random.RandomGenerator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;

public class StagingEnforcementImpositionMapperTest {

    private StagingEnforcementImpositionMapper target;

    @Test
    public void setMajorCreditorFromProsecutionAuthorityCodeWhenNoPromptsIsFound() {

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

        List<Imposition> actual = target.createImpositions();

        actual.forEach(imposition -> Assert.assertEquals("TFL2", imposition.getMajorCreditor()));
    }

    @Test
    public void setMajorCreditorFromProsecutionAuthorityCodeIsCPS() {

        final Map<UUID, List<Prompt>> resultLineIdWithListOfPrompts = new HashMap<>();
        UUID sharedResultLineId = UUID.randomUUID();
        resultLineIdWithListOfPrompts.put(sharedResultLineId, EMPTY_LIST);

        final Map<UUID, UUID> resultLineResultDefinitionIdMap = new HashMap<>();
        resultLineResultDefinitionIdMap.put(sharedResultLineId, RD_COMPENSATION);

        final Map<UUID, String> sharedResultLineOffenceCodeMap = new HashMap<>();
        sharedResultLineOffenceCodeMap.put(sharedResultLineId, RandomGenerator.STRING.next());

        final Set<String> prosecutionAuthorityCodes = new HashSet<>();
        prosecutionAuthorityCodes.add("CPS");

        target = new StagingEnforcementImpositionMapper(EMPTY_LIST, resultLineResultDefinitionIdMap, sharedResultLineOffenceCodeMap, resultLineIdWithListOfPrompts, prosecutionAuthorityCodes);

        List<Imposition> actual = target.createImpositions();

        actual.forEach(imposition -> assertNull(imposition.getMajorCreditor()));
    }

    @Test
    public void setMajorCreditorIsNull() {

        final Map<UUID, List<Prompt>> resultLineIdWithListOfPrompts = new HashMap<>();
        UUID sharedResultLineId = UUID.randomUUID();
        resultLineIdWithListOfPrompts.put(sharedResultLineId, EMPTY_LIST);

        final Map<UUID, UUID> resultLineResultDefinitionIdMap = new HashMap<>();
        resultLineResultDefinitionIdMap.put(sharedResultLineId, RD_SURCHARGE);

        final Map<UUID, String> sharedResultLineOffenceCodeMap = new HashMap<>();
        sharedResultLineOffenceCodeMap.put(sharedResultLineId, RandomGenerator.STRING.next());

        final Set<String> prosecutionAuthorityCodes = new HashSet<>();
        prosecutionAuthorityCodes.add("TFL");

        target = new StagingEnforcementImpositionMapper(EMPTY_LIST, resultLineResultDefinitionIdMap, sharedResultLineOffenceCodeMap, resultLineIdWithListOfPrompts, prosecutionAuthorityCodes);

        List<Imposition> actual = target.createImpositions();

        actual.forEach(imposition -> assertNull(imposition.getMajorCreditor()));
    }

    @Test
    public void setMajorCreditorWithPromptValue() {

        final Map<UUID, List<Prompt>> resultLineIdWithListOfPrompts = new HashMap<>();
        UUID sharedResultLineId = UUID.randomUUID();

        UUID promptId = UUID.randomUUID();
        resultLineIdWithListOfPrompts.put(sharedResultLineId, asList(Prompt.prompt().withId(promptId).build()));

        final Map<UUID, UUID> resultLineResultDefinitionIdMap = new HashMap<>();
        resultLineResultDefinitionIdMap.put(sharedResultLineId, RD_COSTS);

        final Map<UUID, String> sharedResultLineOffenceCodeMap = new HashMap<>();
        sharedResultLineOffenceCodeMap.put(sharedResultLineId, RandomGenerator.STRING.next());

        final Set<String> prosecutionAuthorityCodes = new HashSet<>();
        prosecutionAuthorityCodes.add("CPS");

        final List<SharedResultLine> sharedResultLines = asList(SharedResultLine.sharedResultLine().withPrompts(asList(ResultPrompt.resultPrompt().withId(promptId).withPromptReference(P_CREDITOR_NAME).withValue("Transport for London").build())).build());

        target = new StagingEnforcementImpositionMapper(sharedResultLines, resultLineResultDefinitionIdMap, sharedResultLineOffenceCodeMap, resultLineIdWithListOfPrompts, prosecutionAuthorityCodes);

        List<Imposition> actual = target.createImpositions();

        actual.forEach(imposition -> Assert.assertEquals("TFL2", imposition.getMajorCreditor()));
    }
}