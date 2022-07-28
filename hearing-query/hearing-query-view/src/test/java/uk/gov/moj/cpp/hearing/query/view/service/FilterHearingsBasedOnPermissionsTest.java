package uk.gov.moj.cpp.hearing.query.view.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class FilterHearingsBasedOnPermissionsTest {

    @Mock
    private Hearing hearing1;

    @Mock
    private Hearing hearing2;

    @Mock
    private Hearing hearing3;

    @Mock
    private ProsecutionCase prosecutionCase1;

    @Mock
    private ProsecutionCase prosecutionCase2;

    @Mock
    private ProsecutionCase prosecutionCase3;

    @Mock
    private HearingSnapshotKey hearingSnapshotKey1;

    @Mock
    private HearingSnapshotKey hearingSnapshotKey2;

    @Mock
    private HearingSnapshotKey hearingSnapshotKey3;

    @InjectMocks
    FilterHearingsBasedOnPermissions filterHearingsBasedOnPermissions;


    @Test
    public void shouldReturnNoFilteredHearingsWhenRoleIsDDJAndHasNoHearings() {

        final List<Hearing> hearings = new ArrayList();
        final List<UUID> accessableCaseList = new ArrayList();
        final List<Hearing> filterHearings = filterHearingsBasedOnPermissions.filterCaseHearings(hearings, accessableCaseList);
        assertThat(filterHearings.size(), is(0));
    }

    @Test
    public void shouldReturnNoFilteredHearingsContainingCasesAndApplicationsWhenRoleIsDDJAndHasNoHearings() {

        final List<Hearing> hearings = new ArrayList();
        final List<UUID> accessableCaseList = new ArrayList();
        final List<Hearing> filterHearings = filterHearingsBasedOnPermissions.filterHearings(hearings, accessableCaseList);
        assertThat(filterHearings.size(), is(0));
    }


    //H1 --> C1, C2 , C3
    //H2 --> C1, C3
    //H3 --> C2

    //U1 --> C1, C3

    @Test
    public void shouldReturnFilteredHearingsWhenRoleIsDDJAndHasPermissableCases() throws IOException {
        final List<Hearing> hearings = new ArrayList();
        hearings.add(hearing1);
        hearings.add(hearing2);
        hearings.add(hearing3);

        final List<UUID> accessableCaseList = new ArrayList();
        final UUID caseId1 = UUID.randomUUID();
        final UUID caseId2 = UUID.randomUUID();
        final UUID caseId3 = UUID.randomUUID();
        accessableCaseList.add(caseId1);
        accessableCaseList.add(caseId3);

        final Set<ProsecutionCase> caseSetHearing1 = new HashSet();
        caseSetHearing1.add(prosecutionCase1);
        caseSetHearing1.add(prosecutionCase2);
        caseSetHearing1.add(prosecutionCase3);

        final Set<ProsecutionCase> caseSetHearing2 = new HashSet();
        caseSetHearing2.add(prosecutionCase1);
        caseSetHearing2.add(prosecutionCase2);

        final Set<ProsecutionCase> caseSetHearing3 = new HashSet();
        caseSetHearing2.add(prosecutionCase2);

        when(prosecutionCase1.getId()).thenReturn(hearingSnapshotKey1);
        when(prosecutionCase2.getId()).thenReturn(hearingSnapshotKey2);
        when(prosecutionCase3.getId()).thenReturn(hearingSnapshotKey3);

        when(hearingSnapshotKey1.getId()).thenReturn(caseId1);
        when(hearingSnapshotKey2.getId()).thenReturn(caseId2);
        when(hearingSnapshotKey3.getId()).thenReturn(caseId3);

        when(hearing1.getProsecutionCases()).thenReturn(caseSetHearing1);
        when(hearing2.getProsecutionCases()).thenReturn(caseSetHearing2);
        when(hearing3.getProsecutionCases()).thenReturn(caseSetHearing3);

        final List<Hearing> filterHearings =
                filterHearingsBasedOnPermissions.filterCaseHearings(hearings, accessableCaseList);

        assertThat(filterHearings.size(), is(2));
    }

    @Test
    public void shouldReturnFilteredHearingsWhenRoleIsDDJAndHasNoPermissableCases() {
        final List<Hearing> hearings = new ArrayList();
        hearings.add(hearing1);
        hearings.add(hearing2);
        hearings.add(hearing3);

        final List<UUID> accessableCaseList = new ArrayList();
        final UUID caseId1 = UUID.randomUUID();
        final UUID caseId2 = UUID.randomUUID();
        final UUID caseId3 = UUID.randomUUID();

        final Set<ProsecutionCase> caseSetHearing1 = new HashSet();
        caseSetHearing1.add(prosecutionCase1);
        caseSetHearing1.add(prosecutionCase2);
        caseSetHearing1.add(prosecutionCase3);

        final Set<ProsecutionCase> caseSetHearing2 = new HashSet();
        caseSetHearing2.add(prosecutionCase1);
        caseSetHearing2.add(prosecutionCase2);

        final Set<ProsecutionCase> caseSetHearing3 = new HashSet();
        caseSetHearing2.add(prosecutionCase2);

        when(prosecutionCase1.getId()).thenReturn(hearingSnapshotKey1);
        when(prosecutionCase2.getId()).thenReturn(hearingSnapshotKey2);
        when(prosecutionCase3.getId()).thenReturn(hearingSnapshotKey3);

        when(hearingSnapshotKey1.getId()).thenReturn(caseId1);
        when(hearingSnapshotKey2.getId()).thenReturn(caseId2);
        when(hearingSnapshotKey3.getId()).thenReturn(caseId3);

        when(hearing1.getProsecutionCases()).thenReturn(caseSetHearing1);
        when(hearing2.getProsecutionCases()).thenReturn(caseSetHearing2);
        when(hearing3.getProsecutionCases()).thenReturn(caseSetHearing3);

        final List<Hearing> filterHearings =
                filterHearingsBasedOnPermissions.filterCaseHearings(hearings, accessableCaseList);

        assertThat(filterHearings.size(), is(0));
    }
}