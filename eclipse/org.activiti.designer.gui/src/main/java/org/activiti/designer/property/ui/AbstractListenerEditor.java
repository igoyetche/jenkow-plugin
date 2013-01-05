package org.activiti.designer.property.ui;

import java.util.List;

import org.activiti.bpmn.model.ActivitiListener;
import org.activiti.bpmn.model.FieldExtension;
import org.activiti.bpmn.model.alfresco.AlfrescoScriptTask;
import org.activiti.bpmn.model.alfresco.AlfrescoUserTask;
import org.activiti.designer.util.BpmnBOUtil;
import org.activiti.designer.util.eclipse.ActivitiUiUtil;
import org.apache.commons.lang.StringUtils;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.graphiti.mm.pictograms.Diagram;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.platform.IDiagramEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;


public abstract class AbstractListenerEditor extends TableFieldEditor {
	
	protected Composite parent;
	public PictogramElement pictogramElement;
	public IDiagramEditor diagramEditor;
	public Diagram diagram;
	public boolean isSequenceFlow;
	private List<ActivitiListener> listenerList;
	
	public AbstractListenerEditor(String key, Composite parent) {
		
    super(key, "", new String[] {"Listener implementation", "Type", "Event", "Fields"},
    		new int[] {200, 150, 100, 300}, parent);
    this.parent = parent;
	}
	
	public void initialize(List<ActivitiListener> listenerList) {
	  removeTableItems();
	  this.listenerList = listenerList;
		if(listenerList == null || listenerList.size() == 0) return;
		for (ActivitiListener listener : listenerList) {
			addTableItem(listener);
		}
	}

	@Override
	protected String createList(String[][] items) {
		return null;
	}

	@Override
	protected String[][] parseString(String string) {
		return null;
	}
	
	protected void addTableItem(ActivitiListener listener) {
	  
    if(table != null) {
      TableItem tableItem = new TableItem(table, SWT.NONE);
      tableItem.setText(0, listener.getImplementation());
      tableItem.setText(1, listener.getImplementationType());
      String event = listener.getEvent();
      if(isSequenceFlow && listener.getEvent() == null) {
        event = "take";
      }
      tableItem.setText(2, event);
      String fieldString = "";
      if(listener.getFieldExtensions() != null) {
        for (FieldExtension fieldExtension : listener.getFieldExtensions()) {
          if(fieldString.length() > 0) {
            fieldString += "� ";
          }
          if (StringUtils.isNotEmpty(fieldExtension.getExpression())) {
            fieldString += fieldExtension.getFieldName() + ":" + fieldExtension.getExpression();
          } else {
            fieldString += fieldExtension.getFieldName() + ":" + fieldExtension.getStringValue();
          }
        }
      }
      tableItem.setText(3, fieldString);
    }
  }

	@Override
	protected String[] getNewInputObject() {
	  AbstractListenerDialog dialog = getDialog(parent.getShell(), getItems());
		dialog.open();
		if(StringUtils.isNotEmpty(dialog.eventName) && StringUtils.isNotEmpty(dialog.implementation)) {
			saveNewObject(dialog);
			return new String[] { dialog.implementation, dialog.implementationType, dialog.eventName, getFieldString(dialog.fieldExtensionList) };
		} else {
			return null;
		}
	}
	
	@Override
  protected String[] getChangedInputObject(TableItem item) {
		
		int index = table.getSelectionIndex();
	  AbstractListenerDialog dialog = getDialog(parent.getShell(), getItems(), 
	  		listenerList.get(table.getSelectionIndex()));
    dialog.open();
    if(StringUtils.isNotEmpty(dialog.eventName) && StringUtils.isNotEmpty(dialog.implementation)) {
    	saveChangedObject(dialog, index);
      return new String[] { dialog.implementation, dialog.implementationType, dialog.eventName, getFieldString(dialog.fieldExtensionList) };
    } else {
      return null;
    }
  }
	
	@Override
  protected void removedItem(int index) {
		if(index >= 0 && index < listenerList.size()) {
			saveRemovedObject(listenerList.get(index));
		}
  }
	
