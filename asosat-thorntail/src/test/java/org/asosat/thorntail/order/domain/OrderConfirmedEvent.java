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
package org.asosat.thorntail.order.domain;

import org.asosat.domain.event.AbstractEvent;

/**
 * asosat-thorntail-example
 *
 * @author bingo 下午7:59:44
 *
 */
public class OrderConfirmedEvent extends AbstractEvent {

  private static final long serialVersionUID = -5090978171054060890L;

  /**
   * @param source
   */
  public OrderConfirmedEvent(Order source) {
    super(source);
  }

}
