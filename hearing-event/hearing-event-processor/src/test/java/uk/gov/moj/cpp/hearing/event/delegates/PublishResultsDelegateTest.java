package uk.gov.moj.cpp.hearing.event.delegates;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloper;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMatcher.jsonEnvelope;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMetadataMatcher.metadata;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopePayloadMatcher.payloadIsJson;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;
import static uk.gov.moj.cpp.hearing.event.NowsTemplates.resultsSharedTemplate;

import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.ObjectConverters.asPojo;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.NowDefinitionTemplates.standardNowDefinition;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.VariantDirectoryTemplates.standardVariantTemplate;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.print;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.with;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;
import static uk.gov.moj.cpp.hearing.test.matchers.ElementAtListMatcher.first;
import static uk.gov.moj.cpp.hearing.test.matchers.ElementAtListMatcher.fourth;
import static uk.gov.moj.cpp.hearing.test.matchers.ElementAtListMatcher.second;
import static uk.gov.moj.cpp.hearing.test.matchers.ElementAtListMatcher.third;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.command.initiate.DefendantCase;
import uk.gov.moj.cpp.hearing.command.nowsdomain.variants.VariantKey;
import uk.gov.moj.cpp.hearing.command.result.CompletedResultLine;
import uk.gov.moj.cpp.hearing.command.result.CourtClerk;
import uk.gov.moj.cpp.hearing.command.result.ResultPrompt;
import uk.gov.moj.cpp.hearing.domain.event.DefenceCounselUpsert;
import uk.gov.moj.cpp.hearing.domain.event.ProsecutionCounselUpsert;
import uk.gov.moj.cpp.hearing.domain.event.VerdictUpsert;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsShared;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.NowDefinition;
import uk.gov.moj.cpp.hearing.event.service.ReferenceDataService;
import uk.gov.moj.cpp.hearing.message.shareResults.Address;
import uk.gov.moj.cpp.hearing.message.shareResults.Attendee;
import uk.gov.moj.cpp.hearing.message.shareResults.Case;
import uk.gov.moj.cpp.hearing.message.shareResults.CourtCentre;
import uk.gov.moj.cpp.hearing.message.shareResults.Defendant;
import uk.gov.moj.cpp.hearing.message.shareResults.Hearing;
import uk.gov.moj.cpp.hearing.message.shareResults.Interpreter;
import uk.gov.moj.cpp.hearing.message.shareResults.Offence;
import uk.gov.moj.cpp.hearing.message.shareResults.Person;
import uk.gov.moj.cpp.hearing.message.shareResults.Plea;
import uk.gov.moj.cpp.hearing.message.shareResults.Prompt;
import uk.gov.moj.cpp.hearing.message.shareResults.ShareResultsMessage;
import uk.gov.moj.cpp.hearing.message.shareResults.SharedResultLine;
import uk.gov.moj.cpp.hearing.message.shareResults.Variant;
import uk.gov.moj.cpp.hearing.message.shareResults.Verdict;
import uk.gov.moj.cpp.hearing.test.CommandHelpers;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

public class PublishResultsDelegateTest {

    @Mock
    private ReferenceDataService referenceDataService;

    @Spy
    private final Enveloper enveloper = createEnveloper();

    @Spy
    private final ObjectMapper objectMapper = new ObjectMapperProducer().objectMapper();

    @Spy
    @InjectMocks
    private final ObjectToJsonObjectConverter objectToJsonObjectConverter = new ObjectToJsonObjectConverter();

    @Mock
    private Sender sender;

    @Captor
    private ArgumentCaptor<JsonEnvelope> envelopeArgumentCaptor;

