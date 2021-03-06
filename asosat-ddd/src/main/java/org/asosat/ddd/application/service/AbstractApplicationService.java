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
package org.asosat.ddd.application.service;

import static org.corant.shared.util.Maps.getMapObject;
import static org.corant.suites.bundle.Preconditions.requireNotBlank;
import static org.corant.suites.bundle.Preconditions.requireNotNull;
import static org.corant.suites.bundle.Preconditions.requireTrue;
import java.util.Map;
import org.asosat.ddd.security.SecurityContextHolder;
import org.asosat.shared.Participator;
import org.corant.shared.util.Empties;
import org.corant.shared.util.Objects;
import org.corant.suites.bundle.GlobalMessageCodes;
import org.corant.suites.ddd.annotation.stereotype.ApplicationServices;
import org.corant.suites.ddd.model.Entity.EntityReference;

/**
 * corant-asosat-ddd
 *
 * @author bingo 下午2:08:10
 *
 */

@ApplicationServices
public abstract class AbstractApplicationService implements ApplicationService {

  @Override
  public String getAppVerNum() {
    return null;
  }

  protected Participator currentOrg() {
    return SecurityContextHolder.currentOrg();
  }

  protected Participator currentOrg(Map<?, ?> cmd) {
    return getMapObject(cmd, Participator.CURRENT_ORG_KEY, Objects::forceCast, null);
  }

  protected Participator currentUser() {
    return SecurityContextHolder.currentUser();
  }

  protected Participator currentUser(Map<?, ?> cmd) {
    return getMapObject(cmd, Participator.CURRENT_USER_KEY, Objects::forceCast, null);
  }

  protected String notBlank(String obj) {
    return requireNotBlank(obj, GlobalMessageCodes.ERR_PARAM);
  }

  protected String notBlank(String obj, String msgCode, Object... objects) {
    return requireNotBlank(obj, msgCode, objects);
  }

  protected <T> T notEmpty(T obj) {
    return requireTrue(obj, Empties::isNotEmpty, GlobalMessageCodes.ERR_PARAM);
  }

  protected <T> T notEmpty(T obj, String msgCode, Object... objects) {
    return requireTrue(obj, Empties::isNotEmpty, msgCode, objects);
  }

  protected <T> T notNull(T obj) {
    return this.notNull(obj, GlobalMessageCodes.ERR_OBJ_NON_FUD);
  }

  protected <T> T notNull(T obj, String msgCode, Object... objects) {
    return requireNotNull(obj, msgCode, objects);
  }

  @SuppressWarnings("rawtypes")
  protected <T extends EntityReference> T validRefObj(T obj) {
    return requireTrue(obj, Objects::isNotNull, "");// FIXME
  }
}
