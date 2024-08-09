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

import com.epam.digital.data.platform.bphistory.model.HistoryProcess;
import com.epam.digital.data.platform.bphistory.persistence.audit.AuditableService;
import com.epam.digital.data.platform.bphistory.persistence.exception.NotFoundException;
import com.epam.digital.data.platform.bphistory.persistence.mapper.HistoryProcessMapper;
import com.epam.digital.data.platform.bphistory.persistence.repository.HistoryProcessRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProcessService {

  private final Logger log = LoggerFactory.getLogger(ProcessService.class);

  private final HistoryProcessRepository repository;
  private final HistoryProcessMapper mapper;

  public boolean isExist(String id) {
    return repository.existsById(id);
  }

  @AuditableService(value = AuditableService.Operation.PROCESS_CREATED)
  public void create(HistoryProcess event) {
    log.info("Saving new process with id {}", event.getProcessInstanceId());
    repository.save(mapper.toEntity(event));
  }

  @AuditableService(value = AuditableService.Operation.PROCESS_UPDATED)
  public void update(HistoryProcess event) {
    log.info("Process with id {} already exists, updating fields", event.getProcessInstanceId());

    var existing = repository.findById(event.getProcessInstanceId())
        .orElseThrow(() -> new NotFoundException("No process found"));
    var updatedEvent = mapper.updateEntity(event, existing);

    repository.save(updatedEvent);
  }
}
