package uk.gov.moj.cpp.hearing.domain.aggregate;

import static java.util.stream.Stream.of;
import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.match;
import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.otherwiseDoNothing;

import uk.gov.justice.domain.aggregate.Aggregate;
import uk.gov.moj.cpp.hearing.domain.event.HearingCaseNoteSaved;

import java.util.stream.Stream;

public class HearingCaseNote implements Aggregate {
    @Override
    public Object apply(final Object event) {
        return match(event).with(otherwiseDoNothing());
    }

    public Stream<Object> saveCaseNote(final uk.gov.justice.json.schemas.core.HearingCaseNote caseNote) {
        return of(new HearingCaseNoteSaved(caseNote));
    }
}
