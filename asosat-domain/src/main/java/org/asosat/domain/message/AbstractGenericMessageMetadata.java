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
package org.asosat.domain.message;

import java.time.Instant;
import javax.persistence.MappedSuperclass;

/**
 * @author bingo 下午2:56:35
 *
 */
@MappedSuperclass
public abstract class AbstractGenericMessageMetadata<A> extends AbstractMessageMetadata {

  private static final long serialVersionUID = 819624969461261093L;

  protected AbstractGenericMessageMetadata() {}

  protected AbstractGenericMessageMetadata(String queue, String trackingToken, Instant occurredTime,
      long sequenceNumber) {
    super(queue, trackingToken, occurredTime, sequenceNumber);
  }

  protected AbstractGenericMessageMetadata(String queue, String trackingToken,
      long sequenceNumber) {
    super(queue, trackingToken, sequenceNumber);
  }

  @Override
  public abstract A getAttributes();


}
