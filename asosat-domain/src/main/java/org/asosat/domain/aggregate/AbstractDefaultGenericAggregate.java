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

import java.util.function.Consumer;
import java.util.function.Function;
import javax.persistence.MappedSuperclass;
import org.asosat.kernel.abstraction.Being.Destroyable;
import org.asosat.kernel.abstraction.Being.Enabling;

/**
 * @author bingo 下午7:42:44
 *
 */
@MappedSuperclass
public abstract class AbstractDefaultGenericAggregate<P, T extends AbstractDefaultAggregate>
    extends AbstractDefaultAggregate implements Enabling<P, T>, Destroyable<P, T> {

  private static final long serialVersionUID = 3815839476729207935L;

  public AbstractDefaultGenericAggregate() {}

  @SuppressWarnings("unchecked")
  public T accept(Consumer<T> consumer) {
    T me = (T) this;
    if (consumer != null) {
      consumer.accept(me);
    }
    return me;
  }

  @SuppressWarnings("unchecked")
  public <R> R apply(Function<T, R> function) {
    if (function != null) {
      return function.apply((T) this);
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  public <R> T applyThenAccept(Function<T, R> function, Consumer<R> consumer) {
    T me = (T) this;
    if (function != null) {
      R r = function.apply(me);
      if (consumer != null) {
        consumer.accept(r);
      }
    }
    return me;
  }

  @SuppressWarnings("unchecked")
  @Override
  public void destroy(P param, DestroyHandler<P, T> handler) {
    if (handler != null) {
      handler.onPreDestroy(param, (T) this);
    }
    this.destroy(false);
  }

  @SuppressWarnings("unchecked")
  @Override
  public T enable(P param, EnablingHandler<P, T> handler) {
    if (handler != null) {
      handler.onPreEnable(param, (T) this);
    }
    return (T) this.enable(false);
  }
}
