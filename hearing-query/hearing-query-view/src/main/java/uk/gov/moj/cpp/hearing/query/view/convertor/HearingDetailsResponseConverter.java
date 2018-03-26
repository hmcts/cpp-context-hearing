package uk.gov.moj.cpp.hearing.query.view.convertor;

import org.apache.commons.collections.CollectionUtils;
import uk.gov.justice.services.common.converter.Converter;
import uk.gov.moj.cpp.hearing.persist.entity.ex.Address;
import uk.gov.moj.cpp.hearing.persist.entity.ex.Ahearing;
import uk.gov.moj.cpp.hearing.persist.entity.ex.Attendee;
import uk.gov.moj.cpp.hearing.persist.entity.ex.DefenceAdvocate;
import uk.gov.moj.cpp.hearing.persist.entity.ex.Defendant;
import uk.gov.moj.cpp.hearing.persist.entity.ex.Judge;
import uk.gov.moj.cpp.hearing.persist.entity.ex.LegalCase;
import uk.gov.moj.cpp.hearing.persist.entity.ex.Offence;
import uk.gov.moj.cpp.hearing.persist.entity.ex.ProsecutionAdvocate;
import uk.gov.moj.cpp.hearing.query.view.response.HearingDetailsResponse;

import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_TIME;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.collections.CollectionUtils.isEmpty;

public final class HearingDetailsResponseConverter implements Converter<Ahearing, HearingDetailsResponse> {

    @Override
    public HearingDetailsResponse convert(final Ahearing source) {
        if (null == source || null == source.getId()) {
            return new HearingDetailsResponse();
        }

        final Optional<Temporal> startDateTime = Optional.ofNullable(source.getStartDateTime());
        final Optional<UUID> roomId = Optional.ofNullable(source.getRoomId());
        final Optional<UUID> courtCentreId = Optional.ofNullable(source.getCourtCentreId());

        return new HearingDetailsResponse()
                .withHearingId(source.getId().toString())
                .withStartDate(toDateStringOrNull(startDateTime))
                .withStartTime(toTimeStringOrNull(startDateTime))
                .withRoomName(source.getRoomName())
                .withHearingType(source.getHearingType())
                .withCourtCentreName(source.getCourtCentreName())
                .withJudge(new JudgeConverter().convert(source.getAttendees()))
                .withRoomId(toStringOrNull(roomId))
                .withCourtCentreId(toStringOrNull(courtCentreId))
                .withAttendees(new AttendeesConverter().convert(source.getAttendees()))
                .withCases(new CasesConverter().convert(source));
    }

    // Private methods
    //-----------------------------------------------------------------------
    private static String toStringOrNull(final Optional<?> source) {
        return source.isPresent() ? source.get().toString() : null;
    }

    private static String toTimeStringOrNull(final Optional<Temporal> source) {
        return source.isPresent() ? ISO_LOCAL_TIME.format(source.get()) : null;
    }

    private static String toDateStringOrNull(final Optional<Temporal> source) {
        return source.isPresent() ? ISO_LOCAL_DATE.format(source.get()) : null;
    }

    // JudgeConverter
    //-----------------------------------------------------------------------
    private static final class JudgeConverter implements Converter<List<Attendee>, HearingDetailsResponse.Judge> {

        @Override
        public HearingDetailsResponse.Judge convert(final List<Attendee> source) {
            if (isEmpty(source)) {
                return null;
            }
            final Optional<Attendee> judge = source.stream().filter(a -> a instanceof Judge).findFirst();

            if (!judge.isPresent()) {
                return null;
            }

            return new HearingDetailsResponse.Judge()
                    .withId(judge.get().getId().getId().toString())
                    .withTitle(judge.get().getTitle())
                    .withFirstName(judge.get().getFirstName())
                    .withLastName(judge.get().getLastName());
        }
    }

    // AttendeesConverter
    //-----------------------------------------------------------------------
    private static final class AttendeesConverter implements Converter<List<Attendee>, HearingDetailsResponse.Attendees> {

        @Override
        public HearingDetailsResponse.Attendees convert(final List<Attendee> source) {
            if (isEmpty(source)) {
                return null;
            }
            return new HearingDetailsResponse.Attendees()
                    .withProsecutionCounsels(source.stream().filter(filterProcecutor()).map(convertProcecutor()).collect(toList()))
                    .withDefenceCounsels(source.stream().filter(filterDefence()).map(convertDefence()).collect(toList()));
        }

        private Predicate<? super Attendee> filterDefence() {
            return d -> d instanceof DefenceAdvocate;
        }

        private Function<? super Attendee, ? extends HearingDetailsResponse.DefenceCounsel> convertDefence() {
            return d -> new DefenseCounselConverter().convert((DefenceAdvocate) d);
        }

        private Predicate<? super Attendee> filterProcecutor() {
            return p -> p instanceof ProsecutionAdvocate;
        }

