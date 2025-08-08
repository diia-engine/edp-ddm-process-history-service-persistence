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
import com.epam.digital.data.platform.bphistory.persistence.mapper.HistoryTaskMapper;
import com.epam.digital.data.platform.bphistory.persistence.repository.HistoryTaskRepository;
import com.epam.digital.data.platform.bphistory.persistence.repository.entity.BpmHistoryTask;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TaskService {

  private final Logger log = LoggerFactory.getLogger(TaskService.class);

  private final HistoryTaskRepository repository;
  private final HistoryTaskMapper mapper;

  public Optional<BpmHistoryTask> getById(String id) {
    return repository.findById(id);
  }

  @AuditableService(AuditableService.Operation.TASK_CREATED)
  public void create(Message<HistoryTask> message) {
    HistoryTask event = message.getPayload();
    log.info("Saving new task with id {}", event.getActivityInstanceId());
    repository.save(mapper.toEntity(event));
  }

  @AuditableService(AuditableService.Operation.TASK_UPDATED)
  public void update(Message<HistoryTask> message, BpmHistoryTask existing) {
    HistoryTask event = message.getPayload();
    log.info("Task with id {} already exists, updating fields", event.getActivityInstanceId());

    var updatedEvent = mapper.updateEntity(event, existing);

    repository.save(updatedEvent);
  }
}
