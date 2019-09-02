package uk.gov.moj.cpp.hearing.mapping;

import uk.gov.moj.cpp.hearing.persist.entity.application.ApplicationDraftResult;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;

import javax.enterprise.context.ApplicationScoped;
import java.util.UUID;

@SuppressWarnings("squid:S1186")
@ApplicationScoped
public class ApplicationDraftResultJPAMapper {

    //to satisfy CDI test runner
    public ApplicationDraftResultJPAMapper() {
    }

    public ApplicationDraftResult toJPA(final Hearing hearing, final UUID targetId,
                                        final UUID applicationId,
                                        final String draftResult) {
        final ApplicationDraftResult applicationDraftResult = new ApplicationDraftResult();
        applicationDraftResult.setId(targetId);
        applicationDraftResult.setHearing(hearing);
        applicationDraftResult.setApplicationId(applicationId);
        applicationDraftResult.setDraftResult(draftResult);
        return applicationDraftResult;
    }


}