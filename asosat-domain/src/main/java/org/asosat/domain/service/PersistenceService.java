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
package org.asosat.domain.service;

import org.asosat.domain.annotation.stereotype.InfrastructureServices;

/**
 * Global persistence service to listen the aggregate life cycle event.
 * {@code AbstractDefaultAggregate.enable()} {@code AbstractDefaultAggregate.destory()}
 *
 * @author bingo 上午9:53:57
 */
@InfrastructureServices
public interface PersistenceService {

  public void merge(Object obj, boolean immediately);

  public void persist(Object obj, boolean immediately);

  public void remove(Object obj, boolean immediately);
}
