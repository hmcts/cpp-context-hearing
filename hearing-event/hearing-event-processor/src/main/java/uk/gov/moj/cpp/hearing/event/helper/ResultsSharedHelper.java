package uk.gov.moj.cpp.hearing.event.helper;


import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;

import uk.gov.justice.core.courts.Category;
import uk.gov.justice.core.courts.JudicialResult;
import uk.gov.justice.core.courts.Offence;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsShared;


import java.util.List;

import org.apache.commons.collections.CollectionUtils;

public  class ResultsSharedHelper {

    public static final String CATEGORY_FINAL = "FINAL";


    /**
     *  If any of the JudicialResults have a Category of Final , then set the corresponding Offence's isDisposed Flag to true.
     *
     * @param resultsShared
     * @return void
     *
     */
    public  void setIsDisposedFlagOnOffence(final ResultsShared resultsShared) {
        final List<Offence> offencesList  = resultsShared.getHearing()
                .getProsecutionCases()
                .stream()
                .flatMap( prosecutionCase -> prosecutionCase.getDefendants().stream())
                .flatMap( defendant -> defendant.getOffences().stream())
                .collect(toList());


        if(isNotEmpty(offencesList) ){
            for(final Offence offence : offencesList){

                final List<JudicialResult> judicialResults = offence.getJudicialResults();

                if(nonNull(judicialResults) && isCategoryTypeFinalPresentInJudicialResult(judicialResults)){
                    offence.setIsDisposed(Boolean.TRUE);
                }else{
                    offence.setIsDisposed(Boolean.FALSE);
                }
            }
        }
    }

    private  boolean isCategoryTypeFinalPresentInJudicialResult(final List<JudicialResult> judicialResultsList){

        if(CollectionUtils.isNotEmpty(judicialResultsList)){
            return judicialResultsList
                        .stream()
                        .filter( judicialResult -> nonNull(judicialResult.getCategory()))
                        .anyMatch(judicialResult -> judicialResult.getCategory().equals(Category.FINAL));
        }
        return false;

    }

}
