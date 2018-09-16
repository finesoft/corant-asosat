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

import javax.sql.DataSource;
import org.asosat.query.sql.paging.dialect.Dialect;

/**
 * asosat-query
 *
 * @author bingo 下午5:56:03
 *
 */
public interface SqlQueryConfiguration {

  DataSource getDataSource();

  Dialect getDialect();

  default Integer getFetchDirection() {
    return null;
  }

  default Integer getFetchSize() {
    return 16;
  }

  default Integer getMaxFieldSize() {
    return 0;
  }

  default Integer getMaxRows() {
    return 0;
  }

  default Integer getQueryTimeout() {
    return 0;
  }

}
