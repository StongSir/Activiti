/*
 * Copyright 2018 Alfresco, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.runtime.api;

import java.util.List;

import org.activiti.engine.TaskService;
import org.activiti.engine.task.TaskQuery;
import org.activiti.runtime.api.conf.TaskRuntimeConfiguration;
import org.activiti.runtime.api.identity.UserGroupManager;
import org.activiti.runtime.api.model.Task;
import org.activiti.runtime.api.model.builders.TaskPayloadBuilder;
import org.activiti.runtime.api.model.impl.APITaskConverter;
import org.activiti.runtime.api.model.impl.APIVariableInstanceConverter;
import org.activiti.runtime.api.model.impl.TaskImpl;
import org.activiti.runtime.api.model.payloads.DeleteTaskPayload;
import org.activiti.runtime.api.model.payloads.GetTasksPayload;
import org.activiti.runtime.api.query.Page;
import org.activiti.runtime.api.query.Pageable;
import org.activiti.runtime.api.query.impl.PageImpl;
import org.activiti.runtime.api.security.SecurityManager;

public class TaskAdminRuntimeImpl implements TaskAdminRuntime {

    private final TaskService taskService;

    private final APITaskConverter taskConverter;

    private final APIVariableInstanceConverter variableInstanceConverter;

    private final TaskRuntimeConfiguration configuration;

    private final UserGroupManager userGroupManager;

    private final SecurityManager securityManager;

    public TaskAdminRuntimeImpl(TaskService taskService,
                                UserGroupManager userGroupManager,
                                SecurityManager securityManager,
                                APITaskConverter taskConverter,
                                APIVariableInstanceConverter variableInstanceConverter,
                                TaskRuntimeConfiguration configuration) {
        this.taskService = taskService;
        this.userGroupManager = userGroupManager;
        this.securityManager = securityManager;
        this.taskConverter = taskConverter;
        this.variableInstanceConverter = variableInstanceConverter;
        this.configuration = configuration;
    }

    @Override
    public Task task(String taskId) {
        String authenticatedUserId = securityManager.getAuthenticatedUserId();
        List<String> userRoles = userGroupManager.getUserRoles(authenticatedUserId);
        if (userRoles != null && userRoles.contains("admin")) { //@TODO: make it flexible with a Env Var
            org.activiti.engine.task.Task internalTask = taskService.createTaskQuery().taskId(taskId).singleResult();
            if (internalTask == null) {
                throw new NotFoundException("Unable to find task for the given id: " + taskId);
            }
            return taskConverter.from(internalTask);
        }
        throw new IllegalStateException("Only users with Role Admin can use the Admin Runtimes");
    }

    @Override
    public Page<Task> tasks(Pageable pageable) {
        String authenticatedUserId = securityManager.getAuthenticatedUserId();
        List<String> userRoles = userGroupManager.getUserRoles(authenticatedUserId);
        if (userRoles != null && userRoles.contains("admin")) { //@TODO: make it flexible with a Env Var
            return tasks(pageable,
                         TaskPayloadBuilder.tasks().build());
        }
        throw new IllegalStateException("Only users with Role Admin can use the Admin Runtimes");
    }

    @Override
    public Page<Task> tasks(Pageable pageable,
                            GetTasksPayload getTasksPayload) {
        String authenticatedUserId = securityManager.getAuthenticatedUserId();
        List<String> userRoles = userGroupManager.getUserRoles(authenticatedUserId);
        if (userRoles != null && userRoles.contains("admin")) { //@TODO: make it flexible with a Env Var
            TaskQuery taskQuery = taskService.createTaskQuery();

            if (getTasksPayload.getProcessInstanceId() != null) {
                taskQuery = taskQuery.processInstanceId(getTasksPayload.getProcessInstanceId());
            }
            if (getTasksPayload.getParentTaskId() != null) {
                taskQuery = taskQuery.taskParentTaskId(getTasksPayload.getParentTaskId());
            }

            List<Task> tasks = taskConverter.from(taskQuery.listPage(pageable.getStartIndex(),
                                                                     pageable.getMaxItems()));
            return new PageImpl<>(tasks,
                                  Math.toIntExact(taskQuery.count()));
        }
        throw new IllegalStateException("Only users with Role Admin can use the Admin Runtimes");
    }

    @Override
    public Task delete(DeleteTaskPayload deleteTaskPayload) {
        String authenticatedUserId = securityManager.getAuthenticatedUserId();
        List<String> userRoles = userGroupManager.getUserRoles(authenticatedUserId);
        if (userRoles != null && userRoles.contains("admin")) { //@TODO: make it flexible with a Env Var
            //      we might need to create an empty shell with the task ID and Status only
            Task task = task(deleteTaskPayload.getTaskId());

            TaskImpl deletedTaskData = new TaskImpl(task.getId(),
                                                    task.getName(),
                                                    Task.TaskStatus.DELETED);
            taskService.deleteTask(deleteTaskPayload.getTaskId(),
                                   deleteTaskPayload.getReason(),
                                   true);
            return deletedTaskData;
        }
        throw new IllegalStateException("Only users with Role Admin can use the Admin Runtimes");
    }

    private org.activiti.engine.task.Task getInternalTask(String taskId) {
        org.activiti.engine.task.Task internalTask = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (internalTask == null) {
            throw new NotFoundException("Unable to find task for the given id: " + taskId);
        }
        return internalTask;
    }
}