	protected abstract AbstractListenerDialog getDialog(Shell shell, TableItem[] items);
	
	protected abstract AbstractListenerDialog getDialog(Shell shell, TableItem[] items, 
	        ActivitiListener listener);
	
	private String getFieldString(List<FieldExtension> fieldList) {
	  String fieldString = "";
    if(fieldList != null) {
      for (FieldExtension fieldExtension : fieldList) {
        if(fieldString.length() > 0) {
          fieldString += ", ";
        }
        if (StringUtils.isNotEmpty(fieldExtension.getExpression())) {
          fieldString += fieldExtension.getFieldName() + ":" + fieldExtension.getExpression();
        } else {
          fieldString += fieldExtension.getFieldName() + ":" + fieldExtension.getStringValue();
        }
      }
    }
    return fieldString;
	}
	
	private void saveNewObject(final AbstractListenerDialog dialog) {
		if (pictogramElement != null) {
		  final Object bo = BpmnBOUtil.getExecutionListenerBO(pictogramElement, diagram);
		  if (bo == null) {
        return;
      }
			TransactionalEditingDomain editingDomain = diagramEditor.getEditingDomain();
			ActivitiUiUtil.runModelChange(new Runnable() {
				public void run() {
				  ActivitiListener newListener = new ActivitiListener();
				  newListener.setEvent(dialog.eventName);
				  newListener.setImplementationType(dialog.implementationType);
				  newListener.setImplementation(dialog.implementation);
				  if(AlfrescoUserTask.ALFRESCO_SCRIPT_TASK_LISTENER.equalsIgnoreCase(dialog.implementation) ||
                  AlfrescoScriptTask.ALFRESCO_SCRIPT_EXECUTION_LISTENER.equalsIgnoreCase(dialog.implementation)) {
            
            FieldExtension scriptExtension = new FieldExtension();
            scriptExtension.setFieldName("script");
            scriptExtension.setStringValue(dialog.script);
            newListener.getFieldExtensions().add(scriptExtension);
            
            FieldExtension runAsExtension = new FieldExtension();
            runAsExtension.setFieldName("runAs");
            runAsExtension.setStringValue(dialog.runAs);
            newListener.getFieldExtensions().add(runAsExtension);
            
            FieldExtension scriptProcessorExtension = new FieldExtension();
            scriptProcessorExtension.setFieldName("scriptProcessor");
            scriptProcessorExtension.setStringValue(dialog.scriptProcessor);
            newListener.getFieldExtensions().add(scriptProcessorExtension);
            
          } else {
            setFieldsInListener(newListener, dialog.fieldExtensionList);
          }
				  BpmnBOUtil.addListener(bo, newListener, diagram);
				}
			}, editingDomain, "Model Update");
		}
	}
	
