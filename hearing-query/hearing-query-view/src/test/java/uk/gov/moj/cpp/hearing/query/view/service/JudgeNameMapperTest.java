package uk.gov.moj.cpp.hearing.query.view.service;

import static java.lang.String.format;
import static java.util.UUID.randomUUID;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.JudicialRole;
import uk.gov.justice.core.courts.JudicialRoleType;
import uk.gov.moj.cpp.listing.common.xhibit.CommonXhibitReferenceDataService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.json.JsonObject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class JudgeNameMapperTest {

    private static final String TITLE_PREFIX = "titlePrefix";
    private static final String TITLE_JUDICIAL_PREFIX = "titleJudicialPrefix";
    private static final String TITLE_SUFFIX = "titleSuffix";
    private static final String FORENAMES = "forenames";
    private static final String SURNAME = "surname";
    private static final String REQUESTED_NAME = "requestedName";
    private static final UUID JUDICIAL_ROLE_ID = randomUUID();
    private static final String CIRCUIT = "CIRCUIT";

    @Mock(answer = RETURNS_DEEP_STUBS)
    private CommonXhibitReferenceDataService commonXhibitReferenceDataService;

    @Mock
    private Hearing hearing;

    @InjectMocks
    private JudgeNameMapper judgeNameMapper;


    @Test
    public void shouldReturnRequestedNameAsJudgeName() {
        final JsonObject judiciary = mockJudiciaryWithRequestedName();
        when(hearing.getJudiciary()).thenReturn(getJudicialRoles());
        when(commonXhibitReferenceDataService.getJudiciary(JUDICIAL_ROLE_ID)).thenReturn(judiciary);
        final String judgeName = judgeNameMapper.getJudgeName(hearing);
        assertThat(judgeName, is(REQUESTED_NAME));
        verify(commonXhibitReferenceDataService).getJudiciary(JUDICIAL_ROLE_ID);
        verify(hearing).getJudiciary();
    }

    @Test
    public void shouldNotReturnRequestedNameAsJudgeName() {
        final JsonObject judiciary = mockJudiciaryWithoutRequestedName();
        when(hearing.getJudiciary()).thenReturn(getJudicialRoles());
        when(commonXhibitReferenceDataService.getJudiciary(JUDICIAL_ROLE_ID)).thenReturn(judiciary);
        final String judgeName = judgeNameMapper.getJudgeName(hearing);
        final String formattedName = format("%s %s %s %s", TITLE_JUDICIAL_PREFIX, FORENAMES, SURNAME, TITLE_SUFFIX).trim();

        assertThat(judgeName, is(formattedName));
        verify(commonXhibitReferenceDataService).getJudiciary(JUDICIAL_ROLE_ID);
        verify(hearing).getJudiciary();
    }

    private JsonObject mockJudiciaryWithRequestedName() {
        final JsonObject judiciary = mock(JsonObject.class);
        when(judiciary.getString(eq(REQUESTED_NAME), eq(EMPTY))).thenReturn(REQUESTED_NAME);
        return judiciary;
    }

    private JsonObject mockJudiciaryWithoutRequestedName() {
        final JsonObject judiciary = mock(JsonObject.class);
        when(judiciary.getString(eq(REQUESTED_NAME), eq(EMPTY))).thenReturn(EMPTY);
        when(judiciary.getString(eq(TITLE_PREFIX), eq(EMPTY))).thenReturn(TITLE_PREFIX);
        when(judiciary.getString(eq(TITLE_JUDICIAL_PREFIX), eq(TITLE_PREFIX))).thenReturn(TITLE_JUDICIAL_PREFIX);
        when(judiciary.getString(FORENAMES)).thenReturn(FORENAMES);
        when(judiciary.getString(SURNAME)).thenReturn(SURNAME);
        when(judiciary.getString(eq(TITLE_SUFFIX), eq(EMPTY))).thenReturn(TITLE_SUFFIX);
        return judiciary;
    }

    private List<JudicialRole> getJudicialRoles() {
        final List<JudicialRole> roles = new ArrayList();
        roles.add(getJudicialRole());
        return roles;
    }

    private JudicialRole getJudicialRole() {
        final JudicialRoleType judicialRoleType = JudicialRoleType.judicialRoleType()
                .withJudicialRoleTypeId(randomUUID())
                .withJudiciaryType(CIRCUIT)
                .build();
        return JudicialRole.judicialRole()
                .withJudicialId(JUDICIAL_ROLE_ID)
                .withFirstName(FORENAMES)
                .withLastName(SURNAME)
                .withTitle(TITLE_PREFIX)
                .withJudicialRoleType(judicialRoleType)
                .build();
    }
}
