package uk.gov.moj.cpp.hearing.it;

import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;

import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.Offence;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.justice.core.courts.ProsecutionCaseIdentifier;
import uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher;

import java.time.ZoneId;
import java.util.List;

import org.hamcrest.Matcher;

public class MatcherUtil {

    public static BeanMatcher<ProsecutionCase> getProsecutionCaseMatcher(final ProsecutionCase prosecutionCase) {
        BeanMatcher<Defendant>[] items = prosecutionCase.getDefendants().stream()
                .map(MatcherUtil::getDefendantMatcher)
                .map(defendantBeanMatcher -> defendantBeanMatcher.with(Defendant::getProsecutionCaseId, is(prosecutionCase.getId())))
                .toArray(BeanMatcher[]::new);
        return isBean(ProsecutionCase.class)
                .with(ProsecutionCase::getId, is(prosecutionCase.getId()))
                .with(ProsecutionCase::getInitiationCode, is(prosecutionCase.getInitiationCode()))
                .with(ProsecutionCase::getStatementOfFacts, is(prosecutionCase.getStatementOfFacts()))
                .with(ProsecutionCase::getStatementOfFactsWelsh, is(prosecutionCase.getStatementOfFactsWelsh()))
                .with(ProsecutionCase::getProsecutionCaseIdentifier, isBean(ProsecutionCaseIdentifier.class)
                        .with(ProsecutionCaseIdentifier::getProsecutionAuthorityId, is(prosecutionCase.getProsecutionCaseIdentifier().getProsecutionAuthorityId()))
                        .with(ProsecutionCaseIdentifier::getProsecutionAuthorityCode, is(prosecutionCase.getProsecutionCaseIdentifier().getProsecutionAuthorityCode()))
                        .with(ProsecutionCaseIdentifier::getCaseURN, is(prosecutionCase.getProsecutionCaseIdentifier().getCaseURN()))
                        .with(ProsecutionCaseIdentifier::getProsecutionAuthorityReference, is(prosecutionCase.getProsecutionCaseIdentifier().getProsecutionAuthorityReference())))
                .with(ProsecutionCase::getDefendants, hasItems(items));
    }

    public static BeanMatcher<Defendant> getDefendantMatcher(final Defendant defendant) {
        return isBean(Defendant.class)
                .with(Defendant::getId, is(defendant.getId()))
                .with(Defendant::getMasterDefendantId, is(defendant.getMasterDefendantId()))
                .with(Defendant::getCourtProceedingsInitiated, is(defendant.getCourtProceedingsInitiated().withZoneSameLocal(ZoneId.of("UTC"))))
                .with(Defendant::getOffences, hasItems(defendant.getOffences().stream().map(
                        offence -> isBean(Offence.class)
                                .with(Offence::getId, is(offence.getId()))
                                .with(Offence::getOffenceDefinitionId, is(offence.getOffenceDefinitionId()))
                                .with(Offence::getOffenceCode, is(offence.getOffenceCode()))
                                .with(Offence::getWording, is(offence.getWording()))
                                .with(Offence::getStartDate, is(offence.getStartDate()))
                                .with(Offence::getOrderIndex, is(offence.getOrderIndex()))
                                .with(Offence::getCount, is(offence.getCount()))
                                .with(Offence::getLaidDate, is(offence.getLaidDate()))
                                .with(Offence::getEndorsableFlag, is(offence.getEndorsableFlag()))
                ).toArray(BeanMatcher[]::new)));
    }

    public static Matcher<Iterable<ProsecutionCase>> getProsecutionCasesMatchers(final List<ProsecutionCase> prosecutionCases) {
        return hasItems(prosecutionCases.stream().map(MatcherUtil::getProsecutionCaseMatcher).toArray(BeanMatcher[]::new));
    }

}
