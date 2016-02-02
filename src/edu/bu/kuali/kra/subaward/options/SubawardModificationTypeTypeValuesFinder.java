package edu.bu.kuali.kra.subaward.options;

import org.kuali.rice.core.api.util.ConcreteKeyValue;
import org.kuali.rice.krad.uif.control.UifKeyValuesFinderBase;

import java.util.ArrayList;
import java.util.List;

public class SubawardModificationTypeTypeValuesFinder extends
		UifKeyValuesFinderBase {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public List getKeyValues() {
		List<ConcreteKeyValue> keyValues = new ArrayList<ConcreteKeyValue>();
		keyValues.add(0, new ConcreteKeyValue("", "select"));
        keyValues.add(1, new ConcreteKeyValue("New", "New"));
		keyValues.add(2, new ConcreteKeyValue("Continuation", "Continuation"));
		keyValues.add(3, new ConcreteKeyValue("Increment", "Increment"));
		keyValues.add(4, new ConcreteKeyValue("No Cost Extension", "No Cost Extension"));
        keyValues.add(5, new ConcreteKeyValue("Converted Record", "Converted Record"));
        keyValues.add(6, new ConcreteKeyValue("Other", "Other"));

		return keyValues;
	}
}
