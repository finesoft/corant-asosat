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
package org.asosat.domains.saga;

import java.lang.annotation.Annotation;
import java.util.stream.Stream;
import org.asosat.kernel.abstraction.Message;

/**
 * asosat-kernel
 *
 * @author bingo 上午1:01:40
 *
 */
public interface SagaService {

  Stream<SagaManager> getManagers(Annotation... annotations);

  void trigger(Message message);

}