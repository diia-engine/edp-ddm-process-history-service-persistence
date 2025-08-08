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

package com.epam.digital.data.platform.bphistory.persistence.audit;

import com.epam.digital.data.platform.bphistory.model.HistoryProcess;
import com.epam.digital.data.platform.bphistory.model.HistoryTask;
import com.epam.digital.data.platform.bphistory.persistence.audit.AuditableService.Operation;
import com.epam.digital.data.platform.bphistory.persistence.audit.util.AuditSourceUtils;
import com.epam.digital.data.platform.bphistory.persistence.util.Header;
import com.epam.digital.data.platform.starter.audit.model.AuditSourceInfo;
import com.epam.digital.data.platform.starter.audit.model.EventType;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.stereotype.Component;

@Component
public class KafkaAuditProcessor implements AuditProcessor<Operation> {

  private String STATUS = "SUCCESS";

  static final String BEFORE = "BEFORE";
  static final String AFTER = "AFTER";

  private final KafkaEventsFacade kafkaEventsFacade;

  public KafkaAuditProcessor(KafkaEventsFacade kafkaEventsFacade) {
    this.kafkaEventsFacade = kafkaEventsFacade;
  }

  public Object process(ProceedingJoinPoint joinPoint, Operation operation) throws Throwable {
    return prepareAndSendKafkaAudit(joinPoint, operation);
  }

  private Object prepareAndSendKafkaAudit(ProceedingJoinPoint joinPoint, Operation operation)
      throws Throwable {
    Message<?> message = getArgumentByType(joinPoint, GenericMessage.class);
    Object payload = message.getPayload();
    String token = AuditSourceUtils.getFromHeader(message, Header.X_ACCESS_TOKEN.getHeaderName());

    var sourceInfo = new AuditSourceInfo();
    AuditSourceUtils.fillFromHeaders(sourceInfo, message);

    if (payload instanceof HistoryTask) {
      AuditSourceUtils.fillFromTask(sourceInfo, (HistoryTask) payload);
    } else if (payload instanceof HistoryProcess) {
      AuditSourceUtils.fillFromProcess(sourceInfo, (HistoryProcess) payload);
    }

    String methodName = joinPoint.getSignature().getName();

    kafkaEventsFacade.sendKafkaAudit(
        EventType.USER_ACTION,
        methodName,
        operation.name(),
        token,
        sourceInfo,
        BEFORE,
        null);

    Object result = joinPoint.proceed();

    kafkaEventsFacade.sendKafkaAudit(
        EventType.USER_ACTION,
        methodName,
        operation.name(),
        token,
        sourceInfo,
        AFTER,
        STATUS);
    return result;
  }
}

