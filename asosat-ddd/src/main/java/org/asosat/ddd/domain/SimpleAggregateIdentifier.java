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
package org.asosat.ddd.domain;

import static org.corant.suites.bundle.Preconditions.requireNotNull;
import javax.persistence.Embeddable;
import javax.persistence.MappedSuperclass;
import org.corant.suites.bundle.GlobalMessageCodes;
import org.corant.suites.ddd.model.Aggregate;
import org.corant.suites.ddd.model.Aggregate.AggregateIdentifier;

/**
 * @author bingo 下午4:06:08
 *
 */
@MappedSuperclass
@Embeddable
public class SimpleAggregateIdentifier implements AggregateIdentifier {

  private static final long serialVersionUID = -370892872702435387L;

  private String type;

  private Long id;

  protected SimpleAggregateIdentifier() {}

  protected SimpleAggregateIdentifier(Aggregate aggregation) {
    type = requireNotNull(aggregation.getClass().getName(), GlobalMessageCodes.ERR_PARAM);
    id = (Long) requireNotNull(aggregation.getId(), GlobalMessageCodes.ERR_PARAM);
  }

  protected SimpleAggregateIdentifier(String type, Long id) {
    super();
    this.type = requireNotNull(type, GlobalMessageCodes.ERR_PARAM);
    this.id = requireNotNull(id, GlobalMessageCodes.ERR_PARAM);
  }

  public static SimpleAggregateIdentifier of(Aggregate aggregation) {
    return new SimpleAggregateIdentifier(aggregation);
  }

  public static SimpleAggregateIdentifier of(String type, Long id) {
    return new SimpleAggregateIdentifier(type, id);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (this.getClass() != obj.getClass()) {
      return false;
    }
    SimpleAggregateIdentifier other = (SimpleAggregateIdentifier) obj;
    if (id == null) {
      if (other.id != null) {
        return false;
      }
    } else if (!id.equals(other.id)) {
      return false;
    }
    if (type == null) {
      if (other.type != null) {
        return false;
      }
    } else if (!type.equals(other.type)) {
      return false;
    }
    return true;
  }

  @Override
  public Long getId() {
    return id;
  }

  @Override
  public String getType() {
    return type;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (id == null ? 0 : id.hashCode());
    result = prime * result + (type == null ? 0 : type.hashCode());
    return result;
  }

}
