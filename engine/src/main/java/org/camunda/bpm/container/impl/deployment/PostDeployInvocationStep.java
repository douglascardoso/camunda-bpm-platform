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
package org.camunda.bpm.container.impl.deployment;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.camunda.bpm.application.AbstractProcessApplication;
import org.camunda.bpm.application.PostDeploy;
import org.camunda.bpm.container.impl.deployment.util.InjectionUtil;
import org.camunda.bpm.container.impl.spi.DeploymentOperation;
import org.camunda.bpm.container.impl.spi.DeploymentOperationStep;
import org.camunda.bpm.engine.ProcessEngineException;

/**
 * <p>Operation step responsible for invoking the {@literal @}{@link PostDeploy} method of a 
 * ProcessApplication class.</p> 
 *  
 * @author Daniel Meyer
 *
 */
public class PostDeployInvocationStep extends DeploymentOperationStep {

  public String getName() {
    return "Invoking @PostDeploy";
  }

  public void performOperationStep(DeploymentOperation operationContext) {
    
    final AbstractProcessApplication processApplication = operationContext.getAttachment(Attachments.PROCESS_APPLICATION);
        
    Class<? extends AbstractProcessApplication> paClass = processApplication.getClass();
    Method postDeployMethod = InjectionUtil.detectAnnotatedMethod(paClass, PostDeploy.class);
    
    if(postDeployMethod == null) {
      return;
    }
    
    // resolve injections
    Object[] injections = InjectionUtil.resolveInjections(operationContext, postDeployMethod);
    
    try {
      // perform the actual invocation
      postDeployMethod.invoke(processApplication, injections);
      
    } catch (IllegalArgumentException e) {
      throw new ProcessEngineException("IllegalArgumentException while invoking @PostDeploy method",e);       
    
    } catch (IllegalAccessException e) {
      throw new ProcessEngineException("IllegalAccessException while invoking @PostDeploy method", e);
    
    } catch (InvocationTargetException e) {
      Throwable cause = e.getCause();
      if(cause instanceof RuntimeException) {
        throw (RuntimeException) cause;
      } else {
        throw new ProcessEngineException("Exception while invoking @PostDeploy method", cause);
      }
    }
      
  }

}
