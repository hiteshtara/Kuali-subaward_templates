package edu.bu.kuali.kra.subaward.web.struts.action;

import edu.bu.kuali.kra.award.home.AwardExtension;

import edu.bu.kuali.kra.infrastructure.BUConstants;
import edu.bu.kuali.kra.subaward.bo.SubAwardAmountInfoExtension;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.kuali.kra.award.awardhierarchy.AwardHierarchy;
import org.kuali.kra.award.awardhierarchy.AwardHierarchyService;
import org.kuali.kra.infrastructure.Constants;
import org.kuali.kra.infrastructure.KraServiceLocator;
import org.kuali.kra.proposaldevelopment.bo.AttachmentDataSource;
import org.kuali.kra.subaward.SubAwardForm;
import org.kuali.kra.subaward.bo.SubAward;
import org.kuali.kra.subaward.bo.SubAwardForms;
import org.kuali.kra.subaward.bo.SubAwardFundingSource;
import org.kuali.kra.subaward.customdata.SubAwardCustomData;
import org.kuali.kra.subaward.notification.SubAwardNotificationContext;
import org.kuali.kra.subawardReporting.printing.SubAwardPrintType;
import org.kuali.kra.subawardReporting.printing.service.SubAwardPrintingService;
import org.kuali.rice.krad.service.BusinessObjectService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * Created by mkousheh on 10/20/14.
 */
public class SubAwardActionsAction extends org.kuali.kra.subaward.web.struts.action.SubAwardActionsAction{

    private static final String SUBAWARD_AGREEMENT = "fdpAgreement";

    private AwardHierarchyService awardHierarchyService;

    protected AwardHierarchyService getAwardHierarchyService() {
        awardHierarchyService = KraServiceLocator.getService(AwardHierarchyService.class);
        return awardHierarchyService;
    }

    public void setAwardHierarchyService(AwardHierarchyService awardHierarchyService) {
        this.awardHierarchyService = awardHierarchyService;
    }