	private void saveChangedObject(final AbstractListenerDialog dialog, final int index) {
		if (pictogramElement != null) {
		  final Object bo = BpmnBOUtil.getExecutionListenerBO(pictogramElement, diagram);
		  if (bo == null) {
        return;
      }
			TransactionalEditingDomain editingDomain = diagramEditor.getEditingDomain();
			ActivitiUiUtil.runModelChange(new Runnable() {
				public void run() {
					ActivitiListener listener = listenerList.get(index);
					if(listener != null) {
					  listener.setEvent(dialog.eventName);
					  listener.setImplementation(dialog.implementation);
					  listener.setImplementationType(dialog.implementationType);
					  if(AlfrescoUserTask.ALFRESCO_SCRIPT_TASK_LISTENER.equalsIgnoreCase(dialog.implementation) ||
	                  AlfrescoScriptTask.ALFRESCO_SCRIPT_EXECUTION_LISTENER.equalsIgnoreCase(dialog.implementation)) {
					    
					    List<FieldExtension> extensionList = listener.getFieldExtensions();
					    FieldExtension scriptExtension = null;
					    FieldExtension runAsExtension = null;
					    FieldExtension scriptProcessorExtension = null;
				      for (FieldExtension fieldExtension : extensionList) {
				        if ("script".equalsIgnoreCase(fieldExtension.getFieldName())) {
				          scriptExtension = fieldExtension;
				        } else if ("runAs".equalsIgnoreCase(fieldExtension.getFieldName())) {
                  runAsExtension = fieldExtension;
                } else if ("scriptProcessor".equalsIgnoreCase(fieldExtension.getFieldName())) {
                  scriptProcessorExtension = fieldExtension;
                }
				      }
				      
				      if (scriptExtension != null) {
				        scriptExtension.setStringValue(dialog.script);
				      } else {
				        scriptExtension = new FieldExtension();
				        scriptExtension.setFieldName("script");
				        scriptExtension.setStringValue(dialog.script);
				        listener.getFieldExtensions().add(scriptExtension);
				      }
				      
				      if (runAsExtension != null) {
				        runAsExtension.setStringValue(dialog.runAs);
              } else {
                runAsExtension = new FieldExtension();
                runAsExtension.setFieldName("runAs");
                runAsExtension.setStringValue(dialog.runAs);
                listener.getFieldExtensions().add(runAsExtension);
              }
				      
				      if (scriptProcessorExtension != null) {
				        scriptProcessorExtension.setStringValue(dialog.scriptProcessor);
              } else {
                scriptProcessorExtension = new FieldExtension();
                scriptProcessorExtension.setFieldName("scriptProcessor");
                scriptProcessorExtension.setStringValue(dialog.scriptProcessor);
                listener.getFieldExtensions().add(scriptProcessorExtension);
              }
					    
					  } else {
					    setFieldsInListener(listener, dialog.fieldExtensionList);
					  }
					  BpmnBOUtil.setListener(bo, listener, index, diagram);
					}
					
				}
			}, editingDomain, "Model Update");
		}
	}
	
	private void saveRemovedObject(final ActivitiListener listener) {
		if (pictogramElement != null) {
		  final Object bo = BpmnBOUtil.getExecutionListenerBO(pictogramElement, diagram);
		  if (bo == null) {
        return;
		  }
			TransactionalEditingDomain editingDomain = diagramEditor.getEditingDomain();
			ActivitiUiUtil.runModelChange(new Runnable() {
				public void run() {
					BpmnBOUtil.removeListener(bo, listener, diagram);
				}
			}, editingDomain, "Model Update");
		}
	}
	
	@Override
  protected void upPressed() {
	  final int index = table.getSelectionIndex();
    final Object bo = BpmnBOUtil.getExecutionListenerBO(pictogramElement, diagram);
    if (bo != null) {
      TransactionalEditingDomain editingDomain = diagramEditor.getEditingDomain();
      ActivitiUiUtil.runModelChange(new Runnable() {
        public void run() {
          ActivitiListener listener = listenerList.remove(index);
          listenerList.add(index - 1, listener);
        }
      }, editingDomain, "Model Update");
    }
    super.upPressed();
  }

  @Override
  protected void downPressed() {
    final int index = table.getSelectionIndex();
    final Object bo = BpmnBOUtil.getExecutionListenerBO(pictogramElement, diagram);
    if (bo != null) {
      TransactionalEditingDomain editingDomain = diagramEditor.getEditingDomain();
      ActivitiUiUtil.runModelChange(new Runnable() {
        public void run() {
          ActivitiListener listener = listenerList.remove(index);
          listenerList.add(index + 1, listener);
        }
      }, editingDomain, "Model Update");
    }
    super.downPressed();
  }

  private void setFieldsInListener(ActivitiListener listener, List<FieldExtension> fieldList) {
	  if(listener != null) {
  		listener.getFieldExtensions().clear();
		  for (FieldExtension fieldModel : fieldList) {
		    FieldExtension fieldExtension = new FieldExtension();
		    listener.getFieldExtensions().add(fieldExtension);
		    fieldExtension.setFieldName(fieldModel.getFieldName());
		    fieldExtension.setStringValue(fieldModel.getStringValue());
		    fieldExtension.setExpression(fieldModel.getExpression());
	    }
	  }
	}
}
