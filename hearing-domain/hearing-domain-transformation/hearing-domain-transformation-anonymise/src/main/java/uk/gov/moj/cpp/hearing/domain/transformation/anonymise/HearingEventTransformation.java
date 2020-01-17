package uk.gov.moj.cpp.hearing.domain.transformation.anonymise;

import uk.gov.justice.tools.eventsourcing.anonymization.EventAnonymiserTransformation;
import uk.gov.justice.tools.eventsourcing.transformation.api.annotation.Transformation;

@Transformation
public class HearingEventTransformation extends EventAnonymiserTransformation {
}
