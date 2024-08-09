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

package com.epam.digital.data.platform.bphistory.persistence.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.bphistory.model.HistoryTask;
import com.epam.digital.data.platform.bphistory.persistence.config.TestBeansConfig;
import com.epam.digital.data.platform.bphistory.persistence.repository.HistoryTaskRepository;
import com.epam.digital.data.platform.bphistory.persistence.repository.entity.BpmHistoryTask;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

@SpringBootTest(classes = TaskService.class)
@Import(TestBeansConfig.class)
public class TaskServiceTest {

  private final String ENTITY_ID = "task-id";
  private final String ASSIGNEE = "assignee";
  private final String ASSIGNEE_NEW = "assignee-new";
  private final String DEFINITION_NAME = "definition-name";

  @Autowired
  TaskService instance;

  @MockBean
  HistoryTaskRepository repository;

  @Captor
  ArgumentCaptor<BpmHistoryTask> captor;

  @Test
  void shouldSaveIfNotExistsInDb() {
    instance.create(new HistoryTask());

    verify(repository).save(any());
  }

  @Test
  void shouldUpdateNewFieldsAndNotModifyUnchanged() {
    when(repository.existsById(ENTITY_ID)).thenReturn(true);
    when(repository.findById(ENTITY_ID)).thenReturn(Optional.of(old()));

    instance.update(received());

    verify(repository).save(captor.capture());

    var newValue = captor.getValue();
    assertThat(newValue.getActivityInstanceId()).isEqualTo(ENTITY_ID);
    assertThat(newValue.getAssignee()).isEqualTo(ASSIGNEE_NEW);
    assertThat(newValue.getProcessDefinitionName()).isEqualTo(DEFINITION_NAME);
  }

  private BpmHistoryTask old() {
    var process = new BpmHistoryTask();
    process.setActivityInstanceId(ENTITY_ID);
    process.setAssignee(ASSIGNEE);
    return process;
  }

  private HistoryTask received() {
    var process = new HistoryTask();
    process.setActivityInstanceId(ENTITY_ID);
    process.setAssignee(ASSIGNEE_NEW);
    process.setProcessDefinitionName(DEFINITION_NAME);
    return process;
  }
}

