package uk.gov.moj.cpp.hearing.query.view.service;

import static java.lang.String.format;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.JudicialRole;
import uk.gov.justice.core.courts.JudicialRoleType;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.test.utils.core.converter.JsonObjectToObjectConverterFactory;
import uk.gov.moj.cpp.listing.common.xhibit.CommonXhibitReferenceDataService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class JudgeNameMapperTest {

    private static final String TITLE_PREFIX = "titlePrefix";
    private static final String TITLE_JUDICIAL_PREFIX = "titleJudicialPrefix";
    private static final String TITLE_SUFFIX = "titleSuffix";
    private static final String FORENAMES = "forenames";
    private static final String SURNAME = "surname";
    private static final String REQUESTED_NAME = "requestedName";
    private static final UUID JUDICIAL_ROLE_ID = randomUUID();
    private static final String CIRCUIT = "CIRCUIT";

    private JsonObjectToObjectConverter jsonObjectToObjectConverter = new JsonObjectToObjectConverterFactory().createJsonObjectToObjectConverter();

    @Mock(answer = RETURNS_DEEP_STUBS)
    private CommonXhibitReferenceDataService commonXhibitReferenceDataService;

    @Mock
    private Hearing hearing;

    @InjectMocks
    private JudgeNameMapper judgeNameMapper;


    @Test
    public void shouldReturnRequestedNameAsJudgeName() {
        when(hearing.getJudiciary()).thenReturn(getJudicialRoles());
        when(commonXhibitReferenceDataService.getJudiciary(JUDICIAL_ROLE_ID)).thenReturn(createJudiciaryWithRequestedName());
        final String judgeName = judgeNameMapper.getJudgeName(hearing);
        assertThat(judgeName, is(REQUESTED_NAME));
        verify(commonXhibitReferenceDataService).getJudiciary(JUDICIAL_ROLE_ID);
        verify(hearing).getJudiciary();
    }

    @Test
    public void shouldNotReturnRequestedNameAsJudgeName() {
        when(hearing.getJudiciary()).thenReturn(getJudicialRoles());
        when(commonXhibitReferenceDataService.getJudiciary(JUDICIAL_ROLE_ID)).thenReturn(createJudiciaryWithoutRequestedName());
        final String judgeName = judgeNameMapper.getJudgeName(hearing);
        final String formattedName = format("%s %s %s %s", TITLE_JUDICIAL_PREFIX, FORENAMES, SURNAME, TITLE_SUFFIX).trim();

        assertThat(judgeName, is(formattedName));
        verify(commonXhibitReferenceDataService).getJudiciary(JUDICIAL_ROLE_ID);
        verify(hearing).getJudiciary();
    }

    private JsonObject createJudiciaryWithRequestedName() {
        final JsonObjectBuilder judiciaryBuilder = Json.createObjectBuilder();
        judiciaryBuilder.add(REQUESTED_NAME, REQUESTED_NAME);
        judiciaryBuilder.add(SURNAME, SURNAME);
        judiciaryBuilder.add(FORENAMES, FORENAMES);
        judiciaryBuilder.add(TITLE_SUFFIX, TITLE_SUFFIX);
        judiciaryBuilder.add(TITLE_JUDICIAL_PREFIX, TITLE_JUDICIAL_PREFIX);
        judiciaryBuilder.add(TITLE_PREFIX, TITLE_PREFIX);
        return judiciaryBuilder.build();
    }

    private JsonObject createJudiciaryWithoutRequestedName() {
        final JsonObjectBuilder judiciaryBuilder = Json.createObjectBuilder();
        judiciaryBuilder.add(SURNAME, SURNAME);
        judiciaryBuilder.add(FORENAMES, FORENAMES);
        judiciaryBuilder.add(TITLE_SUFFIX, TITLE_SUFFIX);
        judiciaryBuilder.add(TITLE_JUDICIAL_PREFIX, TITLE_JUDICIAL_PREFIX);
        judiciaryBuilder.add(TITLE_PREFIX, TITLE_PREFIX);
        return judiciaryBuilder.build();
    }

    private List<JudicialRole> getJudicialRoles() {
        final JudicialRole judicialRole = getJudicialRole();

        final List<JudicialRole> roles = new ArrayList();
        roles.add(judicialRole);
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