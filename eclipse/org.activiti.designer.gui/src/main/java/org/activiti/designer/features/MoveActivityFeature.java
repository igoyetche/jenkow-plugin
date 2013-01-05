package org.activiti.designer.features;

import java.util.List;

import org.activiti.bpmn.model.Activity;
import org.activiti.bpmn.model.BoundaryEvent;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.Lane;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.bpmn.model.SubProcess;
import org.activiti.designer.util.editor.Bpmn2MemoryModel;
import org.activiti.designer.util.editor.ModelHandler;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.IMoveShapeContext;
import org.eclipse.graphiti.features.impl.DefaultMoveShapeFeature;
import org.eclipse.graphiti.mm.pictograms.Diagram;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.mm.pictograms.Shape;
import org.eclipse.graphiti.services.Graphiti;
import org.eclipse.graphiti.services.IGaService;

/**
 * This move feature takes care, that for all activity types, attached boundary events will move 
 * when the activity itself is moved.
 * 
 * @author bardioc
 */
public class MoveActivityFeature extends DefaultMoveShapeFeature {

	/** Creates the feature */
	public MoveActivityFeature(IFeatureProvider fp) {
		super(fp);
	}
	
	

	@Override
  public boolean canMoveShape(IMoveShapeContext context) {
	  Object parentObject = getBusinessObjectForPictogramElement(context.getTargetContainer());
    return (context.getTargetContainer() instanceof Diagram || 
            parentObject instanceof SubProcess || parentObject instanceof Lane);
  }

  /**
	 * Makes sure attached boundary events will be moved too, in case the shape itself is moved. 
	 * Determines the amount of pixels the shape moved, finds out all boundary events of the shape 
	 * and moves them the same delta.
	 * 
	 * @param context the context of the move
	 */
	@Override
	protected void postMoveShape(IMoveShapeContext context) {
		final Shape shape = context.getShape();

		// get the delta in both directions, the activity has been moved
		final int deltaX = context.getDeltaX();
		final int deltaY = context.getDeltaY();

		// get the activity itself to determine its boundary events
		final Activity activity = (Activity) getBusinessObjectForPictogramElement(shape);
		moveActivityChilds(activity, deltaX, deltaY);
		
		Bpmn2MemoryModel model = ModelHandler.getModel(EcoreUtil.getURI(getDiagram()));
		
		if (context.getSourceContainer() != context.getTargetContainer()) {
		  if (context.getSourceContainer() instanceof Diagram == false) {
		    Object containerBo = getFeatureProvider().getBusinessObjectForPictogramElement(context.getSourceContainer());
		    if (containerBo instanceof SubProcess) {
		      SubProcess subProcess = (SubProcess) containerBo;
		      subProcess.removeFlowElement(activity.getId());
		      for (SequenceFlow flow : activity.getOutgoingFlows()) {
		        subProcess.removeFlowElement(flow.getId());
		      }
		    } else if (containerBo instanceof Lane) {
		      Lane lane = (Lane) containerBo;
          lane.getFlowReferences().remove(activity.getId());
          lane.getParentProcess().removeFlowElement(activity.getId());
          for (SequenceFlow flow : activity.getOutgoingFlows()) {
            lane.getParentProcess().removeFlowElement(flow.getId());
          }
        }
		  } else {
		    if (model.getBpmnModel().getMainProcess() == null) {
		      model.addMainProcess();
		    }
		    model.getBpmnModel().getMainProcess().removeFlowElement(activity.getId());
		    for (SequenceFlow flow : activity.getOutgoingFlows()) {
		      model.getBpmnModel().getMainProcess().removeFlowElement(flow.getId());
        }
		  }
		  
		  if (context.getTargetContainer() instanceof Diagram == false) {
        Object containerBo = getFeatureProvider().getBusinessObjectForPictogramElement(context.getTargetContainer());
        if (containerBo instanceof SubProcess) {
          SubProcess subProcess = (SubProcess) containerBo;
          subProcess.addFlowElement(activity);
          for (SequenceFlow flow : activity.getOutgoingFlows()) {
            subProcess.addFlowElement(flow);
          }
        } else if (containerBo instanceof Lane) {
          Lane lane = (Lane) containerBo;
          lane.getFlowReferences().add(activity.getId());
          lane.getParentProcess().addFlowElement(activity);
          for (SequenceFlow flow : activity.getOutgoingFlows()) {
            lane.getParentProcess().addFlowElement(flow);
          }
        }
      } else {
        if (model.getBpmnModel().getMainProcess() == null) {
          model.addMainProcess();
        }
        model.getBpmnModel().getMainProcess().addFlowElement(activity);
        for (SequenceFlow flow : activity.getOutgoingFlows()) {
          model.getBpmnModel().getMainProcess().addFlowElement(flow);
        }
      }
		}
	}
	
	private void moveActivityChilds(Activity activity, int deltaX, int deltaY) {
		// get all boundary events of the activity
		final List<BoundaryEvent> boundaryEvents = activity.getBoundaryEvents();
		moveBoundaryEvents(boundaryEvents, deltaX, deltaY);
		
		// also move all boundary events in the sub process
		if(activity instanceof SubProcess) {
			for (FlowElement subElement : ((SubProcess) activity).getFlowElements()) {
	      if(subElement instanceof Activity) {
	      	moveActivityChilds((Activity) subElement, deltaX, deltaY);
	      }
      }
		}
	}
	
	private void moveBoundaryEvents(final List<BoundaryEvent> boundaryEvents, int deltaX, int deltaY) {
		final IGaService gaService = Graphiti.getGaService();
		for (final BoundaryEvent boundaryEvent : boundaryEvents) {
			
			// get all pictogram elements. Actually this should be only a single element, however
			// Graphiti allows multiple elements. The loop itself will not really harm in this case
			final PictogramElement picto = getFeatureProvider().getPictogramElementForBusinessObject(boundaryEvent);
			
			// get the current position of the boundary event 
			int x = picto.getGraphicsAlgorithm().getX();
			int y = picto.getGraphicsAlgorithm().getY();

			// move it the same delta in both directions, the activity itself has been moved
			gaService.setLocation(picto.getGraphicsAlgorithm(), x + deltaX, y + deltaY);
		}
	}
}