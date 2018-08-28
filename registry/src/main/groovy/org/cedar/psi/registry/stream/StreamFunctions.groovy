package org.cedar.psi.registry.stream

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.apache.kafka.streams.kstream.Reducer
import org.apache.kafka.streams.kstream.ValueJoiner
import org.apache.kafka.streams.kstream.ValueMapperWithKey

@Slf4j
@CompileStatic
class StreamFunctions {

  static Reducer identityReducer = new Reducer() {
    @Override
    Object apply(Object aggregate, Object nextValue) {
      nextValue
    }
  }

  static Reducer<Map> mergeMaps = new Reducer<Map>() {
    @Override
    Map apply(Map aggregate, Map nextValue) {
      log.debug("Merging new value $nextValue into existing aggregate ${aggregate}")
      def slurper = new JsonSlurper()
      def result = (aggregate ?: [:]) + (nextValue ?: [:])
      if(aggregate?.contentType == 'application/json' && nextValue?.contentType == 'application/json' ){
        result.content = JsonOutput.toJson(
            (slurper.parseText(aggregate.content as String) as Map ) +
                (slurper.parseText(nextValue.content as String) as Map )
        )
      }
      return result
    }
  }

  /**
   * Returns a ValueJoiner which returns json with the left value under the given left key
   * and the right value under the given right key. For example:
   *
   * def joiner = buildKeyedMapJoiner('left', 'right')
   * joiner.apply(["hello": "world"[, ["answer": 42])
   * >> ["left": ["hello": "world"], "right": ["answer": 42]]
   *
   * @param leftKey  The key to put the left value under
   * @param rightKey The key to put the right value under
   * @return         The combined result
   */
  static ValueJoiner<Map, Map, Map> buildKeyedMapJoiner(String leftKey, String rightKey) {
    return new ValueJoiner<Map, Map, Map>() {
      @Override
      Map apply(Map leftValue, Map rightValue) {
        log.debug("Joining left value ${leftValue} with right value ${rightValue}")
        def result = [(leftKey): leftValue, (rightKey): rightValue]
        return result
      }
    }
  }

  /**
   * Returns a ValueJoiner which returns json with the left value under the given left key
   * and merged into the right value.
   *
   * def joiner = buildKeyedMapJoiner('left')
   * joiner.apply(["hello": "world"], ["answer": 42])
   * >> '["left": ["hello": "world"], "answer": 42]
   *
   * @param leftKey  The key to put the left value under
   * @param rightKey The key to put the right value under
   * @return         The combined result
   */
  static ValueJoiner<Map, Map, Map> buildKeyedMapJoiner(String leftKey) {
    return new ValueJoiner<Map, Map, Map>() {
      @Override
      Map apply(Map leftValue, Map rightValue) {
        log.debug("Joining left value ${leftValue} with right value ${rightValue}")
        Map result = [(leftKey): leftValue] + (rightValue ?: [:])
        return result
      }
    }
  }

  static ValueMapperWithKey<String, Map, Map> parsedInfoNormalizer = new ValueMapperWithKey<String, Map, Map>() {
    @Override
    Map apply(String readOnlyKey, Map value) {
      def result = value ?: [:]
      if (!result.containsKey('publishing')) {
        result.put('publishing', ['private': false])
      }
      return result
    }
  }

}
