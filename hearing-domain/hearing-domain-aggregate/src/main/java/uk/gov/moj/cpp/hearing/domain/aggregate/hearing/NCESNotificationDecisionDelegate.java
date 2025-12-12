package uk.gov.moj.cpp.hearing.domain.aggregate.hearing;

import static uk.gov.moj.cpp.hearing.domain.aggregate.hearing.NCESDecisionConstants.getAllScenariosWithGranted;
import static uk.gov.moj.cpp.hearing.domain.aggregate.hearing.NCESDecisionConstants.getAllScenariosWithRefusedOrWithdrawnResult;

import uk.gov.moj.cpp.hearing.nces.ApplicationDetailsForDefendant;
import uk.gov.moj.cpp.hearing.nces.DefendantUpdateWithApplicationDetails;
import uk.gov.moj.cpp.hearing.nces.DefendantUpdateWithFinancialOrderDetails;
import uk.gov.moj.cpp.hearing.nces.DocumentContent;
import uk.gov.moj.cpp.hearing.nces.FinancialOrderForDefendant;
import uk.gov.moj.cpp.hearing.nces.NCESNotificationRequested;
import uk.gov.moj.cpp.hearing.nces.RemoveGrantedApplicationDetailsForDefendant;

import java.io.Serializable;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NCESNotificationDecisionDelegate implements Serializable {
    private static final Logger LOGGER = LoggerFactory.getLogger(NCESNotificationDecisionDelegate.class);
    private final DefendantAggregateMomento momento;


    public NCESNotificationDecisionDelegate(final DefendantAggregateMomento momento) {
        this.momento = momento;
    }


    public Stream<Object> updateDefendantWithFinancialOrder(final FinancialOrderForDefendant financialOrderForDefendant) {

        if (momento.getFinancialOrderForDefendant() == null) {
            return handleFirstShare(financialOrderForDefendant);
        } else {
            return handleReshare(financialOrderForDefendant);
        }
    }

    public Stream<Object> updateDefendantWithApplicationDetails(final ApplicationDetailsForDefendant applicationDetailsForDefendant) {
        LOGGER.info("NCESNotification-requested : updateDefendantWithApplicationDetails ....");

        final Optional<String> sharedAndApplicationRefusedWithDrawn = checkSharedAndApplicationRefused(applicationDetailsForDefendant);
        // Application refused and was shared before
        if (sharedAndApplicationRefusedWithDrawn.isPresent()) {
            LOGGER.info("NCESNotification-requested : Application refused and was shared before (NCESNotificationRequested) ");
            return createNotificationWithAmendmentType(applicationDetailsForDefendant, sharedAndApplicationRefusedWithDrawn.get());
        }

        LOGGER.info("NCESNotification-requested : Application Shared without requiring notification");
        return Stream.of(
                DefendantUpdateWithApplicationDetails.Builder.newBuilder()
                        .withApplicationDetailsForDefendant(applicationDetailsForDefendant).build()
        );
    }

    private Stream<Object> handleFirstShare(final FinancialOrderForDefendant financialOrderForDefendant) {
        LOGGER.info("NCESNotification-requested : Results shared for first time ...");
        if (hasDeemedServedResults(financialOrderForDefendant)) {
            LOGGER.info("NCESNotification-requested :Results shared and hasDeemedServedResults (NCESNotificationRequested) ");
            final FinancialOrderForDefendant newFinancialOrderForDefendant = FinancialOrderForDefendant.newBuilderFrom(financialOrderForDefendant)
                    .withDocumentContent(
                            DocumentContent.newBuilderFrom(financialOrderForDefendant.getDocumentContent())
                                    .withAmendmentType("Write off one day deemed served")
                                    .build())
                    .build();

            return ncesNotificationWithFinancialOrder(newFinancialOrderForDefendant);
        } else {
            LOGGER.info("NCESNotification-requested :Results shared, and no DeemedServedResults.");
            return Stream.of(
                    DefendantUpdateWithFinancialOrderDetails.newBuilder()
                            .withFinancialOrderForDefendant(financialOrderForDefendant).build()
            );
        }
    }

    private Stream<Object> handleReshare(final FinancialOrderForDefendant resharedFinancialOrderForDefendant) {
        LOGGER.info("NCESNotification-requested : Results re-shared ....");

        // Re-shared currently  and Application Granted after shared
        final Stream<Object> result = handleReshareIfApplicationExists(resharedFinancialOrderForDefendant);
        if (result != null) {
            return result;
        }

        // Re-shared currently and DeemedServedResults when shared
        if (hasDeemedServedResults(momento.getFinancialOrderForDefendant())) {
            LOGGER.info("NCESNotification-requested : re-shared currently , was shared with DeemedServedResults.");
            return Stream.of(
                    DefendantUpdateWithFinancialOrderDetails.newBuilder()
                            .withFinancialOrderForDefendant(
                                    FinancialOrderForDefendant.newBuilderFrom(resharedFinancialOrderForDefendant)
                                            .withDocumentContent(
                                                    newDocumentWithOld(resharedFinancialOrderForDefendant).build()
                                            ).build()
                            ).build()
            );
        } else {
            // Re-shared currently and No DeemedServedResults when shared
            LOGGER.info("NCESNotification-requested : re-shared currently ,was shared without DeemedServedResults (NCESNotificationRequested)");
            final FinancialOrderForDefendant newFinancialOrderForDefendant = FinancialOrderForDefendant.newBuilderFrom(resharedFinancialOrderForDefendant)
                    .withDocumentContent(
                            newDocumentWithOld(resharedFinancialOrderForDefendant).withAmendmentType("Amend result").build()
                    ).build();

            return ncesNotificationWithFinancialOrder(newFinancialOrderForDefendant);
        }
    }

    private Stream<Object> handleReshareIfApplicationExists(final FinancialOrderForDefendant resharedFinancialOrderForDefendant) {
        if (this.momento.getApplicationDetailsForDefendant() != null) {
            LOGGER.info("NCESNotification-requested : re-shared currently ,ApplicationDetailsForDefendant added... ");

            final ApplicationDetailsForDefendant applicationDetailsForDefendant = this.momento.getApplicationDetailsForDefendant();
            final Optional<String> result = getAllScenariosWithGranted(applicationDetailsForDefendant);

            if (result.isPresent()) {
                LOGGER.info("NCESNotification-requested : re-shared currently ,and Application Granted after shared ");
                final FinancialOrderForDefendant newFinancialOrderForDefendant = FinancialOrderForDefendant.newBuilderFrom(resharedFinancialOrderForDefendant)
                        .withDocumentContent(
                                newDocumentWithOld(resharedFinancialOrderForDefendant)
                                        .withAmendmentType(result.get())
                                        .build())
                        .build();
                return Stream.of(
                        DefendantUpdateWithFinancialOrderDetails.newBuilder()
                                .withFinancialOrderForDefendant(newFinancialOrderForDefendant).build(),
                        NCESNotificationRequested.newBuilder()
                                .withFinancialOrderForDefendant(newFinancialOrderForDefendant)
                                .build(),
                        RemoveGrantedApplicationDetailsForDefendant.newBuilder().build()
                );
            }
        }
        return null;
    }

    private DocumentContent.Builder newDocumentWithOld(final FinancialOrderForDefendant financialOrderForDefendant) {
        return DocumentContent.newBuilderFrom(financialOrderForDefendant.getDocumentContent())
                .withOldDivisionCode(this.momento.getFinancialOrderForDefendant().getDocumentContent().getDivisionCode())
                .withOldGobAccountNumber(this.momento.getFinancialOrderForDefendant().getDocumentContent().getGobAccountNumber());
    }




    private Stream<Object> createNotificationWithAmendmentType(final ApplicationDetailsForDefendant applicationDetailsForDefendant, final String amendmentType) {
        return Stream.of(
                DefendantUpdateWithApplicationDetails.Builder.newBuilder()
                        .withApplicationDetailsForDefendant(applicationDetailsForDefendant).build(),
                NCESNotificationRequested.newBuilder()
                        .withFinancialOrderForDefendant(
                                FinancialOrderForDefendant.newBuilderFrom(this.momento.getFinancialOrderForDefendant())
                                        .withDocumentContent(
                                                DocumentContent.newBuilderFrom(this.momento.getFinancialOrderForDefendant().getDocumentContent())
                                                        .withAmendmentType(amendmentType)
                                                        .build()
                                        ).build()
                        ).build()
        );
    }

    private Stream<Object> ncesNotificationWithFinancialOrder(final FinancialOrderForDefendant financialOrderForDefendant) {
        return Stream.of(
                DefendantUpdateWithFinancialOrderDetails.newBuilder()
                        .withFinancialOrderForDefendant(financialOrderForDefendant).build(),
                NCESNotificationRequested.newBuilder()
                        .withFinancialOrderForDefendant(financialOrderForDefendant)
                        .build()
        );
    }

    private Optional<String> checkSharedAndApplicationRefused(final ApplicationDetailsForDefendant applicationDetailsForDefendant) {
        if (this.momento.getFinancialOrderForDefendant() != null
                && this.momento.getFinancialOrderForDefendant().getDocumentContent().getOldGobAccountNumber() == null) {
            return getAllScenariosWithRefusedOrWithdrawnResult(applicationDetailsForDefendant);
        }
        return Optional.empty();
    }

    private boolean hasDeemedServedResults(final FinancialOrderForDefendant financialOrderForDefendant) {
        return financialOrderForDefendant.getResultDefinitionIds() != null &&
                !Collections.disjoint(NCESDecisionConstants.DEEMED_SERVED_RESULTS, financialOrderForDefendant.getResultDefinitionIds());
    }

}
