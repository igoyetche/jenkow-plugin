/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.editor.rest.model;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.activiti.editor.constants.ModelDataJsonConstants;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Model;
import org.apache.commons.lang.math.NumberUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.restlet.data.Status;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

/**
 * @author Tijs Rademakers
 */
public class ModelEditorJsonRestResource extends ServerResource implements ModelDataJsonConstants {
  
  protected static final Logger LOGGER = Logger.getLogger(ModelEditorJsonRestResource.class.getName());
  private ObjectMapper objectMapper = new ObjectMapper();
  
  @Get
  public ObjectNode getEditorJson() {
    ObjectNode modelNode = null;
    String modelId = (String) getRequest().getAttributes().get("modelId");
    
    if(NumberUtils.isNumber(modelId)) {
      RepositoryService repositoryService = ProcessEngines.getDefaultProcessEngine().getRepositoryService();
      Model model = repositoryService.getModel(modelId);
      
      if (model != null) {
        try {
          modelNode = (ObjectNode) objectMapper.readTree(model.getMetaInfo());
          modelNode.put(MODEL_ID, model.getId());
          ObjectNode editorJsonNode = (ObjectNode) objectMapper.readTree(new String(repositoryService.getModelEditorSource(model.getId()), "utf-8"));
          modelNode.put("model", editorJsonNode);
          
        } catch(Exception e) {
          LOGGER.log(Level.SEVERE, "Error creating model JSON", e);
          setStatus(Status.SERVER_ERROR_INTERNAL);
        }
      }
    }
    return modelNode;
  }
}
