package uk.gov.moj.cpp.hearing.mapping;

import uk.gov.justice.core.courts.Address;
import uk.gov.justice.core.courts.ContactNumber;
import uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCaseIdentifier;

import javax.enterprise.context.ApplicationScoped;

import static java.util.Optional.ofNullable;
import static uk.gov.justice.core.courts.ProsecutionCaseIdentifier.prosecutionCaseIdentifier;

@ApplicationScoped
public class ProsecutionCaseIdentifierJPAMapper {

    public ProsecutionCaseIdentifier toJPA(final uk.gov.justice.core.courts.ProsecutionCaseIdentifier pojo) {
        if (null == pojo) {
            return null;
        }
        final ProsecutionCaseIdentifier prosecutionCaseIdentifier = new ProsecutionCaseIdentifier();
        prosecutionCaseIdentifier.setProsecutionAuthorityId(pojo.getProsecutionAuthorityId());
        prosecutionCaseIdentifier.setProsecutionAuthorityCode(pojo.getProsecutionAuthorityCode());
        prosecutionCaseIdentifier.setProsecutionAuthorityReference(pojo.getProsecutionAuthorityReference());
        prosecutionCaseIdentifier.setCaseURN(pojo.getCaseURN());
        if (pojo.getMajorCreditorCode() == null) {
            ofNullable(pojo.getAddress()).ifPresent(address -> {
                prosecutionCaseIdentifier.setProsecutorAuthorityAddress1(address.getAddress1());
                prosecutionCaseIdentifier.setProsecutorAuthorityAddress2(address.getAddress2());
                prosecutionCaseIdentifier.setProsecutorAuthorityAddress3(address.getAddress3());
                prosecutionCaseIdentifier.setProsecutorAuthorityAddress4(address.getAddress4());
                prosecutionCaseIdentifier.setProsecutorAuthorityAddress5(address.getAddress5());
                prosecutionCaseIdentifier.setProsecutorAuthorityPostCode(address.getPostcode());
            });
            ofNullable(pojo.getContact()).ifPresent(contact -> prosecutionCaseIdentifier.setProsecutorAuthorityEmailAddress(contact.getPrimaryEmail()));
            prosecutionCaseIdentifier.setProsecutorAuthorityName(pojo.getProsecutionAuthorityName());
            prosecutionCaseIdentifier.setProsecutorCategory(pojo.getProsecutorCategory());
        }

        return prosecutionCaseIdentifier;
    }

    public uk.gov.justice.core.courts.ProsecutionCaseIdentifier fromJPA(final ProsecutionCaseIdentifier entity) {
        if (null == entity) {
            return null;
        }
        final Address address = Address.address()
                .withAddress1(entity.getProsecutorAuthorityAddress1())
                .withAddress2(entity.getProsecutorAuthorityAddress2())
                .withAddress3(entity.getProsecutorAuthorityAddress3())
                .withAddress4(entity.getProsecutorAuthorityAddress4())
                .withAddress5(entity.getProsecutorAuthorityAddress5())
                .withPostcode(entity.getProsecutorAuthorityPostCode())
                .build();

        final uk.gov.justice.core.courts.ProsecutionCaseIdentifier.Builder builder = prosecutionCaseIdentifier();

        if(!address.equals(Address.address().build())) {
            builder.withAddress(address);
        }

        return builder
                .withCaseURN(entity.getCaseURN())
                .withProsecutionAuthorityCode(entity.getProsecutionAuthorityCode())
                .withProsecutionAuthorityId(entity.getProsecutionAuthorityId())
                .withProsecutionAuthorityReference(entity.getProsecutionAuthorityReference())
                .withProsecutionAuthorityName(entity.getProsecutorAuthorityName())
                .withProsecutorCategory(entity.getProsecutorCategory())
                .withContact(ofNullable(entity.getProsecutorAuthorityEmailAddress())
                        .map(email -> ContactNumber.contactNumber().withPrimaryEmail(email).build())
                        .orElse(null))
                .build();
    }
}