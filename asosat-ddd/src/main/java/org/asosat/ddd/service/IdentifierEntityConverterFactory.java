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
package org.asosat.ddd.service;

import static org.corant.context.Instances.resolve;
import static org.corant.context.Instances.select;
import static org.corant.shared.util.Assertions.shouldNotNull;
import static org.corant.shared.util.Objects.asString;
import static org.corant.shared.util.Objects.defaultObject;
import static org.corant.shared.util.Sets.immutableSetOf;
import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.transaction.Transactional;
import org.asosat.ddd.domain.SimpleAggregateIdentifier;
import org.corant.kernel.event.PostContainerStartedEvent;
import org.corant.shared.conversion.ConversionException;
import org.corant.shared.conversion.Converter;
import org.corant.shared.conversion.ConverterFactory;
import org.corant.shared.conversion.ConverterRegistry;
import org.corant.suites.ddd.annotation.stereotype.InfrastructureServices;
import org.corant.suites.ddd.model.Entity;
import org.corant.suites.ddd.repository.JPARepository;
import org.corant.suites.ddd.repository.JPARepositoryExtension;
import org.corant.suites.jpa.shared.JPAUtils;

/**
 * corant-asosat-ddd
 *
 * @author bingo 上午12:36:03
 *
 */
@ApplicationScoped
@InfrastructureServices
public class IdentifierEntityConverterFactory implements ConverterFactory<Object, Entity> {

  static final Map<Class<?>, Boolean> cached = new ConcurrentHashMap<>();
  static final Map<Class<?>, Annotation[]> puqCached = new ConcurrentHashMap<>();
  final Set<Class<?>> supportedSourceClass =
      immutableSetOf(Long.class, Long.TYPE, String.class, Entity.class);

  final Logger logger = Logger.getLogger(this.getClass().getName());

  @Inject
  JPARepositoryExtension jpaRepoExt;

  @Transactional
  public <T extends Entity> T convert(Object value, Class<T> targetClass, Map<String, ?> hints) {
    if (value == null) {
      return null;
    }
    Long id = null;
    if (value instanceof Long || value.getClass().equals(Long.TYPE)) {
      id = Long.class.cast(value);
    } else if (value instanceof String) {
      id = Long.valueOf(value.toString());
    } else if (value instanceof SimpleAggregateIdentifier) {
      id = SimpleAggregateIdentifier.class.cast(value).getId();
    }
    T entity = null;
    if (id != null) {
      Instance<JPARepository> repos = select(JPARepository.class, resolveQualifier(targetClass));
      if (repos.isResolvable()) {
        entity = repos.get().get(targetClass, id);
      }
    }
    return shouldNotNull(entity, "Can not convert %s to %s!", value.toString(),
        targetClass.getSimpleName());
  }

  @Override
  public Converter<Object, Entity> create(Class<Entity> targetClass, Entity defaultValue,
      boolean throwException) {
    return (t, h) -> {
      Entity result = null;
      try {
        result = resolve(IdentifierEntityConverterFactory.class).convert(t, targetClass, h);
      } catch (Exception e) {
        if (throwException) {
          throw new ConversionException(e);
        } else {
          logger.warning(() -> String.format("Can not convert %s", asString(t)));
        }
      }
      return defaultObject(result, defaultValue);
    };
  }

  @Override
  public boolean isSupportSourceClass(Class<?> sourceClass) {
    return supportedSourceClass.contains(sourceClass)
        || supportedSourceClass.stream().anyMatch(c -> c.isAssignableFrom(sourceClass));
  }

  @Override
  public boolean isSupportTargetClass(Class<?> targetClass) {
    return cached.computeIfAbsent(targetClass,
        t -> Entity.class.isAssignableFrom(t) && JPAUtils.isPersistenceClass(t));
  }

  @PostConstruct
  void onPostConstruct() {
    ConverterRegistry.register(this);
  }

  // FIXME touch
  void onPostContainerStartedEvent(@Observes PostContainerStartedEvent e) {}

  @PreDestroy
  void onPreDestroy() {
    ConverterRegistry.deregister(this);
  }

  Annotation[] resolveQualifier(Class<?> cls) {
    return puqCached.computeIfAbsent(cls, (c) -> {
      return jpaRepoExt.resolveQualifiers(cls);
    });
  }
}
