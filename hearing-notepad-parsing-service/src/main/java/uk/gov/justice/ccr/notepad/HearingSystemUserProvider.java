package uk.gov.justice.ccr.notepad;

import uk.gov.justice.services.core.dispatcher.SystemUserProvider;

import java.util.Optional;
import java.util.UUID;

import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;

@ApplicationScoped
@Alternative
@Priority(2)
public class HearingSystemUserProvider implements SystemUserProvider {
    @Override
    public Optional<UUID> getContextSystemUserId() {
        return Optional.of(UUID.fromString("8959b8b5-92bd-4ada-96f4-7ac9d482671a"));
    }
}
