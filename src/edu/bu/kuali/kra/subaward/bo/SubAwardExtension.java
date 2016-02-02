package edu.bu.kuali.kra.subaward.bo;

import org.kuali.kra.bo.KraPersistableBusinessObjectBase;
import org.kuali.rice.krad.bo.PersistableBusinessObjectExtension;
import java.sql.Date;
public class SubAwardExtension extends KraPersistableBusinessObjectBase implements
PersistableBusinessObjectExtension{
	
	 private Long subAwardId;
	 public Long getSubAwardId() {
		return subAwardId;
	}
	public void setSubAwardId(Long subAwardId) {
		this.subAwardId = subAwardId;
	}
	public Date getDateReceived() {
		return dateReceived;
	}
	public void setDateReceived(Date dateReceived) {
		this.dateReceived = dateReceived;
	}
	private Date dateReceived;

}
