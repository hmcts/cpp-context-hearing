package uk.gov.moj.cpp.hearing.persist.entity.ex;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static uk.gov.moj.cpp.hearing.utils.TestUtils.hasParameterizedConstructor;

import java.util.UUID;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.code.beanmatchers.BeanMatchers;
import com.google.code.beanmatchers.ValueGenerator;

public class HearingSnapshotKeyTest {
    
    @BeforeClass
    public static void registerValueGenerator() {
        BeanMatchers.registerValueGenerator(new ValueGenerator<UUID>() {
            public UUID generate() {
                return UUID.randomUUID();
            }
        }, UUID.class);
    }
    
    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void shouldHaveANoArgsConstructor() {
        assertThat(HearingSnapshotKey.class, hasValidBeanConstructor());
    }

    @Test
    public void shouldHaveAParameterizedConstructor() {
        assertTrue(hasParameterizedConstructor(HearingSnapshotKey.class, UUID.class, UUID.class));
    }
    
    @Test
    public void shouldHaveTheEqualsMethod() {
        assertThat(HearingSnapshotKey.class, hasValidBeanEquals());
    }
    
    @Test
    public void shouldHaveTheHashCodeMethod() {
        assertThat(HearingSnapshotKey.class, hasValidBeanHashCode());
    }
}
