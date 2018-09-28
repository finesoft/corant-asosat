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
package org.asosat.kernel.normal.conversion;

import javax.enterprise.context.ApplicationScoped;

/**
 * asosat-kernel
 *
 * @author bingo 上午12:11:08
 *
 */
@ApplicationScoped
public class DefaultConversionService implements ConversionService {

  @Override
  public Object convert(Object value, Class<?> clazz) {
    return Conversions.provider.convert(value, clazz);
  }

  @Override
  public void deregister(Class<?> clazz) {
    Conversions.provider.deregister(clazz);
  }

  @Override
  public Convertor getConvertor(Class<?> targetType) {
    return Conversions.getConvertor(targetType);
  }

  @Override
  public Convertor getConvertor(Class<?> sourceType, Class<?> targetType) {
    return Conversions.getConvertor(sourceType, targetType);
  }

  @Override
  public <T> void register(Convertor convertor, Class<T> clazz) {
    Conversions.provider.register(convertor, clazz);
  }

}
