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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.bphistory.model.HistoryTask;
import com.epam.digital.data.platform.bphistory.persistence.mapper.HistoryTaskMapper;
import com.epam.digital.data.platform.bphistory.persistence.repository.HistoryTaskRepository;
import com.epam.digital.data.platform.bphistory.persistence.repository.entity.BpmHistoryTask;
import com.epam.digital.data.platform.bphistory.persistence.service.TaskService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

@Import({AopAutoConfiguration.class})
@SpringBootTest(
    classes = {
        AuditAspect.class,
        KafkaAuditProcessor.class,
        TaskService.class
    })
class AuditKafkaEventsAspectTest {

  @Autowired
  private TaskService taskService;
  @MockBean
  private HistoryTaskRepository repository;
  @MockBean
  private HistoryTaskMapper mapper;
  @MockBean
  private KafkaEventsFacade kafkaEventsFacade;

  @Test
  void expectAuditAspectBeforeAndAfterUpdateMethodWhenNoException() {
    when(repository.existsById(any())).thenReturn(false);
    when(mapper.toEntity(any())).thenReturn(Mockito.mock(BpmHistoryTask.class));
    taskService.create(new HistoryTask());

    verify(kafkaEventsFacade, times(2)).sendKafkaAudit(any(), any(), any(), any(), any());
  }
}