    @InjectMocks
    private PublishResultsDelegate publishResultsDelegate;

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @Ignore("GPE-5480 - share results story will address the change in model")
    @Test
    public void shareResults() {

        final NowDefinition nowDefinition = standardNowDefinition();

        final CommandHelpers.ResultsSharedEventHelper resultsShared = h(with(resultsSharedTemplate(), r -> {
            r.getVariantDirectory().get(0).getKey().setNowsTypeId(nowDefinition.getId());
        }));

        final List<uk.gov.moj.cpp.hearing.command.nowsdomain.variants.Variant> newVariants = singletonList(
                standardVariantTemplate(nowDefinition.getId(), resultsShared.getHearingId(), resultsShared.getFirstDefendant().getId()).setReferenceDate(LocalDate.now())
        );

        when(referenceDataService.getNowDefinitionById(
                null, newVariants.get(0).getReferenceDate(),
                nowDefinition.getId())).thenReturn(nowDefinition);

        publishResultsDelegate.shareResults(null, sender,
                envelopeFrom(metadataWithRandomUUID("hearing.results-shared"), objectToJsonObjectConverter.convert(resultsShared)),
                resultsShared.it(), newVariants);

        verify(sender).send(envelopeArgumentCaptor.capture());

        final JsonEnvelope sharedResultsMessage = envelopeArgumentCaptor.getValue();

        assertThat(sharedResultsMessage, jsonEnvelope(metadata().withName("public.hearing.resulted"), payloadIsJson(print())));

        assertThat(asPojo(sharedResultsMessage, ShareResultsMessage.class), isBean(ShareResultsMessage.class)
                .with(ShareResultsMessage::getSharedTime, is(resultsShared.it().getSharedTime()))
                .with(ShareResultsMessage::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(resultsShared.getHearing().getId()))
                        .with(Hearing::getHearingType, is(resultsShared.getHearing().getType()))
                        .with(Hearing::getStartDateTime, is(resultsShared.getHearing().getHearingDays().get(0)))
                        .with(Hearing::getHearingDates, containsInAnyOrder(resultsShared.getHearing().getHearingDays().toArray()))
/*                        .with(Hearing::getCourtCentre, isBean(CourtCentre.class)
                                .with(CourtCentre::getCourtCentreId, is(resultsShared.getHearing().getCourtCentreId()))
                                .with(CourtCentre::getCourtCentreName, is(resultsShared.getHearing().getCourtCentreName()))
                                .with(CourtCentre::getCourtRoomId, is(resultsShared.getHearing().getCourtRoomId()))
                                .with(CourtCentre::getCourtRoomName, is(resultsShared.getHearing().getCourtRoomName()))
                        )*/
                        .with(Hearing::getAttendees, first(isBean(Attendee.class)
                                .with(Attendee::getPersonId, is(resultsShared.getCourtClerk().getId()))
                                .with(Attendee::getFirstName, is(resultsShared.getCourtClerk().getFirstName()))
                                .with(Attendee::getLastName, is(resultsShared.getCourtClerk().getLastName()))
                                .with(Attendee::getType, is("COURTCLERK"))
                        ))
/*                        .with(Hearing::getAttendees, second(isBean(Attendee.class)
                                .with(Attendee::getPersonId, is(resultsShared.getHearing().getJudge().getId()))
                                .with(Attendee::getTitle, is(resultsShared.getHearing().getJudge().getTitle()))
                                .with(Attendee::getFirstName, is(resultsShared.getHearing().getJudge().getFirstName()))
                                .with(Attendee::getLastName, is(resultsShared.getHearing().getJudge().getLastName()))
                                .with(Attendee::getType, is("JUDGE"))
                        ))*/
                        .with(Hearing::getAttendees, third(isBean(Attendee.class)
                                .with(Attendee::getPersonId, is(resultsShared.getFirstDefenseCounsel().getPersonId()))
                                .with(Attendee::getFirstName, is(resultsShared.getFirstDefenseCounsel().getFirstName()))
                                .with(Attendee::getLastName, is(resultsShared.getFirstDefenseCounsel().getLastName()))
                                .with(Attendee::getStatus, is(resultsShared.getFirstDefenseCounsel().getStatus()))
                                .with(Attendee::getTitle, is(resultsShared.getFirstDefenseCounsel().getTitle()))
                                .with(Attendee::getType, is("DEFENCEADVOCATE"))
                                .with(Attendee::getDefendantIds, containsInAnyOrder(resultsShared.getFirstDefenseCounsel().getDefendantIds().toArray()))
                        ))
                        .with(Hearing::getAttendees, fourth(isBean((Attendee.class))
                                .with(Attendee::getPersonId, is(resultsShared.getFirstProsecutionCounsel().getPersonId()))
                                .with(Attendee::getFirstName, is(resultsShared.getFirstProsecutionCounsel().getFirstName()))
                                .with(Attendee::getLastName, is(resultsShared.getFirstProsecutionCounsel().getLastName()))
                                .with(Attendee::getStatus, is(resultsShared.getFirstProsecutionCounsel().getStatus()))
                                .with(Attendee::getTitle, is(resultsShared.getFirstProsecutionCounsel().getTitle()))
                                .with(Attendee::getType, is("PROSECUTIONADVOCATE"))
                                .with(Attendee::getCaseIds, containsInAnyOrder(resultsShared.getCaseIds().toArray()))
                        ))
/*                        .with(Hearing::getDefendants, first(isBean(Defendant.class)
                                .with(Defendant::getId, is(resultsShared.getFirstDefendant().getId()))
                                .with(Defendant::getDefenceOrganisation, is(resultsShared.getFirstDefendant().getDefenceOrganisation()))
                                .with(Defendant::getInterpreter, isBean(Interpreter.class)
                                        .with(Interpreter::getLanguage, is(resultsShared.getFirstDefendant().getInterpreter().getLanguage()))
                                )
                                .with(Defendant::getPerson, isBean(Person.class)
                                        .with(Person::getId, is(resultsShared.getFirstDefendant().getPersonId()))
                                        .with(Person::getDateOfBirth, is(resultsShared.getFirstDefendant().getDateOfBirth()))
                                        .with(Person::getNationality, is(resultsShared.getFirstDefendant().getNationality()))
                                        .with(Person::getGender, is(resultsShared.getFirstDefendant().getGender()))
                                        .with(Person::getFirstName, is(resultsShared.getFirstDefendant().getFirstName()))
                                        .with(Person::getLastName, is(resultsShared.getFirstDefendant().getLastName()))
                                        .with(Person::getAddress, isBean(Address.class)
                                                .with(Address::getAddress1, is(resultsShared.getFirstDefendant().getAddress().getAddress1()))
                                                .with(Address::getAddress2, is(resultsShared.getFirstDefendant().getAddress().getAddress2()))
                                                .with(Address::getAddress3, is(resultsShared.getFirstDefendant().getAddress().getAddress3()))
                                                .with(Address::getAddress4, is(resultsShared.getFirstDefendant().getAddress().getAddress4()))
                                                .with(Address::getPostCode, is(resultsShared.getFirstDefendant().getAddress().getPostCode()))
                                        )
                                )
                                .with(Defendant::getCases, first(isBean(Case.class)
                                        .with(Case::getId, is(resultsShared.getFirstDefendantCase().getCaseId()))
                                        .with(Case::getUrn, is(resultsShared.getFirstCase().getUrn()))
                                        .with(Case::getBailStatus, is(resultsShared.getFirstDefendantCase().getBailStatus()))
                                        .with(Case::getCustodyTimeLimitDate, is(resultsShared.getFirstDefendantCase().getCustodyTimeLimitDate()))
                                        .with(Case::getOffences, first(isBean(Offence.class)
                                                .with(Offence::getId, is(resultsShared.getFirstDefendantFirstOffence().getId()))
                                                .with(Offence::getCode, is(resultsShared.getFirstDefendantFirstOffence().getOffenceCode()))
                                                .with(Offence::getStartDate, is(resultsShared.getFirstDefendantFirstOffence().getStartDate()))
                                                .with(Offence::getEndDate, is(resultsShared.getFirstDefendantFirstOffence().getEndDate()))
                                                .with(Offence::getWording, is(resultsShared.getFirstDefendantFirstOffence().getWording()))
                                                .with(Offence::getPlea, isBean(Plea.class)
                                                        .with(Plea::getId, is(resultsShared.getFirstPlea().getOffenceId()))
                                                        .with(Plea::getDate, is(resultsShared.getFirstPlea().getPleaDate()))
                                                        .with(Plea::getEnteredHearingId, is(resultsShared.getFirstPlea().getOriginHearingId()))
                                                        .with(Plea::getValue, is(resultsShared.getFirstPlea().getValue()))
                                                )
                                                .with(Offence::getVerdict, isBean(Verdict.class)
                                                        //.with(Verdict::getTypeId, is(resultsShared.getFirstVerdict().getVerdictTypeId())) //GPE-4651: Out of scope
                                                        .with(Verdict::getEnteredHearingId, is(resultsShared.getFirstVerdict().getHearingId()))
                                                        .with(Verdict::getVerdictDate, is(resultsShared.getFirstVerdict().getVerdictDate()))
                                                        .with(Verdict::getNumberOfJurors, is(resultsShared.getFirstVerdict().getNumberOfJurors()))
                                                        .with(Verdict::getNumberOfSplitJurors, is(formatNumberOfSplitJurors(resultsShared.getFirstVerdict())))
                                                        .with(Verdict::getVerdictCategory, is(resultsShared.getFirstVerdict().getCategory()))
                                                        .with(Verdict::getUnanimous, is(resultsShared.getFirstVerdict().getUnanimous()))
                                                        .with(Verdict::getVerdictDescription, is(resultsShared.getFirstVerdict().getLegislation()))
                                                )
                                        ))
                                ))
                        ))*/
                        .with(Hearing::getSharedResultLines, first(isBean(SharedResultLine.class)
                                .with(SharedResultLine::getId, is(resultsShared.getFirstCompletedResultLine().getId()))
                                .with(SharedResultLine::getCaseId, is(resultsShared.getFirstCompletedResultLine().getCaseId()))
                                .with(SharedResultLine::getDefendantId, is(resultsShared.getFirstCompletedResultLine().getDefendantId()))
                                .with(SharedResultLine::getOffenceId, is(resultsShared.getFirstCompletedResultLine().getOffenceId()))
                                .with(SharedResultLine::getLabel, is(resultsShared.getFirstCompletedResultLine().getResultLabel()))
                                .with(SharedResultLine::getLevel, is(resultsShared.getFirstCompletedResultLine().getLevel().toString()))
                                .with(SharedResultLine::getCourtClerk, isBean(CourtClerk.class)
                                        .with(CourtClerk::getId, is(resultsShared.getFirstCompletedResultLineStatus().getCourtClerk().getId()))
                                        .with(CourtClerk::getFirstName, is(resultsShared.getFirstCompletedResultLineStatus().getCourtClerk().getFirstName()))
                                        .with(CourtClerk::getLastName, is(resultsShared.getFirstCompletedResultLineStatus().getCourtClerk().getLastName()))
                                )
                                .with(SharedResultLine::getPrompts, first(isBean(Prompt.class)
                                        .with(Prompt::getId, is(resultsShared.getFirstCompletedResultLineFirstPrompt().getId()))
                                        .with(Prompt::getLabel, is(resultsShared.getFirstCompletedResultLineFirstPrompt().getLabel()))
                                        .with(Prompt::getValue, is(resultsShared.getFirstCompletedResultLineFirstPrompt().getValue()))

                                ))
                                .with(SharedResultLine::getLastSharedDateTime, is(resultsShared.getFirstCompletedResultLineStatus().getLastSharedDateTime()))
                        ))
                )
                .with(ShareResultsMessage::getVariants, first(isBean(Variant.class)
                        .with(Variant::getKey, isBean(VariantKey.class)
                                .with(VariantKey::getDefendantId, is(resultsShared.getFirstVariant().getKey().getDefendantId()))
                                .with(VariantKey::getNowsTypeId, is(resultsShared.getFirstVariant().getKey().getNowsTypeId()))
                                .with(VariantKey::getUsergroups, containsInAnyOrder(resultsShared.getFirstVariant().getKey().getUsergroups().toArray()))
                                .with(VariantKey::getHearingId, is(resultsShared.getFirstVariant().getKey().getHearingId()))
                        )
                        .with(Variant::getMaterialId, is(resultsShared.getFirstVariant().getValue().getMaterialId()))
                        .with(Variant::getDescription, is(nowDefinition.getName()))
                        .with(Variant::getTemplateName, is(nowDefinition.getTemplateName()))
                ))
                .with(ShareResultsMessage::getVariants, second(isBean(Variant.class)
                        .with(Variant::getKey, isBean(VariantKey.class)
                                .with(VariantKey::getDefendantId, is(newVariants.get(0).getKey().getDefendantId()))
                                .with(VariantKey::getNowsTypeId, is(newVariants.get(0).getKey().getNowsTypeId()))
                                .with(VariantKey::getUsergroups, containsInAnyOrder(newVariants.get(0).getKey().getUsergroups().toArray()))
                                .with(VariantKey::getHearingId, is(newVariants.get(0).getKey().getHearingId()))
                        )
                        .with(Variant::getMaterialId, is(newVariants.get(0).getValue().getMaterialId()))
                        .with(Variant::getDescription, is(nowDefinition.getName()))
                        .with(Variant::getTemplateName, is(nowDefinition.getTemplateName()))
                ))
        );
    }

    private static String formatNumberOfSplitJurors(final VerdictUpsert v) {
        return v.getNumberOfJurors() != null && v.getNumberOfSplitJurors() != null ?
                String.format("%s-%s", v.getNumberOfJurors() - v.getNumberOfSplitJurors(), v.getNumberOfSplitJurors())
                : null;
    }

}
