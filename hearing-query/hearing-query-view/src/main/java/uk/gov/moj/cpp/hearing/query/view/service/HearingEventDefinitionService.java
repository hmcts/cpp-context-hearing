package uk.gov.moj.cpp.hearing.query.view.service;

import uk.gov.moj.cpp.hearing.persist.HearingDefinitionsRepository;
import uk.gov.moj.cpp.hearing.query.view.convertor.HearingEventDefinitionEntityToHearingEventDefinitionView;
import uk.gov.moj.cpp.hearing.query.view.response.HearingEventDefinitionView;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.transaction.Transactional;

public class HearingEventDefinitionService {
    @Inject
    private HearingDefinitionsRepository hearingDefinitionsRepository;

    @Transactional
    public List<HearingEventDefinitionView> getHearingEventDefinitions() {
        return new ArrayList<>(hearingDefinitionsRepository.findAll()
                .stream().sorted()
                .map(HearingEventDefinitionEntityToHearingEventDefinitionView::convert)
                .collect(Collectors.toList()));
    }

}
