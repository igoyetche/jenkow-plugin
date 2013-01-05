package org.activiti.designer.property;

import org.activiti.bpmn.model.StartEvent;
import org.activiti.bpmn.model.UserTask;
import org.activiti.designer.util.property.ActivitiPropertyFilter;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;

public class PropertyFormPropertyFilter extends ActivitiPropertyFilter {
	
	@Override
	protected boolean accept(PictogramElement pe) {
		Object bo = getBusinessObject(pe);
		if (bo instanceof UserTask) {
		  return true;
		} else if (bo instanceof StartEvent) {
		  StartEvent startEvent = (StartEvent) bo;
      if (startEvent.getEventDefinitions().size() > 0) {
        return false;
      } else {
        return true;
      }
 			 
		}
		return false;
	}

}
