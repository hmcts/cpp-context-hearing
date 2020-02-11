package uk.gov.moj.cpp.hearing.domain.aggregate.hearing;

import static java.util.Arrays.asList;
import static java.util.UUID.fromString;

import uk.gov.moj.cpp.hearing.nces.ApplicationDetailsForDefendant;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiPredicate;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.Pair;

public class NCESDecisionConstants implements Serializable {
    public static final UUID RD_FIDICI = fromString("de946ddc-ad77-44b1-8480-8bbc251cdcfb");
    public static final UUID RD_FIDICTI = fromString("5c023b16-e79c-4eb5-9673-e23accbeb35b");
    public static final UUID RD_FIDIPI = fromString("0e390ae0-8f3c-4735-8c0d-c16e8962537a");
    private static final UUID APPEAL_AGAINST_CONVICTION_ID = fromString("57810183-a5c2-3195-8748-c6b97eda1ebd");
    private static final UUID APPEAL_AGAINST_SENTENCE_ID = fromString("beb08419-0a9a-3119-b3ec-038d56c8a718");
    private static final UUID APPLICATION_TO_REOPEN_CASE_ID = fromString("44c238d9-3bc2-3cf3-a2eb-a7d1437b8383");
    private static final UUID APPEARANCE_TO_MAKE_STATUTORY_DECLARATION_SJP_CASE_ID = fromString("7375727f-30fc-3f55-99f3-36adc4f0e70e");
    private static final UUID APPEARANCE_TO_MAKE_STATUTORY_DECLARATION_ID = fromString("f3a6e917-7cc8-3c66-83dd-d958abd6a6e4");


    private static final UUID GRANTED_APPLICATION_OUTCOME_ID = fromString("c322f934-6b70-3fdd-b196-8628d5ee68db");
    private static final UUID REFUSED_APPLICATION_OUTCOME_ID = fromString("f48b2061-84b7-3429-8345-2ea4c3e88a3a");
    private static final UUID WITHDRAWN_APPLICATION_OUTCOME_ID = fromString("f62dedad-685b-370f-899b-61e94084dab2");

    public static final List<UUID> DEEMED_SERVED_RESULTS = Collections.unmodifiableList(asList(RD_FIDICI, RD_FIDICTI, RD_FIDIPI));

    @SuppressWarnings({"squid:S1067"})
    private static List<Pair<String, BiPredicate<UUID, UUID>>> refusedApplications = Collections.unmodifiableList(
            Arrays.asList(
                    Pair.of("Stat dec refused",
                            (applicationId, applicationOutcomeTypeId) ->
                                    (APPEARANCE_TO_MAKE_STATUTORY_DECLARATION_ID.equals(applicationId)
                                            || APPEARANCE_TO_MAKE_STATUTORY_DECLARATION_SJP_CASE_ID.equals(applicationId))
                                            && REFUSED_APPLICATION_OUTCOME_ID.equals(applicationOutcomeTypeId)),

                    Pair.of("Reopen refused",
                            (applicationId, applicationOutcomeTypeId) ->
                                    APPLICATION_TO_REOPEN_CASE_ID.equals(applicationId)
                                            && REFUSED_APPLICATION_OUTCOME_ID.equals(applicationOutcomeTypeId)),

                    Pair.of("Appeal refused",
                            (applicationId, applicationOutcomeTypeId) ->
                                    (APPEAL_AGAINST_CONVICTION_ID.equals(applicationId)
                                            || APPEAL_AGAINST_SENTENCE_ID.equals(applicationId))
                                            && REFUSED_APPLICATION_OUTCOME_ID.equals(applicationOutcomeTypeId))

            ));

    @SuppressWarnings({"squid:S1067"})
    private static List<Pair<String, BiPredicate<UUID, UUID>>> withDrawnApplications = Collections.unmodifiableList(
            Arrays.asList(
                    Pair.of("Stat dec withdrawn",
                            (applicationId, applicationOutcomeTypeId) ->
                                    (APPEARANCE_TO_MAKE_STATUTORY_DECLARATION_ID.equals(applicationId)
                                            || APPEARANCE_TO_MAKE_STATUTORY_DECLARATION_SJP_CASE_ID.equals(applicationId))
                                            && WITHDRAWN_APPLICATION_OUTCOME_ID.equals(applicationOutcomeTypeId)),

                    Pair.of("Reopen withdrawn",
                            (applicationId, applicationOutcomeTypeId) ->
                                    APPLICATION_TO_REOPEN_CASE_ID.equals(applicationId)
                                            && WITHDRAWN_APPLICATION_OUTCOME_ID.equals(applicationOutcomeTypeId)),

                    Pair.of("Appeal withdrawn",
                            (applicationId, applicationOutcomeTypeId) ->
                                    (APPEAL_AGAINST_CONVICTION_ID.equals(applicationId)
                                            || APPEAL_AGAINST_SENTENCE_ID.equals(applicationId))
                                            && WITHDRAWN_APPLICATION_OUTCOME_ID.equals(applicationOutcomeTypeId))
            ));

    @SuppressWarnings({"squid:S1067"})
    private static List<Pair<String, BiPredicate<UUID, UUID>>> grantedApplications = Collections.unmodifiableList(
            Arrays.asList(
                    Pair.of("Stat dec granted",
                            (applicationId, applicationOutcomeTypeId) ->
                                    (APPEARANCE_TO_MAKE_STATUTORY_DECLARATION_ID.equals(applicationId)
                                            || APPEARANCE_TO_MAKE_STATUTORY_DECLARATION_SJP_CASE_ID.equals(applicationId))
                                            && GRANTED_APPLICATION_OUTCOME_ID.equals(applicationOutcomeTypeId)),
                    Pair.of("Appeal granted",
                            (applicationId, applicationOutcomeTypeId) ->
                                    (APPEAL_AGAINST_CONVICTION_ID.equals(applicationId)
                                            || APPEAL_AGAINST_SENTENCE_ID.equals(applicationId))
                                            && GRANTED_APPLICATION_OUTCOME_ID.equals(applicationOutcomeTypeId)),

                    Pair.of("Reopen granted",
                            (applicationId, applicationOutcomeTypeId) ->
                                    APPLICATION_TO_REOPEN_CASE_ID.equals(applicationId)
                                            && GRANTED_APPLICATION_OUTCOME_ID.equals(applicationOutcomeTypeId))
            ));


    static Optional<String> getAllScenariosWithRefusedOrWithdrawnResult(ApplicationDetailsForDefendant applicationDetailsForDefendant) {
        return Stream.concat(refusedApplications.stream(), withDrawnApplications.stream())
                .filter(pair -> pair.getValue().test(
                        applicationDetailsForDefendant.getApplicationTypeId(), applicationDetailsForDefendant.getApplicationOutcomeTypeId()))
                .map(Pair::getKey)
                .findFirst();
    }

    static Optional<String> getAllScenariosWithGranted(ApplicationDetailsForDefendant applicationDetailsForDefendant) {
        return grantedApplications.stream()
                .filter(pair -> pair.getValue().test(
                        applicationDetailsForDefendant.getApplicationTypeId(), applicationDetailsForDefendant.getApplicationOutcomeTypeId()))
                .map(Pair::getKey)
                .findFirst();
    }
}