        private Function<? super Attendee, ? extends HearingDetailsResponse.ProsecutionCounsel> convertProcecutor() {
            return p -> new ProsecutionCounselConverter().convert((ProsecutionAdvocate) p);
        }
    }

    // ProsecutionCounselConverter
    //-----------------------------------------------------------------------
    private static final class ProsecutionCounselConverter implements Converter<ProsecutionAdvocate, HearingDetailsResponse.ProsecutionCounsel> {

        @Override
        public HearingDetailsResponse.ProsecutionCounsel convert(final ProsecutionAdvocate source) {
            if (null == source || null == source.getId() || null == source.getId().getId()) {
                return null;
            }
            return new HearingDetailsResponse.ProsecutionCounsel()
                    .withAttendeeId(source.getId().getId().toString())
                    .withStatus(source.getStatus())
                    .withTitle(source.getTitle())
                    .withFirstName(source.getFirstName())
                    .withLastName(source.getLastName());
        }
    }

    // DefenseCounselConverter
    //-----------------------------------------------------------------------
    private static final class DefenseCounselConverter implements Converter<DefenceAdvocate, HearingDetailsResponse.DefenceCounsel> {

        @Override
        public HearingDetailsResponse.DefenceCounsel convert(final DefenceAdvocate source) {
            if (null == source || null == source.getId() || null == source.getId().getId()) {
                return null;
            }
            return new HearingDetailsResponse.DefenceCounsel()
                    .withAttendeeId(source.getId().getId().toString())
                    .withStatus(source.getStatus())
                    .withTitle(source.getTitle())
                    .withFirstName(source.getFirstName())
                    .withLastName(source.getLastName());
        }
    }

    // CasesConverter
    //-----------------------------------------------------------------------
    private static final class CasesConverter implements Converter<Ahearing, List<HearingDetailsResponse.Case>> {

        @Override
        public List<HearingDetailsResponse.Case> convert(final Ahearing source) {

            if (null == source || null == source.getId()) {
                return Collections.emptyList();
            }

            // 1. building a set of legal cases to avoid duplications
            final Set<LegalCase> legalCases = source.getDefendants()
                    .stream()
                    .flatMap(defendant -> defendant.getOffences().stream())
                    .map(Offence::getLegalCase)
                    .collect(toSet());

            // 2. building a data structure map to build the expected response
            final Map<LegalCase, Map<Defendant, List<Offence>>> caseDefendants = new LinkedHashMap<>();

            // 3. filtering the given data and filling the structure map
            legalCases.forEach(legalCase -> {
                final Map<Defendant, List<Offence>> defendantOffences = new LinkedHashMap<>();
                source.getDefendants().forEach(defendant -> {
                    final List<Offence> offences = new ArrayList<>();
                    defendant.getOffences().forEach(offence -> {
                        if (isLegalCaseOffence(legalCase, offence)) {
                            if (!defendantOffences.containsKey(defendant)) {
                                defendantOffences.put(defendant, offences);
                            }
                            offences.add(offence);
                        }
                    });
                });
                caseDefendants.put(legalCase, defendantOffences);
            });

            // 4. converting the data structure map to the respective Java Script Object
            // Notation entities
            return caseDefendants.entrySet().stream().map(convert()).collect(toList());
        }

        private boolean isLegalCaseOffence(LegalCase legalCase, Offence offence) {
            return null != legalCase.getId() && null != offence.getLegalCase() && legalCase.getId().equals(offence.getLegalCase().getId());
        }

        private Function<? super Entry<LegalCase, Map<Defendant, List<Offence>>>, ? extends HearingDetailsResponse.Case> convert() {
            return e -> new CaseConverter().convert(e);
        }
    }

    // CasesConverter
    //-----------------------------------------------------------------------
    private static final class CaseConverter implements Converter<Entry<LegalCase, Map<Defendant, List<Offence>>>, HearingDetailsResponse.Case> {

        @Override
        public HearingDetailsResponse.Case convert(final Entry<LegalCase, Map<Defendant, List<Offence>>> source) {
            if (null == source || null == source.getKey()) {
                return null;
            }

            final LegalCase legalCase = source.getKey();

            return new HearingDetailsResponse.Case()
                    .withCaseId(legalCase.getId().toString())
                    .withCaseUrn(legalCase.getCaseurn())
                    .withDefendants(source.getValue().entrySet().stream().map(convert()).collect(toList()));
        }


        private Function<? super Entry<Defendant, List<Offence>>, ? extends HearingDetailsResponse.Defendant> convert() {
            return e -> new DefendantConverter().convert(e);
        }
    }

    // DefendantConverter
    //-----------------------------------------------------------------------
    private static final class DefendantConverter implements Converter<Entry<Defendant, List<Offence>>, HearingDetailsResponse.Defendant> {

