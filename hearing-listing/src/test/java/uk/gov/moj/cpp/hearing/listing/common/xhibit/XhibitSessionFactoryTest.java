package uk.gov.moj.cpp.hearing.listing.common.xhibit;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URL;

import com.github.sardine.Sardine;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class XhibitSessionFactoryTest {
    @Mock
    private XhibitSessionConnectionParameters xhibitSessionConnectionParameters;

    @Mock
    private SardineClientFactory sardineClientFactory;

    @Mock
    private UrlFactory urlFactory;

    @InjectMocks
    private XhibitSessionFactory xhibitSessionFactory;

    @Test
    public void shouldCreateAnXhibitSession() throws Exception {

        final String outboundUrlString = "outboundUrlString";
        final String user = "user";
        final String password = "password";
        final URL outboundUrl = new URL("http://outbound.com");

        final Sardine client = mock(Sardine.class);

        when(xhibitSessionConnectionParameters.getOutboundUrl()).thenReturn(outboundUrlString);
        when(urlFactory.create(outboundUrlString)).thenReturn(outboundUrl);
        when(xhibitSessionConnectionParameters.getUser()).thenReturn(user);
        when(xhibitSessionConnectionParameters.getPassword()).thenReturn(password);
        when(sardineClientFactory.createSardineClient(user, password)).thenReturn(client);

        final XhibitSession xhibitSession = xhibitSessionFactory.createSession();

        assertThat(xhibitSession.getOutbound(), is(outboundUrl));
        assertThat(xhibitSession.getClient(), is(client));

        verify(client).enablePreemptiveAuthentication(outboundUrl);
    }
}