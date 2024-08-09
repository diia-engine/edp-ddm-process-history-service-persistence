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

import com.epam.digital.data.platform.bphistory.model.HistoryTask;
import com.epam.digital.data.platform.bphistory.persistence.audit.AuditableService;
import com.epam.digital.data.platform.bphistory.persistence.exception.NotFoundException;
import com.epam.digital.data.platform.bphistory.persistence.mapper.HistoryTaskMapper;
import com.epam.digital.data.platform.bphistory.persistence.repository.HistoryTaskRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TaskService {

  private final Logger log = LoggerFactory.getLogger(TaskService.class);

  private final HistoryTaskRepository repository;
  private final HistoryTaskMapper mapper;

  public boolean isExist(String id) {
    return repository.existsById(id);
  }

  @AuditableService(AuditableService.Operation.TASK_CREATED)
  public void create(HistoryTask event) {
    log.info("Saving new task with id {}", event.getActivityInstanceId());

    repository.save(mapper.toEntity(event));
  }

  @AuditableService(AuditableService.Operation.TASK_UPDATED)
  public void update(HistoryTask event) {
    log.info("Task with id {} already exists, updating fields", event.getActivityInstanceId());

    var existing = repository.findById(event.getActivityInstanceId())
        .orElseThrow(() -> new NotFoundException("No task found"));
    var updatedEvent = mapper.updateEntity(event, existing);

    repository.save(updatedEvent);
  }
}
