package uk.gov.moj.cpp.hearing.mapping;

import uk.gov.moj.cpp.hearing.persist.entity.ha.ApprovalRequested;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ApprovalRequestedJPAMapper {

    @SuppressWarnings("squid:S1186")
    public ApprovalRequestedJPAMapper() {
    }

    public ApprovalRequested toJPA(final uk.gov.justice.core.courts.ApprovalRequest pojo) {
        if (null == pojo) {
            return null;
        }
        return new ApprovalRequested(UUID.randomUUID(), pojo.getHearingId(), pojo.getUserId(), pojo.getRequestApprovalTime(), pojo.getApprovalType());

    }

    public uk.gov.justice.core.courts.ApprovalRequest fromJPA(final ApprovalRequested entity) {
        if (null == entity) {
            return null;
        }
        return uk.gov.justice.core.courts.ApprovalRequest.approvalRequest()
                .withHearingId(entity.getHearingId())
                .withUserId(entity.getUserId())
                .withRequestApprovalTime(entity.getRequestApprovalTime())
                .withApprovalType(entity.getApprovalType())
                .build();
    }

    @SuppressWarnings("squid:S1612")
    public Set<ApprovalRequested> toJPA(List<uk.gov.justice.core.courts.ApprovalRequest> pojos) {
        if (null == pojos) {
            return new HashSet<>();
        }
        return pojos.stream().map(pojo -> toJPA(pojo)).collect(Collectors.toSet());
    }

    public List<uk.gov.justice.core.courts.ApprovalRequest> fromJPA(Set<ApprovalRequested> entities) {
        if (null == entities) {
            return new ArrayList<>();
        }
        return entities.stream().map(this::fromJPA).sorted(Comparator.comparing(uk.gov.justice.core.courts.ApprovalRequest::getHearingId)).collect(Collectors.toList());
    }
}