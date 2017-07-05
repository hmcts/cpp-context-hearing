package uk.gov.justice.ccr.notepad.process;

import java.math.BigDecimal;
import java.util.Locale;

import org.apache.commons.validator.routines.BigDecimalValidator;
import org.apache.commons.validator.routines.CurrencyValidator;

public class CurrencyMatcher {

    public boolean match(final String value) {
        boolean result = false;
        BigDecimalValidator validator = CurrencyValidator.getInstance();
        BigDecimal amount = validator.validate(value, Locale.UK);
        if (amount != null) {
            result = true;
        }
        return result;
    }
}
