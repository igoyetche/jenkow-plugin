package org.activiti.designer.property;

import org.activiti.bpmn.model.Activity;
import org.activiti.bpmn.model.Pool;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.bpmn.model.UserTask;
import org.activiti.designer.util.property.ActivitiPropertyFilter;
import org.eclipse.graphiti.mm.pictograms.Diagram;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;

public class PropertyExecutionListenerFilter extends ActivitiPropertyFilter {

	@Override
	protected boolean accept(PictogramElement pe) {
		Object bo = getBusinessObject(pe);
		if (bo instanceof Activity && (bo instanceof UserTask == false)) {
			return true;
		} else if (bo instanceof SequenceFlow || pe instanceof Diagram) {
		  return true;
		} else if (bo instanceof Pool) {
		  return true;
		}
		return false;
	}

}
