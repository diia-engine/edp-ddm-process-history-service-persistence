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

import com.epam.digital.data.platform.bphistory.model.HistoryProcess;
import com.epam.digital.data.platform.bphistory.persistence.config.TestBeansConfig;
import com.epam.digital.data.platform.bphistory.persistence.repository.HistoryProcessRepository;
import com.epam.digital.data.platform.bphistory.persistence.repository.entity.BpmHistoryProcess;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

@SpringBootTest(classes = ProcessService.class)
@Import(TestBeansConfig.class)
public class ProcessServiceTest {

  private final String ENTITY_ID = "process-id";
  private final String BUSINESS_KEY = "business-key";
  private final String BUSINESS_KEY_NEW = "business-key-new";
  private final String COMPLETION_RESULT = "COMPLETED";

  @Autowired
  ProcessService instance;

  @MockBean
  HistoryProcessRepository repository;

  @Captor
  ArgumentCaptor<BpmHistoryProcess> captor;

  @Test
  void shouldSaveIfNotExistsInDb() {
    instance.create(new HistoryProcess());

    verify(repository).save(any());
  }

  @Test
  void shouldUpdateNewFieldsAndNotModifyUnchanged() {
    when(repository.existsById(ENTITY_ID)).thenReturn(true);
    when(repository.findById(ENTITY_ID)).thenReturn(Optional.of(old()));

    instance.update(received());

    verify(repository).save(captor.capture());

    var newValue = captor.getValue();
    assertThat(newValue.getProcessInstanceId()).isEqualTo(ENTITY_ID);
    assertThat(newValue.getBusinessKey()).isEqualTo(BUSINESS_KEY_NEW);
    assertThat(newValue.getCompletionResult()).isEqualTo(COMPLETION_RESULT);
  }

  private BpmHistoryProcess old() {
    var process = new BpmHistoryProcess();
    process.setProcessInstanceId(ENTITY_ID);
    process.setBusinessKey(BUSINESS_KEY);
    return process;
  }

  private HistoryProcess received() {
    var process = new HistoryProcess();
    process.setProcessInstanceId(ENTITY_ID);
    process.setBusinessKey(BUSINESS_KEY_NEW);
    process.setCompletionResult(COMPLETION_RESULT);
    return process;
  }
}
