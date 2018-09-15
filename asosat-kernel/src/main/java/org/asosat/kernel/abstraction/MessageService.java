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
package org.asosat.kernel.abstraction;

import org.asosat.kernel.abstraction.Message.ExchangedMessage;
import org.asosat.kernel.pattern.interceptor.Asynchronous;
import org.asosat.kernel.stereotype.InfrastructureServices;

/**
 * asosat-kernel
 *
 * @author bingo 上午11:11:09
 *
 */
public interface MessageService {

  MessageConvertor getConvertor();

  Message persist(Message message);

  @Asynchronous(fair = false)
  void receive(ExchangedMessage message);

  @Asynchronous(fair = false)
  void send(ExchangedMessage messages);

  /**
   * @author bingo 下午12:27:57
   *
   */
  @InfrastructureServices
  public static interface MessageConvertor {

    Message from(ExchangedMessage message);

    ExchangedMessage to(Message message);

  }

}