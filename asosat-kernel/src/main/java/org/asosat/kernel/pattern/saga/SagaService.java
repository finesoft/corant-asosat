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
package org.asosat.kernel.pattern.saga;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.stream.Stream;
import org.asosat.kernel.abstraction.Aggregate.AggregateIdentifier;
import org.asosat.kernel.abstraction.Message;
import org.asosat.kernel.stereotype.DomainServices;

/**
 * asosat-kernel
 *
 * @author bingo 上午11:23:36
 *
 */
public interface SagaService {

  Stream<SagaManager> getManagers(Annotation... annotations);

  void persist(Saga saga);

  void trigger(Message message);

  @DomainServices
  public static interface SagaManager {

    Saga begin(Message message);

    void end(Message message);

    Saga get(String queue, String trackingToken);

    List<Saga> select(AggregateIdentifier aggregateIdentifier);
  }

}