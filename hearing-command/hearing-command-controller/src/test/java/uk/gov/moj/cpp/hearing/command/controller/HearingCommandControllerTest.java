package uk.gov.moj.cpp.hearing.command.controller;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class HearingCommandControllerTest {

	@Mock
	private Sender sender;

	@Mock
	private JsonEnvelope command;

	@InjectMocks
	private HearingCommandController hearingCommandController;

	@Test
	public void shouldListHearing() throws Exception {
		hearingCommandController.listHearing(command);
		verify(sender, times(1)).send(command);
	}
	
	@Test
	public void shouldVacateHearing() throws Exception {
		hearingCommandController.vacateHearing(command);
		verify(sender, times(1)).send(command);
	}

}