package uk.gov.moj.cpp.hearing.query.api;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.kie.api.KieServices;

/**
 * Guards the query-api access-control rulebase: asserts the kbase compiled at least one rule, so a
 * silent zero-rule load (BC-20, e.g. Drools 7→10 packaging drift) fails loudly rather than making the
 * deny-tests in {@link HearingQueryApiAccessControlTest} pass vacuously.
 */
public class AccessControlRuleCountTest {

    @Test
    public void queryApiKieBaseShouldCompileAtLeastOneRule() {
        final long ruleCount = KieServices.get().getKieClasspathContainer()
                .getKieBase("QUERY_API")
                .getKiePackages().stream()
                .mapToLong(kiePackage -> kiePackage.getRules().size())
                .sum();

        assertTrue(ruleCount > 0,
                "QUERY_API kbase compiled 0 rules — access-control deny-tests would pass vacuously");
    }
}
