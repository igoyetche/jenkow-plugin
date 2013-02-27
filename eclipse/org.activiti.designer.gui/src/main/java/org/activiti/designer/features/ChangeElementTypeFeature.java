package org.activiti.designer.features;

import java.util.Collection;
import java.util.List;

import org.activiti.bpmn.model.Activity;
import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.BoundaryEvent;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.FlowNode;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.bpmn.model.SubProcess;
import org.activiti.designer.util.editor.ModelHandler;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.ICustomContext;
import org.eclipse.graphiti.features.context.impl.CreateContext;
import org.eclipse.graphiti.features.custom.AbstractCustomFeature;
import org.eclipse.graphiti.mm.algorithms.GraphicsAlgorithm;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import org.eclipse.graphiti.mm.pictograms.Shape;

public class ChangeElementTypeFeature extends AbstractCustomFeature {
	
	private String newType;
	
	public ChangeElementTypeFeature(IFeatureProvider fp) {
		super(fp);
	}

	public ChangeElementTypeFeature(IFeatureProvider fp, String newType) {
		super(fp);
		this.newType = newType;
	}
	
	@Override
  public boolean canExecute(ICustomContext context) {
	  return true;
  }

	@Override
  public void execute(ICustomContext context) {
	  Shape element = (Shape) context.getProperty("org.activiti.designer.changetype.pictogram");
	  GraphicsAlgorithm elementGraphics = element.getGraphicsAlgorithm();
	  int x = elementGraphics.getX();
	  int y = elementGraphics.getY();
	  
	  CreateContext taskContext = new CreateContext();
	  ContainerShape targetContainer = (ContainerShape) element.getContainer();
  	taskContext.setTargetContainer(targetContainer);
  	taskContext.setLocation(x, y);
  	taskContext.setHeight(elementGraphics.getHeight());
  	taskContext.setWidth(elementGraphics.getWidth());
  	
  	FlowNode oldObject = (FlowNode) getFeatureProvider().getBusinessObjectForPictogramElement(element);
  	if (oldObject instanceof BoundaryEvent) {
  	  BoundaryEvent boundaryEvent = (BoundaryEvent) oldObject;
  	  ContainerShape parentShape = (ContainerShape) getFeatureProvider().getPictogramElementForBusinessObject(boundaryEvent.getAttachedToRef());
  	  taskContext.setTargetContainer(parentShape);
  	  taskContext.setLocation(x - parentShape.getGraphicsAlgorithm().getX(), y - parentShape.getGraphicsAlgorithm().getY());
  	}
	  
	  List<SequenceFlow> sourceList = oldObject.getOutgoingFlows();
	  List<SequenceFlow> targetList = oldObject.getIncomingFlows();
	  
	  taskContext.putProperty("org.activiti.designer.changetype.sourceflows", sourceList);
	  taskContext.putProperty("org.activiti.designer.changetype.targetflows", targetList);
	  taskContext.putProperty("org.activiti.designer.changetype.name", oldObject.getName());
	  
	  targetContainer.getChildren().remove(element);
	  List<Process> processes = ModelHandler.getModel(EcoreUtil.getURI(getDiagram())).getBpmnModel().getProcesses();
    for (Process process : processes) {
      process.removeFlowElement(oldObject.getId());
      removeElement(oldObject, process);
    }
	  
	  if("servicetask".equals(newType)) {
	  	new CreateServiceTaskFeature(getFeatureProvider()).create(taskContext);
	  
	  } else if("businessruletask".equals(newType)) {
	  	new CreateBusinessRuleTaskFeature(getFeatureProvider()).create(taskContext);

	  } else if("mailtask".equals(newType)) {
	  	new CreateMailTaskFeature(getFeatureProvider()).create(taskContext);
	  
	  } else if("manualtask".equals(newType)) {
	  	new CreateManualTaskFeature(getFeatureProvider()).create(taskContext);
	  	
	  } else if("receivetask".equals(newType)) {
	  	new CreateReceiveTaskFeature(getFeatureProvider()).create(taskContext);
	  
	  } else if("scripttask".equals(newType)) {
	  	new CreateScriptTaskFeature(getFeatureProvider()).create(taskContext);
	  	
	  } else if("usertask".equals(newType)) {
	  	new CreateUserTaskFeature(getFeatureProvider()).create(taskContext);
	  
	  } else if("exclusivegateway".equals(newType)) {
	  	new CreateExclusiveGatewayFeature(getFeatureProvider()).create(taskContext);
	 
	  } else if("inclusivegateway".equals(newType)) {
      new CreateInclusiveGatewayFeature(getFeatureProvider()).create(taskContext);
     
    } else if("parallelgateway".equals(newType)) {
	  	new CreateParallelGatewayFeature(getFeatureProvider()).create(taskContext);
	  
    } else if("nonestartevent".equals(newType)) {
      new CreateStartEventFeature(getFeatureProvider()).create(taskContext);
    
    } else if("timerstartevent".equals(newType)) {
      new CreateTimerStartEventFeature(getFeatureProvider()).create(taskContext);
    
    } else if("messagestartevent".equals(newType)) {
      new CreateMessageStartEventFeature(getFeatureProvider()).create(taskContext);
    
    } else if("errorstartevent".equals(newType)) {
      new CreateErrorStartEventFeature(getFeatureProvider()).create(taskContext);
    
    } else if("noneendevent".equals(newType)) {
      new CreateEndEventFeature(getFeatureProvider()).create(taskContext);
    
    } else if("errorendevent".equals(newType)) {
      new CreateErrorEndEventFeature(getFeatureProvider()).create(taskContext);
    
    } else if("terminateendevent".equals(newType)) {
      new CreateTerminateEndEventFeature(getFeatureProvider()).create(taskContext);
    
    } else if ("timerboundaryevent".equals(newType)) {
      new CreateBoundaryTimerFeature(getFeatureProvider()).create(taskContext);
    
    } else if ("errorboundaryevent".equals(newType)) {
      new CreateBoundaryErrorFeature(getFeatureProvider()).create(taskContext);
    
    } else if ("messageboundaryevent".equals(newType)) {
      new CreateBoundaryMessageFeature(getFeatureProvider()).create(taskContext);
    
    } else if ("signalboundaryevent".equals(newType)) {
      new CreateBoundarySignalFeature(getFeatureProvider()).create(taskContext);
    
    }
  }
	
	private void removeElement(FlowElement element, BaseElement parentElement) {
	  Collection<FlowElement> elementList = null;
	  if (parentElement instanceof Process) {
	    elementList = ((Process) parentElement).getFlowElements();
	  } else if (parentElement instanceof SubProcess) {
	    elementList = ((SubProcess) parentElement).getFlowElements();
	    ((SubProcess) parentElement).getBoundaryEvents().remove(element);
	  }
	  
    for (FlowElement flowElement : elementList) {
      if (flowElement instanceof SubProcess) {
        SubProcess subProcess = (SubProcess) flowElement;
        subProcess.removeFlowElement(element.getId());
        removeElement(element, subProcess);
      }
      if (flowElement instanceof Activity) {
        ((Activity) flowElement).getBoundaryEvents().remove(element);
      }
    }
  }
}
