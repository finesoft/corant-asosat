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
package org.asosat.kernel.resource;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.enterprise.context.ApplicationScoped;

/**
 * @author bingo 上午10:25:24
 *
 */
@ApplicationScoped
@SuppressWarnings("rawtypes")
public interface EnumerationResource {

  List<Class<Enum>> getAllEnumClass();

  String getEnumClassLiteral(Class<Enum> enumClass, Locale locale);

  String getEnumItemLiteral(Enum enumVal, Locale locale);

  <T extends Enum> Map<T, String> getEnumItemLiterals(Class<T> enumClass, Locale locale);

}