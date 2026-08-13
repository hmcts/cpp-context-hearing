package uk.gov.moj.cpp.hearing.listing.common.xhibit;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import com.github.sardine.Sardine;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
public class SardineClientFactoryTest {

    @InjectMocks
    private SardineClientFactory sardineClientFactory;

    @Test
    public void shouldCreateTheSardineClient() throws Exception {
        assertThat(sardineClientFactory.createSardineClient("username", "password"), is(instanceOf(Sardine.class)));
    }
}
