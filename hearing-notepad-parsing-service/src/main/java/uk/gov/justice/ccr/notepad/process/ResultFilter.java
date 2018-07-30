package uk.gov.justice.ccr.notepad.process;

import java.time.LocalDate;
import java.util.concurrent.ExecutionException;

@FunctionalInterface
interface ResultFilter<Out, In> {
    Out run(final In in, final LocalDate orderedDate) throws ExecutionException;
}
