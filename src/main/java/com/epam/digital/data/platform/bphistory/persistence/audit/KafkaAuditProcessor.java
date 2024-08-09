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

import com.epam.digital.data.platform.bphistory.persistence.audit.AuditableService.Operation;
import com.epam.digital.data.platform.starter.audit.model.EventType;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.stereotype.Component;

@Component
public class KafkaAuditProcessor {

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

    String methodName = joinPoint.getSignature().getName();

    kafkaEventsFacade.sendKafkaAudit(
        EventType.USER_ACTION, methodName, operation.name(), BEFORE, null);

    Object result = joinPoint.proceed();

    kafkaEventsFacade.sendKafkaAudit(
        EventType.USER_ACTION, methodName, operation.name(), AFTER, STATUS);
    return result;
  }
}

