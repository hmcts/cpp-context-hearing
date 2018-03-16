/**
 * 
 */
package uk.gov.moj.cpp.hearing.query.view.convertor;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_TIME;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.collections.CollectionUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.join;

import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;

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
import uk.gov.moj.cpp.hearing.query.view.response.HearingDetailsResponse.Case;
import uk.gov.moj.cpp.hearing.query.view.response.HearingDetailsResponse.DefenceCounsel;
import uk.gov.moj.cpp.hearing.query.view.response.HearingDetailsResponse.DefendantId;
import uk.gov.moj.cpp.hearing.query.view.response.HearingDetailsResponse.Plea;
import uk.gov.moj.cpp.hearing.query.view.response.HearingDetailsResponse.ProsecutionCounsel;
import uk.gov.moj.cpp.hearing.query.view.response.HearingDetailsResponse.Value;
import uk.gov.moj.cpp.hearing.query.view.response.HearingDetailsResponse.Verdict;

/**
 * HearingDetailsResponseConverter. Deep conversion from a given object {@link Ahearing} to an object {@link HearingDetailsResponse}
 */
public final class HearingDetailsResponseConverter implements Converter<Ahearing, HearingDetailsResponse> {
    
    private static final HearingDetailsResponseConverter HEARING_DETAILS_RESPONSE_CONVERTER = new HearingDetailsResponseConverter();
    private static final AttendeesConverter ATTENDEE_CONVERTER = new AttendeesConverter();
    private static final JudgeConverter JUDGE_CONVERTER = new JudgeConverter();
    private static final CasesConverter CASES_CONVERTER = new CasesConverter();

    /**
     * 
     * @param source
     * @return
     */
    public static HearingDetailsResponse toHearingDetailsResponse(final Ahearing source) {
        return HEARING_DETAILS_RESPONSE_CONVERTER.convert(source);
    }
    
    /* (non-Javadoc)
     * @see uk.gov.justice.services.common.converter.Converter#convert(java.lang.Object)
     */
    @Override
    public HearingDetailsResponse convert(final Ahearing source) {
        if (null == source || null == source.getId()) {
            return new HearingDetailsResponse();
        }
        
        final Optional<Temporal> startDateTime = Optional.of(source.getStartDateTime());
        final Optional<UUID> roomId = Optional.of(source.getRoomId());
        final Optional<UUID> courtCentreId = Optional.of(source.getCourtCentreId());

        return new HearingDetailsResponse()
                .withHearingId(source.getId().toString())
                .withStartDate(toDateStringOrNull(startDateTime))
                .withStartTime(toTimeStringOrNull(startDateTime))
                .withRoomName(source.getRoomName())
                .withHearingType(source.getHearingType())
                .withCourtCentreName(source.getCourtCentreName())
                .withJudge(toJudgeOrNull(source.getAttendees()))
                .withRoomId(toStringOrNull(roomId))
                .withCourtCentreId(toStringOrNull(courtCentreId))
                .withAttendees(toAttendeesOrNull(source.getAttendees()))
                .withCases(toCasesOrNull(source));
    }

    // Private methods
    //-----------------------------------------------------------------------
    /**
     * @param source
     * @return
     */
    private static String toStringOrNull(final Optional<?> source) {
        return source.isPresent() ? source.get().toString() : null;
    }

    /**
     * @param source
     * @return
     */
    private static String toTimeStringOrNull(final Optional<Temporal> source) {
        return source.isPresent() ? ISO_LOCAL_TIME.format(source.get()) : null;
    }

    /**
     * @param source
     * @return
     */
    private static String toDateStringOrNull(final Optional<Temporal> source) {
        return source.isPresent() ? ISO_LOCAL_DATE.format(source.get()) : null;
    }

    /**
     * @param source
     * @return
     */
    private static HearingDetailsResponse.Judge toJudgeOrNull(final List<Attendee> source) {
        return JUDGE_CONVERTER.convert(source);
    }
    
    /**
     * @param source
     * @return
     */
    private static HearingDetailsResponse.Attendees toAttendeesOrNull(final List<Attendee> source) {
        return ATTENDEE_CONVERTER.convert(source);
    }
    
    /**
     * @param source
     * @return
     */
    private static List<HearingDetailsResponse.Case> toCasesOrNull(final Ahearing source) {
        return CASES_CONVERTER.convert(source);
    }
    
