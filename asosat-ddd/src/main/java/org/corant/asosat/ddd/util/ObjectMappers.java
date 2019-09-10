package org.corant.asosat.ddd.util;

import static org.corant.shared.util.ConversionUtils.toEnum;
import static org.corant.shared.util.Empties.isEmpty;
import static org.corant.shared.util.MapUtils.mapOf;
import static org.corant.shared.util.ObjectUtils.defaultObject;
import static org.corant.shared.util.StringUtils.isNotBlank;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.corant.Corant;
import org.corant.kernel.exception.GeneralRuntimeException;
import org.corant.shared.exception.CorantRuntimeException;
import org.corant.suites.bundle.EnumerationBundle;
import org.corant.suites.bundle.GlobalMessageCodes;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.NullSerializer;
import com.fasterxml.jackson.databind.ser.std.SqlDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * 	临时调整做法
 * 
 * @author bingo
 *
 */
public class ObjectMappers {

	public final static Long BROWSER_SAFE_LONG = 9007199254740991L;
	public final static BigInteger BROWSER_SAFE_BIGINTEGER = BigInteger.valueOf(BROWSER_SAFE_LONG);
	public final static SimpleModule SIMPLE_MODULE = new SimpleModule()
			.addSerializer(new SqlDateSerializer().withFormat(Boolean.FALSE, null))
			.addSerializer(new BigIntegerJsonSerializer()).addSerializer(new LongJsonSerializer())
			.addSerializer(new EnumJsonSerializer()).setDeserializerModifier(new EnumBeanDeserializerModifier());

	final static ObjectMapper objectMapper = new ObjectMapper();

