package org.activiti.designer.features;

import java.util.List;

import org.activiti.bpmn.model.ImplementationType;
import org.activiti.bpmn.model.ServiceTask;
import org.activiti.designer.PluginImage;
import org.activiti.designer.integration.servicetask.CustomServiceTask;
import org.activiti.designer.util.eclipse.ActivitiUiUtil;
import org.activiti.designer.util.extension.ExtensionUtil;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.ICreateContext;

public class CreateServiceTaskFeature extends AbstractCreateFastBPMNFeature {

  public static final String FEATURE_ID_KEY = "servicetask";

  private String customServiceTaskId;

  public CreateServiceTaskFeature(IFeatureProvider fp) {
    super(fp, "ServiceTask", "Add service task");
  }

  public CreateServiceTaskFeature(IFeatureProvider fp, String name, String description, String customServiceTaskId) {
    super(fp, name, description);
    this.customServiceTaskId = customServiceTaskId;
  }

  @Override
  public Object[] create(ICreateContext context) {
    ServiceTask newServiceTask = new ServiceTask();
    newServiceTask.setName("Service Task");
    newServiceTask.setExtensionId(customServiceTaskId);

    // Process custom service tasks
    if (newServiceTask.isExtended()) {

      CustomServiceTask targetTask = findCustomServiceTask(newServiceTask);

      if (targetTask != null) {
        newServiceTask.setImplementationType(ImplementationType.IMPLEMENTATION_TYPE_CLASS);
        newServiceTask.setImplementation(targetTask.getRuntimeClassname());
        newServiceTask.setName(targetTask.getName());
      }
    }

    addObjectToContainer(context, newServiceTask, newServiceTask.getName());

    return new Object[] { newServiceTask };
  }

  private CustomServiceTask findCustomServiceTask(ServiceTask serviceTask) {
    CustomServiceTask result = null;

    if (serviceTask.isExtended()) {

      final List<CustomServiceTask> customServiceTasks = ExtensionUtil.getCustomServiceTasks(ActivitiUiUtil.getProjectFromDiagram(getDiagram()));

      for (final CustomServiceTask customServiceTask : customServiceTasks) {
        if (serviceTask.getExtensionId().equals(customServiceTask.getId())) {
          result = customServiceTask;
          break;
        }
      }
    }
    return result;
  }

  @Override
  public String getCreateImageId() {
    return PluginImage.IMG_SERVICETASK.getImageKey();
  }

  @Override
  protected String getFeatureIdKey() {
    return FEATURE_ID_KEY;
  }
}