    // JudgeConverter
    //-----------------------------------------------------------------------
    /**
     * JudgeConverter. Converts a given object {@link Judge} to an object {@link HearingDetailsResponse.Judge}
     */
    private static final class JudgeConverter implements Converter<List<Attendee>, HearingDetailsResponse.Judge> {

        /*
         * (non-Javadoc)
         * @see uk.gov.justice.services.common.converter.Converter#convert(java.lang.Object)
         */
        @Override
        public HearingDetailsResponse.Judge convert(final List<Attendee> source) {
            if (isEmpty(source)) {
                return null;
            }
            
            /* for now we are expecting only one judge per hearing, but it can be changes in
             * the future */
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
    /**
     * AttendeesConverter. Converts a given list of objects {@link Attendee} to an object {@link HearingDetailsResponse.Attendees}
     */
    private static final class AttendeesConverter implements Converter<List<Attendee>, HearingDetailsResponse.Attendees> {
        
        private static final ProsecutionCounselConverter PROSECUTION_COUNSEL_CONVERTER = new ProsecutionCounselConverter();
        private static final DefenseCounselConverter DEFENSE_COUNSEL_CONVERTER = new DefenseCounselConverter(); 
        
        /*
         * (non-Javadoc)
         * @see uk.gov.justice.services.common.converter.Converter#convert(java.lang.Object)
         */
        @Override
        public HearingDetailsResponse.Attendees convert(final List<Attendee> source) {
            if (isEmpty(source)) {
                return null;
            }
            return new HearingDetailsResponse.Attendees()
                    .withProsecutionCounsels(source.stream().filter(filterProcecutor()).map(convertProcecutor()).collect(toList()))
                    .withDefenceCounsels(source.stream().filter(filterDefence()).map(convertDefence()).collect(toList()));
        }

        /**
         * @return
         */
        private Predicate<? super Attendee> filterDefence() {
            return d -> d instanceof DefenceAdvocate;
        }

        /**
         * @return
         */
        private Function<? super Attendee, ? extends DefenceCounsel> convertDefence() {
            return d -> DEFENSE_COUNSEL_CONVERTER.convert((DefenceAdvocate) d);
        }

        /**
         * @return
         */
        private Predicate<? super Attendee> filterProcecutor() {
            return p -> p instanceof ProsecutionAdvocate;
        }
        
        /**
         * @return
         */
        private Function<? super Attendee, ? extends ProsecutionCounsel> convertProcecutor() {
            return p -> PROSECUTION_COUNSEL_CONVERTER.convert((ProsecutionAdvocate) p);
        }
    }
    
    // ProsecutionCounselConverter
    //-----------------------------------------------------------------------
    /**
     * ProsecutionCounselConverter. Converts a given object {@link ProsecutionAdvocate} to an object {@link HearingDetailsResponse.ProsecutionCounsel}
     */
    private static final class ProsecutionCounselConverter implements Converter<ProsecutionAdvocate, HearingDetailsResponse.ProsecutionCounsel> {

        /*
         * (non-Javadoc)
         * @see uk.gov.justice.services.common.converter.Converter#convert(java.lang.Object)
         */
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
    /**
     * DefenseCounselConverter. Converts a given object {@link DefenceAdvocate} to an object {@link HearingDetailsResponse.DefenceCounsel}
     */
    private static final class DefenseCounselConverter implements Converter<DefenceAdvocate, HearingDetailsResponse.DefenceCounsel> {

        /*
         * (non-Javadoc)
         * @see uk.gov.justice.services.common.converter.Converter#convert(java.lang.Object)
         */
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
    /**
     * CasesConverter. Converts a given object {@link Ahearing} to a list of objects {@link HearingDetailsResponse.Case}
     */
    private static final class CasesConverter implements Converter<Ahearing, List<HearingDetailsResponse.Case>> {

        private static final CaseConverter CASE_CONVERTER = new CaseConverter();
        
        /*
         * (non-Javadoc)
         * @see uk.gov.justice.services.common.converter.Converter#convert(java.lang.Object)
         */
        @Override
        public List<HearingDetailsResponse.Case> convert(final Ahearing source) {
            if (null == source || null == source.getId()) {
                return new ArrayList<>();
            }
            
            // 1. building a set of legal cases to avoid duplications
            final Set<LegalCase> legalCases = source.getDefendants()
                    .stream().flatMap(defendant -> defendant.getOffences().stream())
                    .collect(toList())
                    .stream().map(offence -> offence.getLegalCase())
                    .collect(toSet());

            // 2. building a data structure map to build the expected response 
            final Map<LegalCase, Map<Defendant, List<Offence>>> caseDefendants = new LinkedHashMap<>();
            
            // 3. filtering the given data and filling the structure map
            legalCases.forEach(legalCase -> {
                final Map<Defendant, List<Offence>> defendantOffences = new LinkedHashMap<>();
                source.getDefendants().forEach(defendant -> {
                    final List<Offence> offences = new ArrayList<>();
                    defendant.getOffences().forEach(offence -> {
                        if (legalCase.getId().equals(offence.getLegalCase().getId())) {
                            if (!defendantOffences.containsKey(defendant)) {
                                defendantOffences.put(defendant, offences);
                            }
                            offences.add(offence);
                        }
                    });
                });
                caseDefendants.put(legalCase, defendantOffences);
            });
            
            // 4. converting the data structure map to the respective Java Script Object Notation entities
            return caseDefendants.entrySet().stream().map(convert()).collect(toList());
        }

        /**
         * @return
         */
        private Function<? super Entry<LegalCase, Map<Defendant, List<Offence>>>, ? extends Case> convert() {
            return e -> CASE_CONVERTER.convert(e);
        }
    }
    
    // CasesConverter
    //-----------------------------------------------------------------------
    /**
     * CasesConverter. Converts a given entry of object of {@link LegalCase} to an object {@link HearingDetailsResponse.Case}
     */
    private static final class CaseConverter implements Converter<Entry<LegalCase, Map<Defendant, List<Offence>>>, HearingDetailsResponse.Case> {
        
        private static final DefendantConverter DEFENDANT_CONVERTER = new DefendantConverter();

        /*
         * (non-Javadoc)
         * @see uk.gov.justice.services.common.converter.Converter#convert(java.lang.Object)
         */
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

        /**
         * @return
         */
        private Function<? super Entry<Defendant, List<Offence>>, ? extends HearingDetailsResponse.Defendant> convert() {
            return e -> DEFENDANT_CONVERTER.convert(e);
        }
    }
    
    // DefendantConverter
    //-----------------------------------------------------------------------
    /**
     * DefendantConverter. Converts a given entry of object {@link Defendant} to an object {@link HearingDetailsResponse.Defendant}
     */
    private static final class DefendantConverter implements Converter<Entry<Defendant, List<Offence>>, HearingDetailsResponse.Defendant> {

        private static final AddressConverter ADDRESS_CONVERTER = new AddressConverter();
        private static final OffenceConverter OFFENCE_CONVERTER = new OffenceConverter();

        /*
         * (non-Javadoc)
         * @see uk.gov.justice.services.common.converter.Converter#convert(java.lang.Object)
         */
        @Override
        public HearingDetailsResponse.Defendant convert(final Entry<Defendant, List<Offence>> source) {
            if (null == source || null == source.getKey()) {
                return null;
            }

            final Defendant defendant = source.getKey();
            final Optional<Temporal> dateOfBirth = Optional.of(defendant.getDateOfBirth());

            return new HearingDetailsResponse.Defendant()
                    .withDefendantId(defendant.getId().getId().toString())
                    .withPersonId(defendant.getPersonId().toString())
                    .withFirstName(defendant.getFirstName())
                    .withLastName(defendant.getLastName())
                    .withHomeTelephone(defendant.getHomeTelephone())
                    .withMobile(defendant.getMobileTelephone())
                    .withFax(defendant.getFax())
                    .withEmail(defendant.getEmail())
                    .withAddress(convert(defendant.getAddress()))
                    .withDateOfBirth(toDateStringOrNull(dateOfBirth))
                    .withOffences(source.getValue().stream().map(convert()).collect(toList()));
        }

        /**
         * @param defendant
         * @return
         */
        private HearingDetailsResponse.Address convert(final Address source) {
            return ADDRESS_CONVERTER.convert(source);
        }

        /**
         * @return
         */
        private Function<? super Offence, ? extends HearingDetailsResponse.Offence> convert() {
            return o -> OFFENCE_CONVERTER.convert(o);
        }
    }
    
    // AddressConverter
    //-----------------------------------------------------------------------
    /**
     * AddressConverter. Converts a given object {@link Address} to an object {@link HearingDetailsResponse.Address}
     */
    private static final class AddressConverter implements Converter<Address, HearingDetailsResponse.Address> {

        /*
         * (non-Javadoc)
         * @see uk.gov.justice.services.common.converter.Converter#convert(java.lang.Object)
         */
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
                    .withFormatedAddress(format(address1, address2, address3, address4, postCode));
        }
        
        /**
         * @param vals
         * @return
         */
        private static String format(final String... vals) {
            return join(vals, ' ').trim();
        }
    }
    
    // OffenceConverter
    //-----------------------------------------------------------------------
    /**
     * OffenceConverter. Converts a given entry of object {@link Defendant} to an object {@link HearingDetailsResponse.Defendant}
     */
    private static final class OffenceConverter implements Converter<Offence, HearingDetailsResponse.Offence> {
        
        private static final PleaConveter PLEA_CONVETER = new PleaConveter();
        private static final VerdictConveter VERDICT_CONVETER = new VerdictConveter();
        
        /*
         * (non-Javadoc)
         * @see uk.gov.justice.services.common.converter.Converter#convert(java.lang.Object)
         */
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
                    .withPlea(convertPlea(source))
                    .withVerdict(convertVerdict(source));
        }

        /**
         * @param source
         * @return
         */
        private Verdict convertVerdict(final Offence source) {
            return VERDICT_CONVETER.convert(source);
        }

        /**
         * @param source
         * @return
         */
        private static Plea convertPlea(final Offence source) {
            return PLEA_CONVETER.convert(source);
        }
    }
    
    // PleaConveter
    //-----------------------------------------------------------------------
    /**
     * PleaConveter. Converts a given entry of object {@link Offence} to an object {@link HearingDetailsResponse.Plea}
     */
    private static final class PleaConveter implements Converter<Offence, HearingDetailsResponse.Plea> {

        /*
         * (non-Javadoc)
         * @see uk.gov.justice.services.common.converter.Converter#convert(java.lang.Object)
         */
        @Override
        public HearingDetailsResponse.Plea convert(final Offence source) {
            if (null == source || null == source.getId() || null == source.getId().getId()) {
                return null;
            }
            return new HearingDetailsResponse.Plea()
                    .withPleaId(source.getPleaId().toString())
                    .withPleaDate(toDateStringOrNull(Optional.of(source.getPleaDate())));
        }
    }
    
    // VerdictConveter
    //-----------------------------------------------------------------------
    /**
     * VerdictConveter. Converts a given entry of object {@link Offence} to an object {@link HearingDetailsResponse.Verdict}
     */
    private static final class VerdictConveter implements Converter<Offence, HearingDetailsResponse.Verdict> {

        private static final ValueConveter VALUE_CONVETER = new ValueConveter();

        /*
         * (non-Javadoc)
         * @see uk.gov.justice.services.common.converter.Converter#convert(java.lang.Object)
         */
        @Override
        public HearingDetailsResponse.Verdict convert(final Offence source) {
            if (null == source || null == source.getId() || null == source.getId().getId()) {
                return null;
            }
            return new HearingDetailsResponse.Verdict()
                    .withVerdictId(source.getVerdictId().toString())
                    .withVerdictDate(toDateStringOrNull(Optional.of(source.getVerdictDate())))
                    .withNumberOfJurors(source.getNumberOfJurors())
                    .withNumberOfSplitJurors(source.getNumberOfSplitJurors())
                    .withUnanimous(source.getUnanimous())
                    .withValue(convertValue(source));
        }

        /**
         * @param source
         * @return
         */
        private static Value convertValue(final Offence source) {
            return VALUE_CONVETER.convert(source);
        }
    }
    
    // ValueConveter
    //-----------------------------------------------------------------------
    /**
     * ValueConveter. Converts a given entry of object {@link Offence} to an object {@link HearingDetailsResponse.Value}
     */
    private static final class ValueConveter implements Converter<Offence, HearingDetailsResponse.Value> {

        /*
         * (non-Javadoc)
         * @see uk.gov.justice.services.common.converter.Converter#convert(java.lang.Object)
         */
        @Override
        public HearingDetailsResponse.Value convert(final Offence source) {
            if (null == source || null == source.getId() || null == source.getId().getId()) {
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
