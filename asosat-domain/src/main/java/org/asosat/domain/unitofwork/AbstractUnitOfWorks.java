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
package org.asosat.domain.unitofwork;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import org.asosat.kernel.supertype.Message;
import org.asosat.domain.message.MessageConvertor;
import org.asosat.domain.service.MessageService;

/**
 * @author bingo 下午7:13:58
 */
public abstract class AbstractUnitOfWorks implements UnitOfWorks {

  protected final transient Logger logger = Logger.getLogger(this.getClass().toString());
  protected final List<Message> message = new LinkedList<>();
  protected final UnitOfWorksManager manager;
  protected final Stream<UnitOfWorksHandler> handlers;
  protected final Stream<UnitOfWorksListener> listeners;
  protected final MessageService messageService;
  protected final MessageConvertor messageConvertor;
  protected volatile boolean activated = false;


  protected AbstractUnitOfWorks(UnitOfWorksManager manager) {
    this.manager = manager;
    this.handlers = manager.getHandlers();
    this.listeners = manager.getListeners();
    this.messageService = manager.getMessageService();
    this.messageConvertor = manager.getMessageService().getConvertor();
    this.activated = true;
  }

  @Override
  public void complete(boolean success) {
    this.activated = false;
    if (success && !this.message.isEmpty()) {
      this.message.stream().sorted(Message::compareOccurredTime).map(this.messageConvertor::to)
          .filter(Objects::nonNull).forEachOrdered(this.messageService::send);
    }
  }

  protected void clear() {
    this.message.clear();
  }

  protected UnitOfWorksManager getManager() {
    return this.manager;
  }

  protected List<Message> getMessage() {
    return this.message;
  }

  protected void handlePostCompleted(final Object registration, final boolean success) {
    this.manager.getListeners().forEach(listener -> {
      try {
        listener.onCompleted(registration, success);
      } catch (Exception ex) {
        this.logger.log(Level.WARNING, ex.getMessage());
      }
    });
  }

  protected void handlePreComplete() {
    this.manager.getHandlers().forEach(handler -> {
      try {
        handler.onPreComplete(this);
      } catch (Exception ex) {
        this.logger.log(Level.WARNING, ex.getMessage());
      }
    });
  }
}
