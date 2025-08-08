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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.epam.digital.data.platform.bphistory.model.HistoryProcess;
import com.epam.digital.data.platform.bphistory.model.HistoryTask;
import com.epam.digital.data.platform.starter.audit.model.AuditSourceInfo;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

public class AuditSourceUtilsTest {

  @Test
  void expectGetFromHeaderToReturnStringValueWhenHeaderIsString() {
    Message<String> message = MessageBuilder.withPayload("test")
        .setHeader("X-Test-Header", "value")
        .build();

    String result = AuditSourceUtils.getFromHeader(message, "X-Test-Header");

    assertEquals("value", result);
  }

  @Test
  void expectGetFromHeaderToReturnDecodedStringWhenHeaderIsByteArray() {
    byte[] bytes = "utf8-value".getBytes(StandardCharsets.UTF_8);
    Message<String> message = MessageBuilder.withPayload("test")
        .setHeader("X-Test-Header", bytes)
        .build();

    String result = AuditSourceUtils.getFromHeader(message, "X-Test-Header");

    assertEquals("utf8-value", result);
  }

  @Test
  void expectGetFromHeaderToReturnNullWhenHeaderIsMissing() {
    Message<String> message = MessageBuilder.withPayload("test").build();

    String result = AuditSourceUtils.getFromHeader(message, "Missing-Header");

    assertNull(result);
  }

  @Test
  void expectFillFromHeadersToSetSystemAndApplicationFromMessageHeaders() {
    Message<String> message = MessageBuilder.withPayload("test")
        .setHeader("X-Source-System", "bpm")
        .setHeader("X-Source-Application", "my-app")
        .build();

    AuditSourceInfo info = new AuditSourceInfo();

    AuditSourceUtils.fillFromHeaders(info, message);

    assertEquals("bpm", info.getSystem());
    assertEquals("my-app", info.getApplication());
  }

  @Test
  void expectFillFromTaskToSetBusinessFieldsFromHistoryTask() {
    HistoryTask task = new HistoryTask();
    task.setProcessInstanceId("proc-inst-id");
    task.setProcessDefinitionId("proc-def-id");
    task.setProcessDefinitionKey("proc-key");
    task.setActivityInstanceId("act-inst-id");
    task.setTaskDefinitionKey("task-key");

    AuditSourceInfo info = new AuditSourceInfo();

    AuditSourceUtils.fillFromTask(info, task);

    assertEquals(task.getProcessInstanceId(), info.getBusinessProcessInstanceId());
    assertEquals(task.getProcessDefinitionId(), info.getBusinessProcessDefinitionId());
    assertEquals(task.getProcessDefinitionKey(), info.getBusinessProcess());
    assertEquals(task.getActivityInstanceId(), info.getBusinessActivityInstanceId());
    assertEquals(task.getTaskDefinitionKey(), info.getBusinessActivity());
  }

  @Test
  void expectFillFromProcessToSetBusinessFieldsFromHistoryProcess() {
    HistoryProcess process = new HistoryProcess();
    process.setProcessInstanceId("proc-inst-id");
    process.setProcessDefinitionId("proc-def-id");
    process.setProcessDefinitionKey("proc-key");

    AuditSourceInfo info = new AuditSourceInfo();

    AuditSourceUtils.fillFromProcess(info, process);

    assertEquals(process.getProcessInstanceId(), info.getBusinessProcessInstanceId());
    assertEquals(process.getProcessDefinitionId(), info.getBusinessProcessDefinitionId());
    assertEquals(process.getProcessDefinitionKey(), info.getBusinessProcess());
  }
}
