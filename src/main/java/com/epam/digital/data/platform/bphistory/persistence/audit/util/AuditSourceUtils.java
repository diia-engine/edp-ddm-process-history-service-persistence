/*
 * Copyright 2023 EPAM Systems.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.digital.data.platform.bphistory.persistence.audit.util;

import com.epam.digital.data.platform.bphistory.model.HistoryProcess;
import com.epam.digital.data.platform.bphistory.model.HistoryTask;
import com.epam.digital.data.platform.bphistory.persistence.util.Header;
import com.epam.digital.data.platform.starter.audit.model.AuditSourceInfo;
import java.nio.charset.StandardCharsets;
import org.springframework.messaging.Message;

public class AuditSourceUtils {

  public static String getFromHeader(Message<?> message, String headerName) {
    Object value = message.getHeaders().get(headerName);
    if (value == null) {
      return null;
    }
    if (value instanceof byte[]) {
      return new String((byte[]) value, StandardCharsets.UTF_8);
    }
    return value.toString();
  }

  public static void fillFromHeaders(AuditSourceInfo sourceInfo,
      Message<?> message) {
    sourceInfo.setSystem(getFromHeader(message, Header.X_SOURCE_SYSTEM.getHeaderName()));
    sourceInfo.setApplication(getFromHeader(message, Header.X_SOURCE_APPLICATION.getHeaderName()));
  }

  public static void fillFromTask(AuditSourceInfo sourceInfo,
      HistoryTask task) {
    sourceInfo.setBusinessProcessInstanceId(task.getProcessInstanceId());
    sourceInfo.setBusinessProcess(task.getProcessDefinitionKey());
    sourceInfo.setBusinessProcessDefinitionId(task.getProcessDefinitionId());
    sourceInfo.setBusinessActivityInstanceId(task.getActivityInstanceId());
    sourceInfo.setBusinessActivity(task.getTaskDefinitionKey());
  }

  public static void fillFromProcess(AuditSourceInfo sourceInfo,
      HistoryProcess process) {
    sourceInfo.setBusinessProcessInstanceId(process.getProcessInstanceId());
    sourceInfo.setBusinessProcess(process.getProcessDefinitionKey());
    sourceInfo.setBusinessProcessDefinitionId(process.getProcessDefinitionId());
  }
}
