package uk.gov.moj.cpp.hearing.event.nows.service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;

import uk.gov.moj.cpp.hearing.activiti.common.ProcessMapConstant;
import uk.gov.moj.cpp.hearing.activiti.service.ActivitiService;

public class UploadMaterialService {

    @Inject
    private ActivitiService activitiService;

    public void uploadFile(final UUID userId, final UUID hearingId, final UUID materialId, final UUID fileId) {
        final Map<String, Object> processMap = new HashMap<>();
        processMap.put(ProcessMapConstant.USER_ID, userId);
        processMap.put(ProcessMapConstant.FILE_SERVICE_ID, fileId);
        processMap.put(ProcessMapConstant.HEARING_ID, hearingId);
        processMap.put(ProcessMapConstant.MATERIAL_ID, materialId.toString());
        activitiService.startProcess("nowsMaterialUpload", processMap);

    }
}
