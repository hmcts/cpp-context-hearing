package uk.gov.moj.cpp.hearing.utils;

import static io.github.benas.randombeans.EnhancedRandomBuilder.aNewEnhancedRandom;
import static io.github.benas.randombeans.api.EnhancedRandom.randomStreamOf;

import uk.gov.justice.core.courts.BailStatus;
import uk.gov.justice.core.courts.HearingLanguage;
import uk.gov.justice.core.courts.JurisdictionType;
import uk.gov.justice.services.test.utils.core.random.RandomGenerator;
import uk.gov.moj.cpp.hearing.persist.entity.ha.AllocationDecision;
import uk.gov.moj.cpp.hearing.persist.entity.ha.DefenceCounsel;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.persist.entity.ha.IndicatedPlea;
import uk.gov.moj.cpp.hearing.persist.entity.ha.NotifiedPlea;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Organisation;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Person;
import uk.gov.moj.cpp.hearing.persist.entity.ha.PersonDefendant;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Plea;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Prompt;
import uk.gov.moj.cpp.hearing.persist.entity.ha.ResultLine;

import java.util.UUID;
import java.util.stream.Stream;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public final class HearingJPADataTemplate {

    private final uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing hearing;

    private HearingJPADataTemplate() {
        this(false);
    }

    private HearingJPADataTemplate(final boolean sysoutPrint) {
        //
        final uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing hearingEntity = aNewEnhancedRandom().nextObject(uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing.class);
        hearingEntity.setHearingLanguage(RandomGenerator.values(HearingLanguage.values()).next());
        hearingEntity.setJurisdictionType(RandomGenerator.values(JurisdictionType.values()).next());
        //
        randomStreamOf(1, uk.gov.moj.cpp.hearing.persist.entity.ha.HearingDay.class)
                .forEach(hearingDay -> {
                    hearingDay.setId(aNewHearingSnapshotKey(hearingEntity.getId()));
                    hearingDay.setHearing(hearingEntity);
                    hearingEntity.getHearingDays().add(hearingDay);
                });
        //
        Stream.generate(() -> new uk.gov.moj.cpp.hearing.persist.entity.ha.HearingCaseNote()).limit(1)
                .forEach(hearingCaseNote -> {
                    hearingCaseNote.setId(new HearingSnapshotKey(UUID.randomUUID(), hearingEntity.getId()));
                    hearingCaseNote.setHearing(hearingEntity);
                    hearingEntity.getHearingCaseNotes().add(hearingCaseNote);
                });

        //
        randomStreamOf(1, uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCase.class)
                .forEach(prosecutionCase -> {
                    prosecutionCase.setId(aNewHearingSnapshotKey(hearingEntity.getId()));
                    prosecutionCase.setHearing(hearingEntity);
                    hearingEntity.getProsecutionCases().add(prosecutionCase);
                });
        //
        randomStreamOf(1, uk.gov.moj.cpp.hearing.persist.entity.ha.Defendant.class)
                .forEach(defendant -> {
                    defendant.setId(aNewHearingSnapshotKey(hearingEntity.getId()));

                    final Person person = aNewEnhancedRandom().nextObject(uk.gov.moj.cpp.hearing.persist.entity.ha.Person.class);

                    final PersonDefendant personDefendant = aNewEnhancedRandom().nextObject(uk.gov.moj.cpp.hearing.persist.entity.ha.PersonDefendant.class);
                    personDefendant.setBailStatus(BailStatus.CONDITIONAL.name());
                    personDefendant.setPersonDetails(person);

                    final Organisation legalEntityOrganisation = aNewEnhancedRandom().nextObject(uk.gov.moj.cpp.hearing.persist.entity.ha.Organisation.class);

                    defendant.setLegalEntityOrganisation(legalEntityOrganisation);
                    defendant.setPersonDefendant(personDefendant);
                    defendant.setProsecutionCaseId(hearingEntity.getProsecutionCases().iterator().next().getId().getId());

                    hearingEntity.getProsecutionCases().iterator().next().getDefendants().add(defendant);
                });
        //
        randomStreamOf(1, uk.gov.moj.cpp.hearing.persist.entity.ha.AssociatedPerson.class)
                .forEach(associatedPerson -> {
                    associatedPerson.setId(aNewHearingSnapshotKey(hearingEntity.getId()));
                    hearingEntity.getProsecutionCases().iterator().next().getDefendants().iterator().next().getAssociatedPersons().add(associatedPerson);
                });
        //
        randomStreamOf(1, uk.gov.moj.cpp.hearing.persist.entity.ha.Offence.class)
                .forEach(offence -> {
                    offence.setId(aNewHearingSnapshotKey(hearingEntity.getId()));

                    final NotifiedPlea notifiedPlea = aNewEnhancedRandom().nextObject(uk.gov.moj.cpp.hearing.persist.entity.ha.NotifiedPlea.class);

                    offence.setNotifiedPlea(notifiedPlea);

                    final AllocationDecision allocationDecision = aNewEnhancedRandom().nextObject(uk.gov.moj.cpp.hearing.persist.entity.ha.AllocationDecision.class);

                    final IndicatedPlea indicatedPlea = aNewEnhancedRandom().nextObject(uk.gov.moj.cpp.hearing.persist.entity.ha.IndicatedPlea.class);

                    offence.setIndicatedPlea(indicatedPlea);

                    offence.setAllocationDecision(allocationDecision);

                    final Plea plea = aNewEnhancedRandom().nextObject(uk.gov.moj.cpp.hearing.persist.entity.ha.Plea.class);

                    offence.setPlea(plea);

                    offence.getOffenceFacts().setAlcoholReadingAmount(Integer.valueOf(123).toString());

                    hearingEntity.getProsecutionCases().iterator().next().getDefendants().iterator().next().getOffences().add(offence);
                });
        //
        randomStreamOf(1, uk.gov.moj.cpp.hearing.persist.entity.ha.Target.class)
                .forEach(target -> {
                    target.setHearing(hearingEntity);
                    hearingEntity.getTargets().add(target);
                    randomStreamOf(1, ResultLine.class).forEach(
                            resultLine -> {
                                resultLine.setTarget(target);
                                target.getResultLines().add(resultLine);
                                randomStreamOf(1, Prompt.class).forEach(
                                        prompt -> {
                                            prompt.setResultLine(resultLine);
                                            resultLine.getPrompts().add(prompt);
                                        }
                                );
                            }
                    );
                    hearingEntity.getTargets().add(target);
                });
        //
        randomStreamOf(1, uk.gov.moj.cpp.hearing.persist.entity.ha.DefendantReferralReason.class)
                .forEach(defendantReferralReason -> {
                    defendantReferralReason.setId(aNewHearingSnapshotKey(hearingEntity.getId()));
                    defendantReferralReason.setHearing(hearingEntity);
                    hearingEntity.getDefendantReferralReasons().add(defendantReferralReason);
                });
        //
        randomStreamOf(1, uk.gov.moj.cpp.hearing.persist.entity.ha.JudicialRole.class)
                .forEach(judicialRole -> {
                    judicialRole.setId(aNewHearingSnapshotKey(hearingEntity.getId()));
                    judicialRole.setHearing(hearingEntity);
                    hearingEntity.getJudicialRoles().add(judicialRole);
                });
//        // Will be covered by GGPE-5825 story
//        randomStreamOf(1, DefenceCounsel.class)
//                .forEach(hearingDefenceCounsel -> {
//                    hearingEntity.getDefenceCounsels().add(hearingDefenceCounsel);
//                });
        // Will be covered by GPE-5565 story
        randomStreamOf(1, uk.gov.moj.cpp.hearing.persist.entity.ha.DefendantAttendance.class)
                .forEach(defendantAttendance -> {
                    hearingEntity.getDefendantAttendance().add(defendantAttendance);
                });
//        // Will be covered by GGPE-5825 story
//        randomStreamOf(1, uk.gov.moj.cpp.hearing.persist.entity.ha.HearingProsecutionCounsel.class)
//                .forEach(prosecutionCounsel -> {
//                    hearingEntity.getProsecutionCounsels().add(prosecutionCounsel);
//                });
        // Will be covered by GPE-5479 story
        randomStreamOf(1, uk.gov.moj.cpp.hearing.persist.entity.ha.Target.class)
                .forEach(target -> {
                    hearingEntity.getTargets().add(target);
                });
        //
        this.hearing = hearingEntity;
        if (true == sysoutPrint) {
            System.out.println(ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE));
        }
    }

    private static HearingSnapshotKey aNewHearingSnapshotKey(final UUID hearingId) {
        return new HearingSnapshotKey(UUID.randomUUID(), hearingId);
    }

    public static HearingJPADataTemplate aNewHearingJPADataTemplate() {
        return new HearingJPADataTemplate();
    }

    public static HearingJPADataTemplate aNewHearingJPADataTemplate(final boolean sysoutPrint) {
        return new HearingJPADataTemplate(sysoutPrint);
    }

    public uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing getHearing() {
        return hearing;
    }
}
