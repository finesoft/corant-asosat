package org.corant.asosat.ddd.domain.model;

import static org.apache.commons.lang3.reflect.ConstructorUtils.invokeExactConstructor;
import static org.corant.shared.util.ConversionUtils.toLong;
import static org.corant.shared.util.ConversionUtils.toObject;
import static org.corant.shared.util.Empties.isEmpty;
import static org.corant.shared.util.ObjectUtils.forceCast;
import static org.corant.shared.util.StringUtils.isNotBlank;
import static org.corant.suites.bundle.GlobalMessageCodes.ERR_OBJ_NON_FUD;
import static org.corant.suites.bundle.GlobalMessageCodes.ERR_PARAM;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import org.corant.kernel.exception.GeneralRuntimeException;
import org.corant.kernel.util.Instances;
import org.corant.suites.ddd.model.Entity.EntityReference;
import org.corant.suites.ddd.repository.JPARepository;
import org.corant.suites.ddd.repository.JPARepositoryExtension;

@SuppressWarnings("rawtypes")
public interface AggregateReference<T extends AbstractGenericAggregate> extends EntityReference<T> {

  static <A extends AbstractGenericAggregate, T extends AggregateReference<A>> T of(Object param,
      Class<T> cls) {
    if (param == null) {
      return null; // FIXME like c++ reference
    }
    try {
      if (cls.isAssignableFrom(param.getClass())) {
        return forceCast(param);
      } else {
        Long id = toLong(param);
        if (id != null) {
          return invokeExactConstructor(cls, new Object[] {id}, new Class<?>[] {Long.class});
        }
      }
    } catch (Exception e) {
      throw new GeneralRuntimeException(e, ERR_OBJ_NON_FUD);
    }
    throw new GeneralRuntimeException(ERR_OBJ_NON_FUD);
  }

  static <X> X resolve(Serializable id, Class<X> cls) {
    if (id != null && cls != null) {
      return toObject(id, cls);
    }
    throw new GeneralRuntimeException(ERR_PARAM);
  }

  static <X> X resolve(String namedQuery, Class<X> cls, Map<Object, Object> params) {
    if (isNotBlank(namedQuery)) {
      Annotation[] quas = JPARepositoryExtension.resolveQualifiers(cls);
      JPARepository jpar = Instances.resolve(JPARepository.class, quas).get();
      List<X> list = jpar.select(namedQuery, params);
      if (!isEmpty(list)) {
        if (list.size() > 1) {
          throw new GeneralRuntimeException(ERR_OBJ_NON_FUD);
        }
        return list.get(0);
      }
    }
    throw new GeneralRuntimeException(ERR_PARAM);
  }

  static <X> X resolve(String namedQuery, Class<X> cls, Object... params) {
    if (isNotBlank(namedQuery)) {
      Annotation[] quas = JPARepositoryExtension.resolveQualifiers(cls);
      JPARepository jpar = Instances.resolve(JPARepository.class, quas).get();
      List<X> list = jpar.select(namedQuery, params);
      if (!isEmpty(list)) {
        if (list.size() > 1) {
          throw new GeneralRuntimeException(ERR_OBJ_NON_FUD);
        }
        return list.get(0);
      }
    }
    throw new GeneralRuntimeException(ERR_PARAM);
  }

  static <X> List<X> resolveList(String namedQuery, Class<X> cls, Object... params) {
    Annotation[] quas = JPARepositoryExtension.resolveQualifiers(cls);
    JPARepository jpar = Instances.resolve(JPARepository.class, quas).get();
    return jpar.select(namedQuery, params);
  }

  @SuppressWarnings("unchecked")
  @Override
  default T retrieve() {
    Class<T> resolveClass = null;
    Class<?> t = getClass();
    do {
      Type[] genericInterfaces = t.getGenericInterfaces();
      if (genericInterfaces != null) {
        for (Type type : genericInterfaces) {
          if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            if (parameterizedType.getRawType() == AggregateReference.class) {
              resolveClass = (Class<T>) parameterizedType.getActualTypeArguments()[0];
              break;
            }
          }
        }
      }
    } while (resolveClass == null && (t = t.getSuperclass()) != null);
    return resolve(getId(), resolveClass);
  }
}