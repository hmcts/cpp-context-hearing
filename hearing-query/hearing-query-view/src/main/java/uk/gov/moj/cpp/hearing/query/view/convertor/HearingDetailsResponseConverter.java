package uk.gov.moj.cpp.hearing.query.view.convertor;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_TIME;
import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.collections.CollectionUtils.isEmpty;

import uk.gov.justice.services.common.converter.Converter;
import uk.gov.moj.cpp.hearing.command.witness.DefenceWitness;
import uk.gov.moj.cpp.hearing.command.DefendantId;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Address;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Attendee;
import uk.gov.moj.cpp.hearing.persist.entity.ha.DefenceAdvocate;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Defendant;
import uk.gov.moj.cpp.hearing.persist.entity.ha.DefendantCase;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingDate;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Judge;
import uk.gov.moj.cpp.hearing.persist.entity.ha.LegalCase;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Offence;
import uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionAdvocate;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Witness;
import uk.gov.moj.cpp.hearing.query.view.response.hearingResponse.Attendees;
import uk.gov.moj.cpp.hearing.query.view.response.hearingResponse.Case;
import uk.gov.moj.cpp.hearing.query.view.response.hearingResponse.DefenceCounsel;
import uk.gov.moj.cpp.hearing.query.view.response.hearingResponse.HearingDetailsResponse;
import uk.gov.moj.cpp.hearing.query.view.response.hearingResponse.Plea;
import uk.gov.moj.cpp.hearing.query.view.response.hearingResponse.ProsecutionCounsel;
import uk.gov.moj.cpp.hearing.query.view.response.hearingResponse.Value;
import uk.gov.moj.cpp.hearing.query.view.response.hearingResponse.Verdict;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class HearingDetailsResponseConverter implements Converter<Hearing, HearingDetailsResponse> {

    @Override
    public HearingDetailsResponse convert(final Hearing source) {
        if (null == source || null == source.getId()) {
            return new HearingDetailsResponse();
        }

        return HearingDetailsResponse.builder()
                .withHearingId(source.getId().toString())
                .withStartDate(toDateStringOrNull(getHearingDayDate(source.getHearingDays())))
                .withStartTime(toTimeStringOrNull(getHearingDayDate(source.getHearingDays())))
                .withHearingDays(source.getHearingDays().stream()
                        .map(ahd -> ahd.getDateTime().toInstant().toString())
                        .sorted()
                        .collect(toList()))
                .withRoomName(source.getRoomName())
                .withHearingType(source.getHearingType())
                .withCourtCentreName(source.getCourtCentreName())
                .withJudge(new JudgeConverter().convert(source.getAttendees()))
                .withRoomId(toStringOrNull(source.getRoomId()))
                .withCourtCentreId(toStringOrNull(source.getCourtCentreId()))
                .withAttendees(new AttendeesConverter().convert(source.getAttendees()))
                .withCases(new CasesConverter().convert(source))
                .withDefenceWiteness(new DefenceWitnessesConverter().convert(source))
                .build();
    }


    private static String toStringOrNull(final Object source) {
        return ofNullable(source).map(Object::toString).orElse(null);
    }

    private static String toTimeStringOrNull(final Temporal source) {
        return ofNullable(source).map(ISO_LOCAL_TIME::format).orElse(null);
    }

    private static String toDateStringOrNull(final Temporal source) {
        return ofNullable(source).map(ISO_LOCAL_DATE::format).orElse(null);
    }

    private static ZonedDateTime getHearingDayDate(final  List<HearingDate>  source) {
        if (isEmpty(source)) {
            return null;
        }else{
            final List<ZonedDateTime> hearingDays = source.stream()
                    .map(HearingDate::getDateTime)
                    .sorted()
                    .collect(Collectors.toList());
            return hearingDays.get(0);
        }
    }

    // JudgeConverter
    //-----------------------------------------------------------------------
    private static final class JudgeConverter implements Converter<List<Attendee>, uk.gov.moj.cpp.hearing.query.view.response.hearingResponse.Judge> {

        @Override
        public uk.gov.moj.cpp.hearing.query.view.response.hearingResponse.Judge convert(final List<Attendee> source) {
            if (isEmpty(source)) {
                return null;
            }
            final Optional<Attendee> judge = source.stream().filter(a -> a instanceof Judge).findFirst();

            if (!judge.isPresent()) {
                return null;
            }

            return uk.gov.moj.cpp.hearing.query.view.response.hearingResponse.Judge.builder()
                    .withId(judge.get().getId().getId().toString())
                    .withTitle(judge.get().getTitle())
                    .withFirstName(judge.get().getFirstName())
                    .withLastName(judge.get().getLastName())
                    .build();
        }
    }

    // AttendeesConverter
    //-----------------------------------------------------------------------
    private static final class AttendeesConverter implements Converter<List<Attendee>, Attendees> {

        @Override
        public Attendees convert(final List<Attendee> source) {
            if (isEmpty(source)) {
                return null;
            }
            return Attendees.builder()
                    .withProsecutionCounsels(source.stream()
                            .filter(p -> p instanceof ProsecutionAdvocate)
                            .map(ProsecutionAdvocate.class::cast)
                            .map(p -> new ProsecutionCounselConverter().convert(p))
                            .collect(toList()))
                    .withDefenceCounsels(source.stream()
                            .filter(d -> d instanceof DefenceAdvocate)
                            .map(DefenceAdvocate.class::cast)
                            .map(d -> new DefenseCounselConverter().convert(d))
                            .collect(toList()))
                    .build();
        }
    }

    // ProsecutionCounselConverter
    //-----------------------------------------------------------------------
    private static final class ProsecutionCounselConverter implements Converter<ProsecutionAdvocate, ProsecutionCounsel> {

        @Override
        public ProsecutionCounsel convert(final ProsecutionAdvocate source) {
            if (null == source || null == source.getId() || null == source.getId().getId()) {
                return null;
            }
            return ProsecutionCounsel.builder()
                    .withAttendeeId(source.getId().getId().toString())
                    .withStatus(source.getStatus())
                    .withTitle(source.getTitle())
                    .withFirstName(source.getFirstName())
                    .withLastName(source.getLastName())
                    .withHearingDates(source.getHearingDates())
                    .build();
        }
    }

    // DefenseCounselConverter
    //-----------------------------------------------------------------------
    private static final class DefenseCounselConverter implements Converter<DefenceAdvocate, DefenceCounsel> {

        @Override
        public DefenceCounsel convert(final DefenceAdvocate source) {
            if (null == source || null == source.getId() || null == source.getId().getId()) {
                return null;
            }

            final String defendantId = source.getDefendants().isEmpty() ? "" :
                    source.getDefendants().get(0).getId().getId().toString();

            return DefenceCounsel.builder()
                    .withAttendeeId(source.getId().getId().toString())
                    .withStatus(source.getStatus())
                    .withTitle(source.getTitle())
                    .withFirstName(source.getFirstName())
                    .withLastName(source.getLastName())
                    .withDefendantId(defendantId)
                    .withHearingDates(source.getHearingDates())
                    .build();
        }
    }

    // DefenceWitnessesConverter
    private static final class DefenceWitnessesConverter implements Converter<Hearing, List<DefenceWitness>> {

        @Override
        public List<DefenceWitness> convert(Hearing source) {
            if (null == source || null == source.getId() || isEmpty(source.getDefendants())) {
                return emptyList();
            }

            //  building a set of witness to avoid duplications
            final Set<Witness> witnesses = source.getDefendants().stream()
                    .flatMap(defendant -> defendant.getDefendantWitnesses().stream())
                    .distinct()
                    .collect(toSet());

            return witnesses.stream().map( w -> new DefenceWitnessConverter().convert(w)).collect(toList());
        }
    }

    private static final class DefenceWitnessConverter implements Converter<Witness, DefenceWitness> {

        @Override
        public DefenceWitness convert(final Witness source) {
            if (null == source || null == source.getId()) {
                return null;
            }

            return DefenceWitness.builder()
                    .withId(toStringOrNull(source.getId().getId()))
                    .withtType(source.getType())
                    .withClassification(source.getClassification())
                    .withTitle(source.getTitle())
                    .withFirstName(source.getFirstName())
                    .withLastName(source.getLastName())
                    .withDefendants(source.getDefendants().stream().map( d -> (DefendantId.builder().withDefendantId(d.getId().getId())).build()).collect(toList()))
                    .build();

        }
    }

    // CasesConverter
    //-----------------------------------------------------------------------
    private static final class CasesConverter implements Converter<Hearing, List<Case>> {

        @Override
        public List<Case> convert(final Hearing source) {
            if (null == source || null == source.getId() || isEmpty(source.getDefendants())) {
                return emptyList();
            }

            // 1. building a set of legal cases to avoid duplications
            final Set<LegalCase> legalCases = source.getDefendants().stream()
                    .flatMap(defendant -> defendant.getOffences().stream())
                    .map(Offence::getLegalCase)
                    .distinct()
                    .collect(toSet());

            if (isEmpty(legalCases)) {
                return emptyList();
            }

            // 2. building a data structure map to build the expected response
            final Map<LegalCase, Cases> cases = new LinkedHashMap<>();

            // 3. filtering the given data and filling the structure map
            legalCases.forEach(legalCase -> {
                cases.putIfAbsent(legalCase, new Cases());

                source.getDefendants().forEach(d -> {
                    cases.get(legalCase).getDefendants().putIfAbsent(d, new ArrayList<>());
                    d.getOffences().stream().filter(offence -> isOffenceCase(offence, legalCase)).forEach(cases.get(legalCase).getDefendants().get(d)::add);
                });

                source.getWitnesses().stream().filter(witness -> isWitnessCase(witness, legalCase)).forEach(cases.get(legalCase).getWitnesses()::add);
            });

            // 4. converting the data structure map to the respective Java Script Object
            // Notation entities
            return cases.entrySet().stream().map(e -> new CaseConverter().convert(e)).collect(toList());
        }

        private boolean isWitnessCase(final Witness witness, final LegalCase legalCase) {
            return null != legalCase.getId() && null != witness.getLegalCase() && legalCase.getId().equals(witness.getLegalCase().getId());
        }


        private boolean isOffenceCase(final Offence offence, final LegalCase legalCase) {
            return null != legalCase.getId() && null != offence.getLegalCase() && legalCase.getId().equals(offence.getLegalCase().getId());
        }

    }

    // Cases class
    private static final class Cases {
        Map<Defendant, List<Offence>> defendants = new HashMap<>();
        List<Witness> witnesses = new ArrayList<>();

        public Map<Defendant, List<Offence>> getDefendants() {
            return defendants;
        }

        public void setDefendants(final Map<Defendant, List<Offence>> defendants) {
            this.defendants = defendants;
        }

        public List<Witness> getWitnesses() {
            return witnesses;
        }

        public void setWitnesses(final List<Witness> witnesses) {
            this.witnesses = witnesses;
        }
    }

    // CasesConverter
    //-----------------------------------------------------------------------
    private static final class CaseConverter implements Converter<Entry<LegalCase, Cases>, Case> {

        @Override
        public Case convert(final Entry<LegalCase, Cases> source) {
            if (null == source || null == source.getKey()) {
                return null;
            }

            final LegalCase legalCase = source.getKey();

            return Case.builder()
                    .withCaseId(legalCase.getId().toString())
                    .withCaseUrn(legalCase.getCaseUrn())
                    .withDefendants(
                            source.getValue().getDefendants().entrySet().stream()
                                    .map(e -> new DefendantConverter().convertDefendant(source.getKey().getId(), e))
                                    .collect(toList())
                    )
                    .withWitnesses(
                            source.getValue().getWitnesses().stream()
                                    .map(w -> new WitnessConverter().convert(w)).collect(toList())
                    )
                    .build();
        }


    }

    // WitnessConverter
    //-----------------------------------------------------------------------
    private static final class WitnessConverter implements Converter<Witness, uk.gov.moj.cpp.hearing.query.view.response.hearingResponse.Witness> {

        @Override
        public uk.gov.moj.cpp.hearing.query.view.response.hearingResponse.Witness convert(final Witness source) {
            if (null == source || null == source.getId()) {
                return null;
            }
            return uk.gov.moj.cpp.hearing.query.view.response.hearingResponse.Witness.builder()
                            .withId(toStringOrNull(source.getId().getId()))
                    .withCaseId(toStringOrNull(source.getLegalCase().getId()))
                    .withtType(source.getType())
                    .withClassification(source.getClassification())
                    .withTitle(source.getTitle())
                    .withFirstName(source.getFirstName())
                    .withLastName(source.getLastName())
                    .withDateOfBirth(toDateStringOrNull(source.getDateOfBirth()))
                    .withNationality(source.getNationality())
                    .withGender(source.getGender())
                    .withHomeTelephone(source.getHomeTelephone())
                    .withWorkTelephone(source.getWorkTelephone())
                    .withMobile(source.getMobileTelephone())
                    .withEmail(source.getEmail())
                    .withFax(source.getFax())
                    .build();

        }
    }

    // DefendantConverter
    //-----------------------------------------------------------------------
    private static final class DefendantConverter {

        public uk.gov.moj.cpp.hearing.query.view.response.hearingResponse.Defendant convertDefendant(final UUID caseId, final Entry<Defendant, List<Offence>> source) {

            final Defendant defendant = source.getKey();

            final String bailStatus = defendant.getDefendantCases()
                    .stream()
                    .filter(dc -> dc.getLegalCase().getId().equals(caseId))
                    .filter(dc -> dc.getBailStatus() != null)
                    .map(DefendantCase::getBailStatus)
                    .findFirst()
                    .orElse(null);

            return uk.gov.moj.cpp.hearing.query.view.response.hearingResponse.Defendant.builder()
                    .withDefendantId(defendant.getId().getId().toString())
                    .withPersonId(toStringOrNull(defendant.getPersonId()))
                    .withFirstName(defendant.getFirstName())
                    .withLastName(defendant.getLastName())
                    .withHomeTelephone(defendant.getHomeTelephone())
                    .withMobile(defendant.getMobileTelephone())
                    .withFax(defendant.getFax())
                    .withEmail(defendant.getEmail())
                    .withAddress(new AddressConverter().convert(defendant.getAddress()))
                    .withDateOfBirth(toDateStringOrNull(defendant.getDateOfBirth()))
                    .withOffences(source.getValue().stream()
                            .map(o -> new OffenceConverter().convert(o))
                            .collect(toList()))
                    .withBailStatus(bailStatus)
                    .build();
        }
    }

    // AddressConverter
    //-----------------------------------------------------------------------
    private static final class AddressConverter implements Converter<Address, uk.gov.moj.cpp.hearing.query.view.response.hearingResponse.Address> {

        @Override
        public uk.gov.moj.cpp.hearing.query.view.response.hearingResponse.Address convert(final Address source) {
            if (null == source) {
                return null;
            }

            final String address1 = source.getAddress1();
            final String address2 = source.getAddress2();
            final String address3 = source.getAddress3();
            final String address4 = source.getAddress4();
            final String postCode = source.getPostCode();

            return uk.gov.moj.cpp.hearing.query.view.response.hearingResponse.Address.builder()
                    .withAddress1(address1)
                    .withAddress2(address2)
                    .withAddress3(address3)
                    .withAddress4(address4)
                    .withPostCode(postCode)
                    .withFormattedAddress(Stream.of(address1, address2, address3, address4, postCode)
                            .filter(Objects::nonNull)
                            .collect(joining(" ")))
                    .build();
        }
    }

    // OffenceConverter
    //-----------------------------------------------------------------------
    private static final class OffenceConverter implements Converter<Offence, uk.gov.moj.cpp.hearing.query.view.response.hearingResponse.Offence> {

        @Override
        public uk.gov.moj.cpp.hearing.query.view.response.hearingResponse.Offence convert(final Offence source) {
            if (null == source || null == source.getId() || null == source.getId().getId()) {
                return null;
            }
            return uk.gov.moj.cpp.hearing.query.view.response.hearingResponse.Offence.builder()
                    .withId(source.getId().getId().toString())
                    .withWording(source.getWording())
                    .withCount(source.getCount())
                    .withTitle(source.getTitle())
                    .withLegislation(source.getLegislation())
                    .withPlea(new PleaConveter().convert(source))
                    .withVerdict(new VerdictConveter().convert(source))
                    .withConvictionDate(ofNullable(source.getConvictionDate()).map(LocalDate::toString).orElse(null))
                    .build();
        }
    }

    // PleaConveter
    //-----------------------------------------------------------------------
    private static final class PleaConveter implements Converter<Offence, Plea> {

        @Override
        public Plea convert(final Offence source) {
            if (null == source || null == source.getId() || null == source.getId().getId()) {
                return null;
            }

            return Plea.builder()
                    .withPleaDate(toDateStringOrNull(source.getPleaDate()))
                    .withValue(source.getPleaValue())
                    .build();
        }
    }

    // VerdictConveter
    //-----------------------------------------------------------------------
    private static final class VerdictConveter implements Converter<Offence, Verdict> {

        @Override
        public Verdict convert(final Offence source) {
            if (null == source || null == source.getId() || null == source.getId().getId()) {
                return null;
            }
            return Verdict.builder()
                    .withVerdictDate(toDateStringOrNull(source.getVerdictDate()))
                    .withNumberOfJurors(source.getNumberOfJurors())
                    .withNumberOfSplitJurors(source.getNumberOfSplitJurors())
                    .withUnanimous(source.getUnanimous())
                    .withValue(new ValueConveter().convert(source))
                    .build();
        }
    }

    // ValueConveter
    //-----------------------------------------------------------------------
    private static final class ValueConveter implements Converter<Offence, Value> {

        @Override
        public Value convert(final Offence source) {
            if (null == source || null == source.getId() || null == source.getId().getId()) {
                return null;
            }
            return Value.builder()
                    .withVerdictTypeId(toStringOrNull(source.getVerdictTypeId()))
                    .withCategory(source.getVerdictCategory())
                    .withCategoryType(source.getVerdictCategoryType())
                    .withLesserOffence(source.getLesserOffence())
                    .withCode(source.getVerdictCode())
                    .withDescription(source.getVerdictDescription())
                    .build();
        }
    }
}