	static {
		objectMapper.registerModules(SIMPLE_MODULE, new JavaTimeModule());
		objectMapper.getSerializerProvider().setNullKeySerializer(NullSerializer.instance);
		objectMapper.enable(Feature.ALLOW_COMMENTS);
		objectMapper.enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		objectMapper.enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);
		objectMapper.disable(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS);
		objectMapper.disable(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS);
		objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
	}

	private ObjectMappers() {
	}

	public static void clearEnumSerials() {
		EnumJsonSerializer.CACHES.clear();
	}

	/**
	 * @return The ObjectMapper clone that use in this application
	 */
	public static ObjectMapper copyMapper() {
		return objectMapper.copy();
	}

	/**
	 * Convert bytes to object
	 *
	 * @param <T>
	 * @param bytes
	 * @param cls
	 * @return fromBytes
	 */
	public static <T> T fromBytes(byte[] bytes, Class<T> cls) {
		if (isEmpty(bytes)) {
			return null;
		} else {
			try {
				return objectMapper.readerFor(cls).readValue(bytes);
			} catch (IOException e) {
				throw new CorantRuntimeException(e);
			}
		}
	}

	/**
	 * Convert input json string to Map
	 *
	 * @param cmd The json string
	 * @return Map
	 */
	@SuppressWarnings("unchecked")
	public static <K, V> Map<K, V> fromString(String cmd) {
		return fromString(cmd, Map.class);
	}

	/**
	 * Convert input string to Parameterized type object.
	 *
	 * @param cmd
	 * @param parametrized
	 * @param parameterClasses
	 * @return Object
	 */
	@SafeVarargs
	public static <C, E> C fromString(String cmd, Class<C> parametrized, Class<E>... parameterClasses) {
		if (!isNotBlank(cmd)) {
			return null;
		}
		try {
			return objectMapper.readValue(cmd,
					objectMapper.getTypeFactory().constructParametricType(parametrized, parameterClasses));
		} catch (IOException e) {
			throw new GeneralRuntimeException(e.getCause(), GlobalMessageCodes.ERR_OBJ_SEL, cmd);
		}
	}

	/**
	 * Convert input string to Map.
	 *
	 * @param cmd
	 * @param keyCls
	 * @param valueCls
	 * @return Map
	 */
	public static <K, V> Map<K, V> fromString(String cmd, Class<K> keyCls, Class<V> valueCls) {
		if (!isNotBlank(cmd)) {
			return null;
		}
		try {
			return objectMapper.readValue(cmd,
					objectMapper.getTypeFactory().constructParametricType(Map.class, keyCls, valueCls));
		} catch (IOException e) {
			throw new GeneralRuntimeException(e.getCause(), GlobalMessageCodes.ERR_OBJ_SEL, cmd);
		}
	}

	/**
	 * Convert input string to type object.
	 *
	 * @param cmd
	 * @param clazz
	 * @return
	 */
	public static <T> T fromString(String cmd, Class<T> clazz) {
		if (isNotBlank(cmd)) {
			try {
				return objectMapper.readValue(cmd, clazz);
			} catch (IOException e) {
				e.printStackTrace();
				throw new GeneralRuntimeException(e.getCause(), GlobalMessageCodes.ERR_OBJ_SEL, cmd,
						clazz.getClass().getName());
			}
		}
		return null;
	}

	public static ObjectMapper referenceMapper() {
		return objectMapper;
	}

	/**
	 * Convert object to bytes
	 *
	 * @param obj
	 * @return toBytes
	 */
	public static byte[] toBytes(Object obj) {
		if (obj == null) {
			return new byte[0];
		} else {
			try {
				return objectMapper.writeValueAsBytes(obj);
			} catch (JsonProcessingException e) {
				throw new CorantRuntimeException(e);
			}
		}
	}

	/**
	 * Convert object to json string
	 *
	 * @param obj
	 * @throws IOException
	 * @throws JsonMappingException
	 * @throws JsonGenerationException
	 */
	public static String toString(Object obj) {
		return toString(obj, false);
	}

	/**
	 * Convert object to json string
	 *
	 * @param obj
	 * @param pretty
	 * @return toJsonStr
	 */
	public static String toString(Object obj, boolean pretty) {
		if (obj != null) {
			try {
				if (pretty) {
					return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
				} else {
					return objectMapper.writeValueAsString(obj);
				}
			} catch (JsonProcessingException e) {
				throw new GeneralRuntimeException(e.getCause(), GlobalMessageCodes.ERR_OBJ_SEL, obj);
			}
		}
		return null;
	}

	static final class BigIntegerJsonSerializer extends JsonSerializer<BigInteger> {
		@Override
		public Class<BigInteger> handledType() {
			return BigInteger.class;
		}

		@Override
		public void serialize(BigInteger value, JsonGenerator gen, SerializerProvider serializers)
				throws IOException, JsonProcessingException {
			if (value.compareTo(BROWSER_SAFE_BIGINTEGER) > 0) {
				gen.writeString(value.toString());
			} else {
				gen.writeNumber(value);
			}
		}
	}

	static final class EnumBeanDeserializerModifier extends BeanDeserializerModifier {
		@SuppressWarnings("rawtypes")
		@Override
		public JsonDeserializer<Enum> modifyEnumDeserializer(DeserializationConfig config, final JavaType type,
				BeanDescription beanDesc, final JsonDeserializer<?> deserializer) {
			return new JsonDeserializer<Enum>() {
				@SuppressWarnings("unchecked")
				@Override
				public Enum deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
					Class<? extends Enum> rawClass = (Class<Enum<?>>) type.getRawClass();
					if (jp.currentToken() == JsonToken.VALUE_STRING
							|| jp.currentToken() == JsonToken.VALUE_NUMBER_INT) {
						return toEnum(jp.getValueAsString(), rawClass);
					} else if (jp.currentToken() == JsonToken.START_OBJECT) {
						JsonNode node = jp.getCodec().readTree(jp);
						return toEnum(node.get("name").asText(), rawClass);
					}
					return null;
				}
			};
		}
	}

	@SuppressWarnings("rawtypes")
	static final class EnumJsonSerializer extends JsonSerializer<Enum> {

		static final Map<Enum, Map<String, Object>> CACHES = new ConcurrentHashMap<>();

		static volatile EnumerationBundle bundle;

		@Override
		public Class<Enum> handledType() {
			return Enum.class;
		}

		@Override
		public void serialize(Enum value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
			gen.writeObject(resolveEnumLiteral(value));
		}

		EnumerationBundle resolveBundle() {
			if (bundle == null) {
				synchronized (this) {
					if (bundle == null && Corant.me() != null
							&& Corant.instance().select(EnumerationBundle.class).isResolvable()) {
						bundle = Corant.instance().select(EnumerationBundle.class).get();
					} else {
						bundle = new EnumJsonSerializerBundle();
					}
				}
			}
			return bundle;
		}

		Map<String, Object> resolveEnumLiteral(Enum value) {
			return CACHES.computeIfAbsent(value, (v) -> {
				String literal = resolveBundle().getEnumItemLiteral(value, Locale.getDefault());
				return mapOf("name", value.name(), "literal", defaultObject(literal, value.name()), "class",
						value.getDeclaringClass().getName(), "ordinal", value.ordinal());
			});
		}
	}

	@SuppressWarnings("rawtypes")
	static final class EnumJsonSerializerBundle implements EnumerationBundle {
		@Override
		public String getEnumItemLiteral(Enum enumVal, Locale locale) {
			return enumVal.name();
		}
	}

	static final class LongJsonSerializer extends JsonSerializer<Long> {
		@Override
		public Class<Long> handledType() {
			return Long.class;
		}

		@Override
		public void serialize(Long value, JsonGenerator gen, SerializerProvider serializers)
				throws IOException, JsonProcessingException {
			if (value.compareTo(BROWSER_SAFE_LONG) > 0) {
				gen.writeString(value.toString());
			} else {
				gen.writeNumber(value);
			}
		}
	}
}
