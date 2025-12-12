package uk.gov.moj.cpp.hearing.event.delegates.helper;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Objects.nonNull;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.justice.core.courts.ResultLine.resultLine;

import uk.gov.justice.core.courts.ResultLine;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ResultLineHelperTest {

    private ResultLineHelper target = new ResultLineHelper();
    private ResultLine parent;
    private ResultLine child;
    private ResultLine grandChild;

    @BeforeEach
    public void setUp() {
        parent = buildResultLine(null);
        child = buildResultLine(parent.getResultLineId());
        grandChild = buildResultLine(child.getResultLineId());
    }

    @Test
    public void shouldGetRootResultLineFromGrandChild() {
        final List<ResultLine> resultLineList = asList(parent, child, grandChild);
        doAssertion(resultLineList, parent, grandChild);
    }

    @Test
    public void shouldGetRootResultLineFromChild() {
        final List<ResultLine> resultLineList = asList(parent, child);
        doAssertion(resultLineList, parent, child);
    }

    @Test
    public void shouldGetRootResultLineFromParent() {
        final List<ResultLine> resultLineList = asList(parent);
        doAssertion(resultLineList, parent, parent);
    }

    private ResultLine buildResultLine(UUID parentResultId) {
        final ResultLine.Builder resultLineBuilder = resultLine()
                .withResultLineId(randomUUID())
                .withResultDefinitionId(randomUUID());

        if (nonNull(parentResultId)) {
            resultLineBuilder.withParentResultLineIds(singletonList(parentResultId));
        }

        return resultLineBuilder.build();
    }

    private void doAssertion(final List<ResultLine> resultLineList, final ResultLine parent, final ResultLine from) {
        final ResultLine rootResultLine = target.getResultLine(resultLineList, from);
        assertThat(rootResultLine, is(notNullValue()));
        assertThat(rootResultLine.getResultLineId(), is(parent.getResultLineId()));
        assertThat(rootResultLine.getResultDefinitionId(), is(parent.getResultDefinitionId()));
    }
}