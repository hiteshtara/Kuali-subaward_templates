package edu.bu.kuali.kra.subaward.bo;

import org.kuali.kra.bo.KraPersistableBusinessObjectBase;
import org.kuali.rice.krad.bo.PersistableBusinessObjectExtension;

public class SubAwardAmountInfoExtension extends KraPersistableBusinessObjectBase implements
PersistableBusinessObjectExtension{
	 private String modificationType;
	
	

	public String getModificationType() {
		return modificationType;
	}

	public void setModificationType(String modificationType) {
		this.modificationType = modificationType;
	}

	public Integer getSubAwardAmountInfoId() {
		return subAwardAmountInfoId;
	}

	public void setSubAwardAmountInfoId(Integer subAwardAmountInfoId) {
		this.subAwardAmountInfoId = subAwardAmountInfoId;
	}



	private Integer subAwardAmountInfoId;
	 
	
	}
	
	

	



