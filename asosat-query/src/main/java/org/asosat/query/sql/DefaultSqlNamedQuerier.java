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
package org.asosat.query.sql;

import java.util.Arrays;
import java.util.List;
import org.asosat.query.NamedQueryResolver.Querier;
import org.asosat.query.mapping.FetchQuery;

/**
 * asosat-query
 *
 * @author bingo 下午4:35:55
 *
 */
public class DefaultSqlNamedQuerier implements Querier<String, Object[], FetchQuery> {

  final String script;
  Object[] convertedParams;
  final Class<?> resultClass;
  final List<FetchQuery> fetchQueries;


  /**
   * @param script
   * @param resultClass
   * @param fetchQueries
   */
  public DefaultSqlNamedQuerier(String script, Class<?> resultClass,
      List<FetchQuery> fetchQueries) {
    super();
    this.script = script;
    this.resultClass = resultClass;
    this.fetchQueries = fetchQueries;
  }


  /**
   * @param script
   * @param convertedParams
   * @param resultClass
   * @param fetchQueries
   */
  public DefaultSqlNamedQuerier(String script, Object[] convertedParams, Class<?> resultClass,
      List<FetchQuery> fetchQueries) {
    this(script, resultClass, fetchQueries);
    this.setConvertedParams(convertedParams);
  }


  @Override
  public Object[] getConvertedParameters() {
    return this.convertedParams;
  }

  @Override
  public List<FetchQuery> getFetchQueries() {
    return this.fetchQueries;
  }

  /**
   * @return the resultClass
   */
  @SuppressWarnings("unchecked")
  @Override
  public <T> Class<T> getResultClass() {
    return (Class<T>) this.resultClass;
  }

  @Override
  public String getScript() {
    return this.script;
  }

  void setConvertedParams(Object[] param) {
    this.convertedParams = param == null ? new Object[0] : Arrays.copyOf(param, param.length);
  }

  DefaultSqlNamedQuerier withParam(Object[] convertedParams) {
    this.setConvertedParams(convertedParams);
    return this;
  }
}