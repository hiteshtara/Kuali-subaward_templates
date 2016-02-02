package edu.bu.kuali.kra.subawardReporting.printing.xmlstream;


import edu.bu.kuali.kra.award.awardhierarchy.AwardHierarchyService;
import edu.mit.coeus.utils.xml.bean.subcontractFdpReports.award.AwardHeaderType;
import edu.mit.coeus.utils.xml.bean.subcontractFdpReports.award.AwardType;
import edu.mit.coeus.utils.xml.bean.subcontractFdpReports.award.AwardType.AwardDetails.OtherHeaderDetails;
import edu.mit.coeus.utils.xml.bean.subcontractFdpReports.subcontract.OrganizationType;
import edu.mit.coeus.utils.xml.bean.subcontractFdpReports.subcontract.RolodexDetailsType;
import edu.mit.coeus.utils.xml.bean.subcontractFdpReports.subcontract.SubContractDataDocument.SubContractData;
import edu.mit.coeus.utils.xml.bean.subcontractFdpReports.subcontract.SubContractDataDocument.SubContractData.SubcontractAmountInfo;
import edu.mit.coeus.utils.xml.bean.subcontractFdpReports.subcontract.SubContractDataDocument.SubContractData.SubcontractDetail;
import org.apache.commons.lang.StringUtils;
import org.apache.xmlbeans.XmlObject;
import org.kuali.kra.award.contacts.AwardPerson;
import org.kuali.kra.award.home.Award;
import org.kuali.kra.award.version.service.AwardVersionService;
import org.kuali.kra.bo.KcPerson;
import org.kuali.kra.bo.KraPersistableBusinessObjectBase;
import org.kuali.kra.bo.Organization;
import org.kuali.kra.infrastructure.KraServiceLocator;
import org.kuali.kra.printing.util.PrintingUtils;
import org.kuali.kra.service.KcPersonService;
import org.kuali.kra.service.UnitService;
import org.kuali.kra.subaward.bo.SubAward;
import org.kuali.kra.subaward.bo.SubAwardAmountInfo;
import edu.mit.coeus.utils.xml.bean.subcontractFdpReports.subcontract.PersonDetailsType;
import org.kuali.rice.krad.service.BusinessObjectService;
import org.kuali.rice.krad.util.ObjectUtils;
import org.kuali.rice.location.api.state.State;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SubAwardFDPPrintXmlStream extends org.kuali.kra.subawardReporting.printing.xmlstream.SubAwardFDPPrintXmlStream {

    private AwardVersionService awardVersionService;

    private BusinessObjectService businessObjectService;

    private AwardHierarchyService awardHierarchyService;
    Map<String, Object> reportParameters;

    // BUKC-0103: FDP Template (Cost Reimbursement) Enhancement (Subaward Enhancement 25)
    private String fain;

    // BUKC-0129: KC Subaward: FDP Modification Template Enhancement (ENHC0012935)
    private String modificationType;
    private String fcoi;
    // BUKC-0129: KC Subaward: FDP Modification Template Enhancement  FFATA (ENHC0012935)
    private String ffata;

    // BUKC-0146: Add Subaward Custom Fields - R&D and Cost Sharing (ENHC0013365 and ENHC0013357)
    private String randd;
    private String costshare;

    /**
     * Gets cost share attribute
     */
    public String getCostshare() {
        return costshare;
    }

    public void setCostshare(String costshare) {
        this.costshare = costshare;
    }

    /**
     * Gets the fain attribute.
     *
     * @return Returns the fain.
     */
    public String getFain() {
        return fain;
    }

    /**
     * Sets the fain attribute value.
     *
     * @param fain The fain to set.
     */
    public void setFain(String fain) {
        this.fain = fain;
    }

    /**
     * gets RandD attribute
     */
    public String getRandd() {
        return randd;
    }

    public void setRandd(String randd) {
        this.randd = randd;
    }

    public String getFfata() {
        return ffata;
    }

    public void setFfata(String ffata) {
        this.ffata = ffata;
    }

    @Override
    public Map<String, XmlObject> generateXmlStream(KraPersistableBusinessObjectBase printableBusinessObject,
                                                    Map<String, Object> reportParameters) {
        this.reportParameters = reportParameters;
        // BUKC-0129: KC Subaward: FDP Modification Template Enhancement (ENHC0012935)
        this.modificationType = (String) reportParameters.get("modificationType");
        this.fcoi = (String) reportParameters.get("fcoi");
        this.fain = (String) reportParameters.get("fain");
        // BUKC-0129: KC Subaward: FDP Modification Template Enhancement  FFATA (ENHC0012935)
        this.ffata = (String) reportParameters.get("ffata");
        this.randd = (String) reportParameters.get("randd");
        this.costshare = (String) reportParameters.get("costshare");
        return super.generateXmlStream(printableBusinessObject, reportParameters);
    }

    @Override
    public void setAwardHeader(SubContractData subContractData, SubAward subaward) {
        AwardType awardType= AwardType.Factory.newInstance();
        AwardHeaderType awardHeaderType =AwardHeaderType.Factory.newInstance();
        AwardType.AwardDetails awardDetails= AwardType.AwardDetails.Factory.newInstance();
        OtherHeaderDetails otherDetails = OtherHeaderDetails.Factory.newInstance();
        List<AwardType> awardTypeList = new ArrayList<AwardType>();

        Award award = getAwardVersionService().getWorkingAwardVersion(super.getAwardNumber());

        if (award != null) {
            if (award.getSponsorAwardNumber() != null) {
                awardHeaderType.setSponsorAwardNumber(award.getSponsorAwardNumber());
            }

            if (award.getSponsorName() != null) {
                awardHeaderType.setSponsorDescription(award.getSponsorName());
            }

            if (award.getTitle() != null) {
                awardHeaderType.setTitle(award.getTitle());
            }
            if (award.getCfdaNumber() != null) {
                otherDetails.setCFDANumber(award.getCfdaNumber());
            }
            // BUKC-0129: KC Subaward: FDP Modification Template Enhancement (ENHC0012935) add Prime sponsor details
            if (award.getPrimeSponsor() != null) {
                otherDetails.setPrimeSponsorDescription(award.getPrimeSponsor() != null ? award.getPrimeSponsor().getSponsorName() : null);
            }

            // BUKC-0329: KC Subaward: New FDP templates (ENHC0013043)
            if (award.getNoticeDate() != null) {
                otherDetails.setLastUpdate(getDateTimeService().getCalendar(award.getNoticeDate()));
            }
            // BUKC-0176: Subaward: FDP Agreement - Get total Amount of Federal Award to PTE field from parent award (ENHC0014206)
            Award rootAward = getAwardHierarchyService().loadAwardHierarchy(award.getAwardNumber()).getRoot().getAward();
            if (rootAward.getObligatedTotal() != null) {
                otherDetails.setObligatedAmt(rootAward.getObligatedTotal().bigDecimalValue());
            }
            if (rootAward.getAnticipatedTotal() != null) {
                otherDetails.setAnticipatedAmt(rootAward.getAnticipatedTotal().bigDecimalValue());
            }
        }

        otherDetails.setFAIN(fain);
        awardDetails.setAwardHeader(awardHeaderType);
        awardDetails.setOtherHeaderDetails(otherDetails);
        awardType.setAwardDetails(awardDetails);
        awardTypeList.add(awardType);
        subContractData.setAwardArray((AwardType[])awardTypeList.toArray(new AwardType[0]));
    }

    public AwardVersionService getAwardVersionService() {
        awardVersionService = KraServiceLocator.getService(AwardVersionService.class);
        return awardVersionService;
    }

    public void setAwardVersionService(AwardVersionService awardVersionService) {
        this.awardVersionService = awardVersionService;
    }

    public AwardHierarchyService getAwardHierarchyService() {
        awardHierarchyService = KraServiceLocator.getService(AwardHierarchyService.class);
        return awardHierarchyService;
    }

    public void setAwardHierarchyService(AwardHierarchyService awardHierarchyService) {
        this.awardHierarchyService = awardHierarchyService;
    }

    @Override
    // BUKC-0104: FDP  Modification Enhancement (Subaward Enhancement 13, 38,  43, 45)
    public void setSubcontractDetail(SubContractData subContractData,
                                     SubAward subaward) {
        SubcontractDetail subcontractDetail = SubcontractDetail.Factory
                .newInstance();
        RolodexDetailsType rolodexDetails = RolodexDetailsType.Factory
                .newInstance();
        RolodexDetailsType rolodexDetailsType = RolodexDetailsType.Factory
                .newInstance();
        OrganizationType organisation = OrganizationType.Factory.newInstance();
        if (subaward.getRolodex() != null) {
            subcontractDetail.setSiteInvestigator(subaward.getRolodex()
                    .getFullName());
            if (subaward.getRolodex().getFullName() != null
                    && subaward.getRolodex().getFullName().length() > 0) {
                rolodexDetails.setRolodexName(subaward.getRolodex()
                        .getFullName());
            } else {
                rolodexDetails.setRolodexName(subaward.getRolodex()
                        .getOrganization());
            }
            rolodexDetails.setAddress1(subaward.getRolodex().getAddressLine1());
            rolodexDetails.setAddress2(subaward.getRolodex().getAddressLine2());
            rolodexDetails.setAddress3(subaward.getRolodex().getAddressLine3());
            rolodexDetails.setCity(subaward.getRolodex().getCity());
            String countryCode = subaward.getRolodex().getCountryCode();
            String stateName = subaward.getRolodex().getState();
            if (countryCode != null && countryCode.length() > 0
                    && stateName != null && stateName.length() > 0) {
                State state = KraServiceLocator.getService(PrintingUtils.class)
                        .getStateFromName(countryCode, stateName);
                if (state != null) {
                    rolodexDetails.setStateDescription(state.getName());
                }
            }
            rolodexDetails.setPincode(subaward.getRolodex().getPostalCode());
            rolodexDetails.setPhoneNumber(subaward.getRolodex()
                    .getPhoneNumber());
            rolodexDetails.setFax(subaward.getRolodex().getFaxNumber());
            rolodexDetails.setEmail(subaward.getRolodex().getEmailAddress());
        }
        subcontractDetail.setPONumber(subaward.getPurchaseOrderNum());
        if (subaward.getOrganization() != null) {
            subcontractDetail.setSubcontractorName(subaward.getOrganization()
                    .getOrganizationName());
            rolodexDetailsType.setAddress1(subaward.getOrganization()
                    .getRolodex().getAddressLine1());
            rolodexDetailsType.setAddress2(subaward.getOrganization()
                    .getRolodex().getAddressLine2());
            rolodexDetailsType.setAddress3(subaward.getOrganization()
                    .getRolodex().getAddressLine3());
            rolodexDetailsType.setCity(subaward.getOrganization().getRolodex()
                    .getCity());
            String countryCode = subaward.getOrganization().getRolodex()
                    .getCountryCode();
            String stateName = subaward.getOrganization().getRolodex()
                    .getState();
            if (countryCode != null && countryCode.length() > 0
                    && stateName != null && stateName.length() > 0) {
                State state = KraServiceLocator.getService(PrintingUtils.class)
                        .getStateFromName(countryCode, stateName);
                if (state != null) {
                    rolodexDetailsType.setStateDescription(state.getName());
                }
            }
            rolodexDetailsType.setPincode(subaward.getOrganization()
                    .getRolodex().getPostalCode());
            organisation.setFedralEmployerId(subaward.getOrganization()
                    .getFederalEmployerId());
            organisation.setDunsNumber(subaward.getOrganization()
                    .getDunsNumber());
            organisation.setCongressionalDistrict(subaward.getOrganization()
                    .getCongressionalDistrict());
        }
        if (subaward.getStartDate() != null) {
            subcontractDetail.setStartDate(getDateTimeService().getCalendar(
                    subaward.getStartDate()));
        }
        if (subaward.getEndDate() != null) {
            subcontractDetail.setEndDate(getDateTimeService().getCalendar(
                    subaward.getEndDate()));
        }

        subcontractDetail.setSubcontractorOrgRolodexDetails(rolodexDetailsType);
        subcontractDetail.setSiteInvestigatorDetails(rolodexDetails);
        subcontractDetail.setSubcontractorDetails(organisation);

        // BUKC-0128: Check if SAAI list is not empty to avoid
        // IndexOutOfBoundsException (DFCT0011431)
        if (!subaward.getSubAwardAmountInfoList().isEmpty()) {
            subcontractDetail.setComments(subaward.getSubAwardAmountInfoList()
                    .get(subaward.getSubAwardAmountInfoList().size() - 1)
                    .getComments());
        }
        // // BUKC-0129: KC Subaward: FDP Modification Template Enhancement
        // (ENHC0012935)
        setCustomAttributes(subcontractDetail);



        subContractData.setSubcontractDetail(subcontractDetail);
    }

    private void setCustomAttributes(SubcontractDetail subcontractDetail) {
        String modificationType = (String) this.reportParameters
                .get("modificationType");
        if (modificationType != null && !modificationType.isEmpty()) {
            subcontractDetail.setModificationType(modificationType);
        }
        String fcoi = (String) this.reportParameters.get("fcoi");
        // BUKC-0129: KC Subaward: FDP Modification Template Enhancement FFATA
        // (ENHC0012935)
        String ffata = (String) this.reportParameters.get("ffata");
        if (ObjectUtils.isNotNull(ffata) && StringUtils.isNotEmpty(ffata)) {
            subcontractDetail.setFFATA(ffata.toUpperCase());

        }
        String randd = (String) this.reportParameters.get("randd");
        if (ObjectUtils.isNotNull(ffata) && StringUtils.isNotEmpty(randd)) {
            subcontractDetail.setRANDD(randd.toUpperCase());

        }
        String costshare = (String) this.reportParameters.get("costshare");
        if (ObjectUtils.isNotNull(costshare)
                && StringUtils.isNotEmpty(costshare)) {
            subcontractDetail.setCOSTSHARE(costshare.toUpperCase());

        }
        if (ObjectUtils.isNotNull(fcoi)
                && StringUtils.isNotEmpty(fcoi)) {
            subcontractDetail.setPHSFCOI(fcoi.toUpperCase());

        }
    }



    // // BUKC-0129: KC Subaward: FDP Modification Template Enhancement (ENHC0012935)
    @Override
    public void setSubcontractAmountInfo(SubContractData subContractData, SubAward subaward) {

        SubcontractAmountInfo subContractAmountInfo = SubcontractAmountInfo.Factory.newInstance();
        List<SubcontractAmountInfo> amountinfoList = new ArrayList<SubcontractAmountInfo>();
        if(subaward.getSubAwardAmountInfoList() != null && !subaward.getSubAwardAmountInfoList().isEmpty()){
            subContractAmountInfo.setObligatedAmount(subaward.getTotalObligatedAmount().bigDecimalValue());
            subContractAmountInfo.setAnticipatedAmount(subaward.getTotalAnticipatedAmount().bigDecimalValue());
        }
        // add the last subawardamountinfochange
        if(subaward.getSubAwardAmountInfoList() != null && !subaward.getSubAwardAmountInfoList().isEmpty()){
            SubAwardAmountInfo amountInfo = subaward.getSubAwardAmountInfoList().get(subaward.getSubAwardAmountInfoList().size() - 1);
            subContractAmountInfo.setObligatedChange(amountInfo.getObligatedChange().bigDecimalValue());
            subContractAmountInfo.setAnticipatedChange(amountInfo.getAnticipatedChange().bigDecimalValue());
        }

        // BUKC-0329: KC Subaward: New FDP templates (ENHC0013043)
        if(subaward.getSubAwardAmountInfoList() != null && !subaward.getSubAwardAmountInfoList().isEmpty()){
            SubAwardAmountInfo amountInfo = subaward.getSubAwardAmountInfoList().get(subaward.getSubAwardAmountInfoList().size() - 1);
            if(amountInfo.getPeriodofPerformanceStartDate() != null){
                subContractAmountInfo.setPerformanceStartDate(getDateTimeService().getCalendar(amountInfo.getPeriodofPerformanceStartDate()));
            }
            if(amountInfo.getPeriodofPerformanceEndDate() != null){
                subContractAmountInfo.setPerformanceEndDate(getDateTimeService().getCalendar(amountInfo.getPeriodofPerformanceEndDate()));
            }
            if(amountInfo.getModificationEffectiveDate() != null){
                subContractAmountInfo.setModificationEffectiveDate(getDateTimeService().getCalendar(amountInfo.getModificationEffectiveDate()));
            }
            if (amountInfo.getModificationID() != null) {
                subContractAmountInfo.setModificationNumber(amountInfo.getModificationID());
            }

        }
        amountinfoList.add(subContractAmountInfo);
        subContractData.setSubcontractAmountInfoArray((SubcontractAmountInfo[]) amountinfoList.toArray(new SubcontractAmountInfo[0]));
    }

    public void setPrimePrincipalInvestigator(SubContractData subContractData, SubAward subaward) {
        PersonDetailsType personDetails = PersonDetailsType.Factory.newInstance();
        List<PersonDetailsType> personDetailsList = new ArrayList<PersonDetailsType>();
        Map<String, String> awardNum = new HashMap<String, String>();
        if(getAwardNumber() != null){
            awardNum.put("awardNumber",getAwardNumber());

            // BUKC-0126: Only show PI on the form (DFCT0011317)
            awardNum.put("roleCode", "PI");

            List<AwardPerson> awardNumList = (List<AwardPerson>) getBusinessObjectService().findMatchingOrderBy(AwardPerson.class, awardNum, "sequenceNumber", true);
            AwardPerson awardPerson = awardNumList.get(awardNumList.size() - 1);
            KcPerson awardPersons = KraServiceLocator.getService(KcPersonService.class).getKcPersonByPersonId(awardPerson.getPersonId());

            personDetails.setFullName(awardPersons.getFullName());
            personDetails.setAddressLine1(awardPersons.getAddressLine1());
            personDetails.setAddressLine2(awardPersons.getAddressLine2());
            personDetails.setAddressLine3(awardPersons.getAddressLine3());
            personDetails.setCity(awardPersons.getCity());
            String countryCode = awardPersons.getCountryCode();
            String stateName = awardPersons.getState();
            if(countryCode != null && countryCode.length() > 0 && stateName != null && stateName.length() > 0){
                State state = KraServiceLocator.getService(PrintingUtils.class).getStateFromName(countryCode, stateName);
                if(state != null){
                    personDetails.setState(state.getName());
                }
            }
            personDetails.setPostalCode(awardPersons.getPostalCode());
            personDetails.setMobilePhoneNumber(awardPersons.getOfficePhone());
            personDetails.setFaxNumber(awardPersons.getFaxNumber());
            personDetails.setEmailAddress(awardPersons.getEmailAddress());
        }
        personDetailsList.add(personDetails);
        subContractData.setPrimePrincipalInvestigatorArray((PersonDetailsType[])personDetailsList.toArray(new PersonDetailsType [0]));
    }




    // BUKC-0108: Show Award Prime Recipient address based on Requisitioner Unit to show on FDP Modification and FDP Attachment 3A templates (BU Enhancements #8)
    public void setPrimeRecipientContacts(SubContractData subContractData, SubAward subaward) {

        SubContractData.PrimeRecipientContacts primeReceipient = SubContractData.PrimeRecipientContacts.Factory.newInstance();
        OrganizationType organisation = OrganizationType.Factory.newInstance();
        RolodexDetailsType rolodexDetails = RolodexDetailsType.Factory.newInstance();
        Map<String, String> primeUniversityMap = new HashMap<String, String>();
        primeUniversityMap.put("organizationId", "000001");
        UnitService unitService = KraServiceLocator.getService(UnitService.class);
        Organization univOrganisation = KraServiceLocator.getService(BusinessObjectService.class).findByPrimaryKey(Organization.class, primeUniversityMap);
        if (univOrganisation.getRolodex() != null) {
            organisation.setOrganizationName(univOrganisation.getOrganizationName());
            primeUniversityMap.clear();
        }

        primeUniversityMap.put("organizationId", StringUtils.substring(subaward.getRequisitionerUnit(), 0, 1));

        Organization primeOrganisation = KraServiceLocator.getService(BusinessObjectService.class).findByPrimaryKey(Organization.class, primeUniversityMap);
        if (primeOrganisation.getRolodex() != null) {

            rolodexDetails.setAddress1(primeOrganisation.getRolodex().getAddressLine1());
            rolodexDetails.setAddress2(primeOrganisation.getRolodex().getAddressLine2());
            rolodexDetails.setAddress3(primeOrganisation.getRolodex().getAddressLine3());
            rolodexDetails.setCity(primeOrganisation.getRolodex().getCity());
            String countryCode = primeOrganisation.getRolodex().getCountryCode();
            String stateName = primeOrganisation.getRolodex().getState();
            if (countryCode != null && countryCode.length() > 0 && stateName != null && stateName.length() > 0) {
                State state = KraServiceLocator.getService(PrintingUtils.class).getStateFromName(countryCode, stateName);
                if (state != null) {
                    rolodexDetails.setStateDescription(state.getName());
                }
            }
            rolodexDetails.setPincode(primeOrganisation.getRolodex().getPostalCode());
        }
        primeReceipient.setOrgRolodexDetails(rolodexDetails);
        primeReceipient.setRequisitionerOrgDetails(organisation);
        subContractData.setPrimeRecipientContacts(primeReceipient);
    }

    /**
     * Sets fcoi attribute value.
     *
     * @param fcoi fcoi to set
     */
    public void setFcoi(String fcoi) {
        this.fcoi = fcoi;
    }

    /**
     * Gets fcoi attribute
     *
     * @return Returns fcoi
     */
    public String getFcoi() {
        return fcoi;
    }

    /**
     * Sets modificationType attribute value
     *
     * @param modificationType modificationType to set
     */
    public void setModificationType(String modificationType) {
        this.modificationType = modificationType;
    }

    /**
     * Gets modificationType attribute
     *
     * @return Returns modificationType
     */
    public String getModificationType() {
        return modificationType;
    }
}