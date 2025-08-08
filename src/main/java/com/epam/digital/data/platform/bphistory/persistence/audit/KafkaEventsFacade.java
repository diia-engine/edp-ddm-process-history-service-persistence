/*
 * Copyright 2021 EPAM Systems.
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

import com.epam.digital.data.platform.bphistory.persistence.service.TraceService;
import com.epam.digital.data.platform.starter.audit.model.AuditSourceInfo;
import com.epam.digital.data.platform.starter.audit.model.AuditUserInfo;
import com.epam.digital.data.platform.starter.audit.model.EventType;
import com.epam.digital.data.platform.starter.audit.service.AbstractAuditFacade;
import com.epam.digital.data.platform.starter.audit.service.AuditService;
import com.epam.digital.data.platform.starter.security.jwt.TokenParser;
import java.time.Clock;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class KafkaEventsFacade extends AbstractAuditFacade {

  static final String KAFKA_REQUEST = "Kafka request. Method: ";

  private final TraceService traceService;
  private final TokenParser tokenParser;

  public KafkaEventsFacade(
      AuditService auditService,
      @Value("${spring.application.name}") String appName,
      Clock clock,
      TraceService traceService,
      TokenParser tokenParser) {
    super(auditService, appName, clock);
    this.traceService = traceService;
    this.tokenParser = tokenParser;
  }

  public void sendKafkaAudit(EventType eventType, String methodName,
      String action, String token, AuditSourceInfo info, String step,
      String result) {
    var event = createBaseAuditEvent(
        eventType, KAFKA_REQUEST + methodName, traceService.getRequestId())
        .setSourceInfo(info);

    var context = auditService.createContext(action, step, null, null,null, result);
    event.setContext(context);
    setUserInfoToEvent(event, token);

    auditService.sendAudit(event.build());
  }

  private void setUserInfoToEvent(GroupedAuditEventBuilder event, String jwt) {
    if (jwt == null) {
      return;
    }

    var jwtClaimsDto = tokenParser.parseClaims(jwt);
    var userInfo = AuditUserInfo.AuditUserInfoBuilder.anAuditUserInfo()
        .userName(jwtClaimsDto.getFullName())
        .userKeycloakId(jwtClaimsDto.getSubject())
        .userDrfo(jwtClaimsDto.getDrfo())
        .build();
    event.setUserInfo(userInfo);
  }
}