    /**
     *
     * This method is called to print forms
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @Override
    public ActionForward printForms(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, Object> reportParameters = new HashMap<String, Object>();
        SubAwardForm subAwardForm = (SubAwardForm) form;
        List<SubAwardForms> printFormTemplates = new ArrayList<SubAwardForms>();
        List<SubAwardForms> subAwardFormList = subAwardForm.getSubAwardDocument().getSubAwardList().get(0).getSubAwardForms();
        SubAwardPrintingService printService = KraServiceLocator.getService(SubAwardPrintingService.class);
        printFormTemplates = printService.getSponsorFormTemplates(subAwardForm.getSubAwardPrintAgreement(),subAwardFormList);

        // BUKC-0172: Subaward - Fix Subaward print pdf performance - lookup only for awards that are linked to the current subaward (DFCT0011760)
        Map<String, Object> values = new HashMap<String, Object>();
        values.put("subAwardId", subAwardForm.getSubAwardDocument().getSubAwardList().get(0).getSubAwardId());
        Collection<SubAwardFundingSource> fundingSource = (Collection<SubAwardFundingSource>) KraServiceLocator.getService(BusinessObjectService.class).findMatching(SubAwardFundingSource.class, values);

        if(subAwardForm.getSubAwardPrintAgreement().getFundingSource() != null){
            for (SubAwardFundingSource subAwardFunding : fundingSource) {
                //if(subAwardForm.getSubAwardPrintAgreement().getFundingSource().equals(subAwardFunding.getSubAwardFundingSourceId().toString())){
                    reportParameters.put("awardNumber",subAwardFunding.getAward().getAwardNumber());
                    reportParameters.put("awardTitle",subAwardFunding.getAward().getParentTitle());
                    reportParameters.put("sponsorAwardNumber",subAwardFunding.getAward().getSponsorAwardNumber());
                    reportParameters.put("sponsorName",subAwardFunding.getAward().getSponsor().getSponsorName());
                    reportParameters.put("cfdaNumber",subAwardFunding.getAward().getCfdaNumber());

                    // BUKC-0129: KC Subaward: FDP Modification Template Enhancement (ENHC0012935)
                    int index=  subAwardForm.getSubAwardDocument().getSubAward().getSubAwardAmountInfoList().size() - 1;
                    if(!subAwardForm.getSubAwardDocument().getSubAward().getSubAwardAmountInfoList().isEmpty()){
                        String modificationType =
                                ((SubAwardAmountInfoExtension) subAwardForm.getSubAwardDocument().getSubAward().getSubAwardAmountInfoList().get(index).getExtension()).getModificationType();
                        reportParameters.put("modificationType", modificationType);
                    }
                    // BUKC-0103: FDP Agreement Template (Cost Reimbursement) enhancement (Subaward enhancements 25)
                    AwardHierarchy hierarchy = getAwardHierarchyService().loadAwardHierarchy(subAwardFunding.getAward().getAwardNumber());
                    if (!(hierarchy == null) ) {
                        String fain = ((AwardExtension) hierarchy.getRoot().getAward().getExtension()).getFain();
                        reportParameters.put("fain",fain);
                    }
                    reportParameters.put("awardID",subAwardFunding.getAward().getAwardId());
                //}
            }
        }

        SubAwardPrintingService subAwardPrintingService = KraServiceLocator.getService(SubAwardPrintingService.class);
        AttachmentDataSource dataStream ;
        reportParameters.put(SubAwardPrintingService.SELECTED_TEMPLATES, printFormTemplates);

        // BUKC-0104: FDP  Modification Enhancement (Subaward Enhancement 13, 38,  43, 45)
        reportParameters.put("fcoi", getCustomAttributeValue(subAwardForm,CustomAtribute.FCOI));
        // BUKC-0129: KC Subaward: FDP Modification Template Enhancement  FFATA (ENHC0012935)
        reportParameters.put("ffata", getCustomAttributeValue(subAwardForm,CustomAtribute.FFATA));

        //BUKC-0146: Add Subaward Custom Fields - R&D and Cost Sharing (ENHC0013365 and ENHC0013357) 
        reportParameters.put("randd", getCustomAttributeValue(subAwardForm,CustomAtribute.RD));
        reportParameters.put("costshare", getCustomAttributeValue(subAwardForm,CustomAtribute.COSTSHARE));

        reportParameters.put("fdpType",subAwardForm.getSubAwardPrintAgreement().getFdpType());
        if(subAwardForm.getSubAwardPrintAgreement().getFdpType().equals(SUBAWARD_AGREEMENT)){
            dataStream = subAwardPrintingService.printSubAwardFDPReport(subAwardForm.getSubAwardDocument().getSubAward(), SubAwardPrintType.SUB_AWARD_FDP_TEMPLATE, reportParameters);
        } else{
            dataStream = subAwardPrintingService.printSubAwardFDPReport(subAwardForm.getSubAwardDocument().getSubAward(), SubAwardPrintType.SUB_AWARD_FDP_MODIFICATION, reportParameters);
        }
        streamToResponse(dataStream,response);

        return  mapping.findForward(Constants.MAPPING_BASIC);
    }

    private String getCustomAttributeValue(SubAwardForm subAwardForm, CustomAtribute type) {
        String result = null;
        for(SubAwardCustomData data:subAwardForm.getSubAward().getSubAwardCustomDataList()){
            if(data.getCustomAttributeId() != null && data.getCustomAttributeId()==type.getCustomValue()){
                result= data.getValue();
                break;
            }
        }
        return result;
    }




    private static enum CustomAtribute {
        FCOI(BUConstants.FCOI_CUSTOM_DATA_ID), FFATA(BUConstants.FFATA_CUSTOM_DATA_ID),RD(BUConstants.RD_CUSTOM_DATA_ID),COSTSHARE(BUConstants.COSTSHARE_CUSTOM_DATA_ID);
        private int customValue;

        private CustomAtribute(int value) {
            customValue = value;
        }

        public int getCustomValue() {
            return customValue;
        }


    }
}

