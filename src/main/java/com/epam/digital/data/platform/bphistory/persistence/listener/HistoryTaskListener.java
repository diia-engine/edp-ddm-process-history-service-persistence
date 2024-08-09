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

package com.epam.digital.data.platform.bphistory.persistence.listener;

import com.epam.digital.data.platform.bphistory.model.HistoryTask;
import com.epam.digital.data.platform.bphistory.persistence.service.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class HistoryTaskListener {

  private final Logger log = LoggerFactory.getLogger(HistoryTaskListener.class);

  private final TaskService taskService;

  public HistoryTaskListener(TaskService taskService) {
    this.taskService = taskService;
  }

  @KafkaListener(
      topics = "\u0023{kafkaProperties.topics['bpm-history-task']}",
      groupId = "\u0023{kafkaProperties.consumer.groupId}",
      containerFactory = "concurrentKafkaListenerContainerFactory")
  public void save(HistoryTask input) {
    log.info("Kafka event received");
    if (input != null) {
      log.info(
          "Save Task with id: {}",
          input.getActivityInstanceId());
      if(taskService.isExist(input.getActivityInstanceId())) {
        taskService.update(input);
      } else {
        taskService.create(input);
      }
    }
  }
}