        @Override
        public HearingDetailsResponse.Defendant convert(final Entry<Defendant, List<Offence>> source) {
            if (null == source || null == source.getKey()) {
                return null;
            }

            final Defendant defendant = source.getKey();

            if (null == defendant.getId() || null == defendant.getId().getId()) {
                return null;
            }

            final Optional<Temporal> dateOfBirth = Optional.ofNullable(defendant.getDateOfBirth());

            return new HearingDetailsResponse.Defendant()
                    .withDefendantId(defendant.getId().getId().toString())
                    .withPersonId(toStringOrNull(Optional.ofNullable(defendant.getPersonId())))
                    .withFirstName(defendant.getFirstName())
                    .withLastName(defendant.getLastName())
                    .withHomeTelephone(defendant.getHomeTelephone())
                    .withMobile(defendant.getMobileTelephone())
                    .withFax(defendant.getFax())
                    .withEmail(defendant.getEmail())
                    .withAddress(new AddressConverter().convert(defendant.getAddress()))
                    .withDateOfBirth(toDateStringOrNull(dateOfBirth))
                    .withOffences(source.getValue().stream().map(convert()).collect(toList()));
        }

        private Function<? super Offence, ? extends HearingDetailsResponse.Offence> convert() {
            return o -> new OffenceConverter().convert(o);
        }
    }

    // AddressConverter
    //-----------------------------------------------------------------------
    private static final class AddressConverter implements Converter<Address, HearingDetailsResponse.Address> {

        @Override
        public HearingDetailsResponse.Address convert(final Address source) {
            if (null == source) {
                return null;
            }

            final String address1 = source.getAddress1();
            final String address2 = source.getAddress2();
            final String address3 = source.getAddress3();
            final String address4 = source.getAddress4();
            final String postCode = source.getPostCode();

            return new HearingDetailsResponse.Address()
                    .withAddress1(address1)
                    .withAddress2(address2)
                    .withAddress3(address3)
                    .withAddress4(address4)
                    .withPostCode(postCode)
                    .withformattedAddress(Stream.of(address1, address2, address3, address4, postCode)
                            .filter(Objects::nonNull)
                            .collect(Collectors.joining(" ")));
        }
    }

    // OffenceConverter
    //-----------------------------------------------------------------------
    private static final class OffenceConverter implements Converter<Offence, HearingDetailsResponse.Offence> {

        @Override
        public HearingDetailsResponse.Offence convert(final Offence source) {
            if (null == source || null == source.getId() || null == source.getId().getId()) {
                return null;
            }
            return new HearingDetailsResponse.Offence()
                    .withId(source.getId().getId().toString())
                    .withWording(source.getWording())
                    .withCount(source.getCount())
                    .withTitle(source.getTitle())
                    .withLegislation(source.getLegislation())
                    .withPlea(new PleaConveter().convert(source))
                    .withVerdict(new VerdictConveter().convert(source));
        }
    }

    // PleaConveter
    //-----------------------------------------------------------------------
    private static final class PleaConveter implements Converter<Offence, HearingDetailsResponse.Plea> {

        @Override
        public HearingDetailsResponse.Plea convert(final Offence source) {
            if (null == source || null == source.getId() || null == source.getId().getId()) {
                return null;
            }
            if (null == source.getPleaId()) {
                return null;
            }
            return new HearingDetailsResponse.Plea()
                    .withPleaId(source.getPleaId().toString())
                    .withPleaDate(toDateStringOrNull(Optional.ofNullable(source.getPleaDate())));
        }
    }

    // VerdictConveter
    //-----------------------------------------------------------------------
    private static final class VerdictConveter implements Converter<Offence, HearingDetailsResponse.Verdict> {

        @Override
        public HearingDetailsResponse.Verdict convert(final Offence source) {
            if (null == source || null == source.getId() || null == source.getId().getId()) {
                return null;
            }
            if (null == source.getVerdictId()) {
                return null;
            }
            return new HearingDetailsResponse.Verdict()
                    .withVerdictId(source.getVerdictId().toString())
                    .withVerdictDate(toDateStringOrNull(Optional.ofNullable(source.getVerdictDate())))
                    .withNumberOfJurors(source.getNumberOfJurors())
                    .withNumberOfSplitJurors(source.getNumberOfSplitJurors())
                    .withUnanimous(source.getUnanimous())
                    .withValue(new ValueConveter().convert(source));
        }
    }

    // ValueConveter
    //-----------------------------------------------------------------------
    private static final class ValueConveter implements Converter<Offence, HearingDetailsResponse.Value> {

        @Override
        public HearingDetailsResponse.Value convert(final Offence source) {
            if (null == source || null == source.getId() || null == source.getId().getId()) {
                return null;
            }
            if (null == source.getVerdictId()) {
                return null;
            }
            return new HearingDetailsResponse.Value()
                    .withId(source.getVerdictId().toString()) /* TODO which id must be set here? */
                    .withCategory(source.getVerdictCategory())
                    .withCode(source.getVerdictCode())
                    .withDescription(source.getVerdictDescription());
        }
    }
}