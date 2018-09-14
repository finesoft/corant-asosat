/*
 * Copyright (c) 2013-2018. BIN.CHEN
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.asosat.domain.aggregate;

import java.lang.annotation.Annotation;
import java.util.List;
import org.asosat.kernel.abstraction.Aggregate;
import org.asosat.kernel.abstraction.Event;
import org.asosat.kernel.abstraction.Message;

/**
 * An aggregate event and message assistant for collect message to queue or just fire event to bus.
 *
 * @author bingo 上午10:50:56
 *
 */
public interface AggregateAssistant {

  /**
   * Clear the message queue.
   */
  void clearMessages();

  /**
   * Obtain the message queue, if flush is true then clear queue.
   */
  List<Message> extractMessages(boolean flush);

  /**
   * fire aggregate event
   */
  void fire(Event event, Annotation... qualifiers);

  /**
   * fire aggregate asynchronous event
   */
  void fireAsync(Event event, Annotation... qualifiers);

  /**
   * The aggregate which it serve
   */
  Aggregate getAggregate();

  /**
   * Obtain the message serial number
   */
  long getMessageSequenceNumber();

  /**
   * Handle aggregate message
   */
  void raise(Message... messages);

}
