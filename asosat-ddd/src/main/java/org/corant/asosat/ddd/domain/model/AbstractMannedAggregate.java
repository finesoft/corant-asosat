/*
 * Copyright (c) 2013-2018, Bingo.Chen (finesoft@gmail.com).
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
package org.corant.asosat.ddd.domain.model;

import java.time.Instant;
import javax.persistence.Embedded;
import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;

import org.asosat.shared.CreationInfo;
import org.asosat.shared.Manned;
import org.asosat.shared.ModificationInfo;
import org.asosat.shared.Participator;

/**
 * corant-asosat-ddd
 *
 * @author bingo 下午1:26:45
 *
 */
@MappedSuperclass
@EntityListeners(value = {MannedAggregateEntityListener.class})
public abstract class AbstractMannedAggregate<P, T extends AbstractMannedAggregate<P, T>>
    extends AbstractGenericAggregate<P, T> implements Manned {

  private static final long serialVersionUID = 4296767808160742486L;

  @Embedded
  private CreationInfo creationInfo = CreationInfo.empty();

  @Embedded
  private ModificationInfo modificationInfo = ModificationInfo.empty();

  public AbstractMannedAggregate() {}

  public AbstractMannedAggregate(Participator creator) {
    this.initCreationInfo(creator);
  }

  @Override
  public T preserve(P param, PreservingHandler<P, T> handler) {
    return super.preserve(param, handler);
  }

  @Override
  public Instant getCreatedTime() {
    return this.obtainCreationInfo().getCreatedTime();
  }

  @Override
  public Participator getCreator() {
    return this.obtainCreationInfo().getCreator();
  }

  @Override
  public Instant getModifiedTime() {
    return this.obtainModificationInfo().getModifiedTime();
  }

  @Override
  public Participator getModifier() {
    return this.obtainModificationInfo().getModifier();
  }

  @SuppressWarnings("unchecked")
  public T withModificationInfo(Participator modifier) {
    this.initModificationInfo(modifier);
    return (T) this;
  }

  protected void initCreationInfo(Participator creator) {
    this.initCreationInfo(creator, Instant.now());// FIXME
  }

  protected void initCreationInfo(Participator creator, Instant createdTime) {
    this.creationInfo = new CreationInfo(creator, createdTime);
  }

  protected void initModificationInfo(Participator modifier) {
    this.initModificationInfo(modifier, Instant.now());// FIXME
  }

  protected void initModificationInfo(Participator modifier, Instant modifiedTime) {
    this.modificationInfo = new ModificationInfo(modifier, modifiedTime);
  }

  protected CreationInfo obtainCreationInfo() {
    if (this.creationInfo == null) {
      this.creationInfo = CreationInfo.empty();
    }
    return this.creationInfo;
  }

  protected ModificationInfo obtainModificationInfo() {
    if (this.modificationInfo == null) {
      this.modificationInfo = ModificationInfo.empty();
    }
    return this.modificationInfo;
  }
}