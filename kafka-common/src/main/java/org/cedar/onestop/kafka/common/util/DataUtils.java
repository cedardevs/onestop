package org.cedar.onestop.kafka.common.util;

import org.cedar.onestop.data.util.ListUtils;
import org.cedar.schemas.avro.psi.AggregatedInput;
import org.cedar.schemas.avro.psi.ErrorEvent;
import org.cedar.schemas.avro.psi.ParsedRecord;
import org.cedar.schemas.avro.util.AvroUtils;

import java.lang.reflect.Method;
import java.util.*;

@SuppressWarnings("unchecked")
public class DataUtils {

  /**
   * @param builderType   type of schema builder either ParsedRecord or AggregatedInput, otherwise error out
   * @param fieldData     parsed or input metadata values
   * @param fieldsToParse list of schema fields that only support merged map
   *
   * @throws ClassCastException if builderType is not of ParsedRecord.Builder or AggregatedInput.Builder
   */
  public static void updateDerivedFields(Object builderType, Map<String, Object> fieldData, List fieldsToParse) {
    // 1. identify the builder type
    var aggregatedSchemaType = builderType instanceof AggregatedInput.Builder;
    var schema = aggregatedSchemaType ? AggregatedInput.getClassSchema() : ParsedRecord.getClassSchema();
    var builder = aggregatedSchemaType ? (AggregatedInput.Builder) builderType : (ParsedRecord.Builder) builderType;

    fieldData
        .entrySet()
        .stream()
        .filter(e -> fieldsToParse.contains(e.getKey()))
        .forEach(e -> {
          try {
            // 2. coerce the value to its field value and apply it to the builder
            var field = schema.getField(e.getKey());
            var coerced = AvroUtils.coerceValueForSchema(e.getValue(), field.schema());
            setValueOnPojo(builder, field.name(), coerced);
          }
          catch (Exception ex) {
            // 3. if it fails attach an error to the builder, but continue with remaining fields
            var error = ErrorEvent.newBuilder()
                .setTitle("Failed to parse field [" + e.getKey() + "]")
                .setDetail(ex.getMessage())
                .build();
            if (aggregatedSchemaType) {
              var aggregatedInputBuilder = (AggregatedInput.Builder) builder;
              aggregatedInputBuilder.setErrors(ListUtils.addOrInit(aggregatedInputBuilder.getErrors(), error));
            }
            else {
              var parsedBuilder = (ParsedRecord.Builder) builder;
              parsedBuilder.setErrors(ListUtils.addOrInit(parsedBuilder.getErrors(), error));
            }
          }
        });
  }

  /**
   *
   * @param pojo schema builder type
   * @param fieldName schema field name
   * @param value metadata values that need to be updated
   * @param <T> type of pojo
   * @return it returns the new updated metadata value otherwise, throws unsupported operation
   */
  public static <T extends Object> T setValueOnPojo(T pojo, String fieldName, Object value) {
    try {
      var setter = findSetterForValue(pojo, fieldName, value);
      if (setter.isPresent()) {
        setter.get().invoke(pojo, value);
        return pojo;
      }
      else {
        throw new UnsupportedOperationException("Unable to find a setter for field [" + fieldName +
            "] on builder [" + pojo + "] accepting parameter type [" + value.getClass() + "]");
      }
    } catch (Exception e) {
      throw new UnsupportedOperationException("Unable to set value for field [" + fieldName +
          "] on builder [" + pojo + "]", e);
    }
  }

  /**
   *
   * @param pojo schema builder type
   * @param fieldName schema field name
   * @param value metadata values that need to be updated
   * @return returns the builder setter name
   */
  private static Optional<Method> findSetterForValue(Object pojo, String fieldName, Object value) {
    var setterName = "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
    return Arrays.stream(pojo.getClass().getMethods())
        .filter(m -> m.getName().equals(setterName))
        .filter(m -> m.getParameterCount() == 1)
        .filter(m -> m.getParameterTypes()[0].isAssignableFrom(value.getClass()))
        .findFirst();
  }

  /**
   *
   * @param pojo schema builder type
   * @param fieldName schema field name
   * @param clazz schema class name
   * @return returns the builder getter name
   */
  private static Optional<Method> findGetterForType(Object pojo, String fieldName, Class clazz) {
    var getterName = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
    return Arrays.stream(pojo.getClass().getMethods())
        .filter(m -> m.getName().equals(getterName))
        .filter(m -> m.getParameterCount() == 0)
        .filter(m -> m.getReturnType().isAssignableFrom(clazz))
        .findFirst();
  }

  // Helper functions:
  public static Properties filterProperties(Map kafkaConfigMap, Set<String> keySet) {
    var props = new Properties();
    kafkaConfigMap.forEach( (k, v) -> {
      if(keySet.contains(k)) {
        props.put(k, v);
      }
    });
    return props;
  }
}
