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
package org.asosat.query.mongo;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.asosat.query.NamedQuery;

/**
 * asosat-query
 *
 * @author bingo 上午9:59:20
 *
 */
public abstract class AbstractMongoNamedQuery implements NamedQuery {

  @Override
  public <T> T get(String q, Map<String, Object> param) {
    return null;
  }

  @Override
  public <T> PagedList<T> page(String q, Map<String, Object> param) {
    return null;
  }

  @Override
  public <T> ScrolledList<T> scroll(String q, Map<String, Object> param) {
    return null;
  }

  @Override
  public <T> List<T> select(String q, Map<String, Object> param) {
    return null;
  }

  @Override
  public <T> Stream<T> stream(String q, Map<String, Object> param) {
    return null;
  }

}