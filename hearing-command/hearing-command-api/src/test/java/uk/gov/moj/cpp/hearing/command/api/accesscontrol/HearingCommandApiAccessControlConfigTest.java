package uk.gov.moj.cpp.hearing.command.api.accesscontrol;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

/**
 * Guards against the command API's RAML and its Drools access-control rules drifting apart.
 *
 * <p>The RAML declares the actions the command API accepts; the DRL grants access to them.
 * Nothing else checks that every RAML action has a matching rule. An action declared in the RAML
 * with no rule is refused at runtime for every caller - including the system user - and every
 * unit test still passes. That is exactly how a previously reviewed endpoint in a sibling repo
 * went unreachable: declared in RAML, no access-control rule, 403 for everyone.
 */
public class HearingCommandApiAccessControlConfigTest {

    private static final String PATH_TO_RAML = "src/raml/hearing-command-api.raml";
    private static final String PATH_TO_DRL =
            "src/main/resources/uk/gov/moj/cpp/hearing/command/api/accesscontrol/hearing-command-api.drl";

    private static final String CONTEXT_PREFIX = "hearing.";

    /**
     * Actions with no access-control rule, allowlisted with the reason. Every entry here MUST be
     * a deliberate, documented exception - not a place to quietly grow the list.
     *
     * <p>{@code hearing.replicate-shared-results} is a SUSPECTED PRE-EXISTING DEFECT, not an
     * intentional exemption. Evidence: it is declared in the RAML (see
     * {@code src/raml/hearing-command-api.raml}, {@code name: hearing.replicate-shared-results}),
     * it is reachable via {@code @Handles("hearing.replicate-shared-results")} at
     * {@code HearingCommandApi.java:352}, and no {@code .drl} file anywhere in this repository
     * mentions it - there are only two access-control DRLs in the whole repo (command-api and
     * query-api) and neither carries a rule for it. There is also no catch-all rule in the
     * command-api DRL: all ~71 rules there are keyed on one specific {@code hearing.*} action
     * name, so an action with no matching rule is refused for every caller. This is unrelated to
     * the reserve-a-slot feature this test was written for and is deliberately NOT fixed here -
     * it is allowlisted so this guard can go in without silencing a pre-existing gap, and needs
     * raising with the owners of that action.
     */
    private static final Set<String> ALLOWLISTED_ACTIONS_WITH_NO_RULE = Set.of(
            "hearing.replicate-shared-results"
    );

    // Matches a RAML "name: <value>" mapping entry, e.g. "            name: hearing.amend"
    private static final Pattern RAML_NAME_PATTERN = Pattern.compile("name:\\s*(\\S+)");

    // Matches a Drools access-control rule condition, e.g. Action(name == "hearing.amend")
    private static final Pattern DRL_ACTION_PATTERN = Pattern.compile("Action\\(name == \"([^\"]+)\"\\)");

    /**
     * Every action the command API declares must have an access-control rule, unless it is
     * explicitly allowlisted above with a reason. Without one the framework refuses the request
     * for every caller, at runtime, with every unit test green.
     *
     * <p>Deliberately one-directional. The DRL legitimately carries rules with no action in this
     * RAML - e.g. {@code hearing.delete-attendee}, {@code hearing.save-application-response},
     * {@code hearing.update-nows-material-status} - so asserting set equality would fail on
     * arrival for a pre-existing reason, and a test that fails on arrival gets disabled rather
     * than fixed.
     */
    @Test
    public void everyRamlActionHasAnAccessControlRule() throws Exception {
        final Set<String> ramlActions = ramlActionNames();
        final Set<String> ruleActions = drlActionNames();

        assertThat("the RAML parse found no actions - the extraction is broken, not the config",
                ramlActions, is(not(empty())));

        final Set<String> missing = new TreeSet<>(ramlActions);
        missing.removeAll(ruleActions);
        missing.removeAll(ALLOWLISTED_ACTIONS_WITH_NO_RULE);

        assertThat("command API actions with no access-control rule - every caller gets refused "
                + "at runtime: " + missing, missing, is(empty()));
    }

    /**
     * Guards against the allowlist above going stale - e.g. once
     * {@code hearing.replicate-shared-results} gets a rule, it must be removed from the
     * allowlist, or this test would keep passing while silently hiding the fact that the
     * allowlist no longer matches reality.
     */
    @Test
    public void allowlistedActionsAreStillWithoutARuleAndStillDeclaredInRaml() throws Exception {
        final Set<String> ramlActions = ramlActionNames();
        final Set<String> ruleActions = drlActionNames();

        for (final String allowlisted : ALLOWLISTED_ACTIONS_WITH_NO_RULE) {
            assertThat("allowlisted action '" + allowlisted + "' is no longer declared in the "
                    + "RAML - remove it from the allowlist", ramlActions, hasItem(allowlisted));
            assertThat("allowlisted action '" + allowlisted + "' now has an access-control rule - "
                    + "remove it from the allowlist, it is no longer a gap", ruleActions,
                    is(not(hasItem(allowlisted))));
        }
    }

    private Set<String> ramlActionNames() throws Exception {
        final Set<String> names = new TreeSet<>();
        for (final String line : readLines(PATH_TO_RAML)) {
            final Matcher matcher = RAML_NAME_PATTERN.matcher(line);
            if (matcher.find()) {
                final String name = matcher.group(1);
                if (name.startsWith(CONTEXT_PREFIX)) {
                    names.add(name);
                }
            }
        }
        return names;
    }

    private Set<String> drlActionNames() throws Exception {
        final Set<String> names = new TreeSet<>();
        final String content = withoutComments(readFile(PATH_TO_DRL));
        final Matcher matcher = DRL_ACTION_PATTERN.matcher(content);
        while (matcher.find()) {
            names.add(matcher.group(1));
        }
        return names;
    }

    /**
     * Drools honours both {@code //} line comments and {@code /* ... *}{@code /} block comments -
     * a rule hidden behind either is disabled at runtime exactly as if it were deleted, so it
     * must not count as an access-control rule here. Without this, commenting out a rule to
     * "temporarily" disable it would leave this guard green while the action it covered is
     * refused for everyone. Strip block comments first (they can span, and hide, whole {@code //}
     * lines), then line comments, then match what's left.
     */
    private String withoutComments(final String drlContent) {
        final String withoutBlockComments = drlContent.replaceAll("(?s)/\\*.*?\\*/", "");
        return withoutBlockComments.replaceAll("//[^\\n]*", "");
    }

    private List<String> readLines(final String path) throws Exception {
        return Files.readAllLines(new File(path).toPath(), StandardCharsets.UTF_8);
    }

    private String readFile(final String path) throws Exception {
        return new String(Files.readAllBytes(new File(path).toPath()), StandardCharsets.UTF_8);
    }
}
