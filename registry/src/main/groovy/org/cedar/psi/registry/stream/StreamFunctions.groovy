package org.cedar.psi.registry.stream

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.apache.kafka.streams.kstream.Reducer
import org.apache.kafka.streams.kstream.ValueJoiner
import org.cedar.psi.common.avro.Input

@Slf4j
@CompileStatic
class StreamFunctions {

  static Reducer identityReducer = new Reducer() {
    @Override
    Object apply(Object aggregate, Object nextValue) {
      nextValue
    }
  }

  static Reducer<Set> setReducer = new Reducer<Set>() {
    @Override
    Set apply(Set aggregate, Set nextValue) {
      if (nextValue == null) {
        return null // if we get a tombstone, tomestone the whole set
      }
      else {
        aggregate.addAll(nextValue)
        return aggregate
      }
    }
  }

  static Reducer<Map> mergeContentMaps = new Reducer<Map>() {
    @Override
    Map apply(Map aggregate, Map nextValue) {
      log.debug("Merging new value $nextValue into existing aggregate ${aggregate}")
      def slurper = new JsonSlurper()
      def result = (aggregate ?: [:]) + (nextValue ?: [:])
      if (aggregate?.contentType == 'application/json' && nextValue?.contentType == 'application/json' ) {
        result.content = mergeJsonMapStrings(aggregate?.content as String, nextValue?.content as String)
      }
      return result
    }
  }

  static Reducer<Input> mergeInputs = new Reducer<Input>() {
    @Override
    Input apply(Input aggregate, Input nextValue) {
      log.debug("Merging new input $nextValue into existing aggregate ${aggregate}")

      def resultBuilder = Input.newBuilder(aggregate)
      if (nextValue.contentType) { resultBuilder.contentType = nextValue.contentType }
      if (nextValue.host) { resultBuilder.host = nextValue.host }
      if (nextValue.method) { resultBuilder.method = nextValue.method }
      if (nextValue.protocol) { resultBuilder.protocol = nextValue.protocol }
      if (nextValue.requestUrl) { resultBuilder.requestUrl = nextValue.requestUrl }

      if (aggregate?.contentType == 'application/json' && nextValue?.contentType == 'application/json' ) {
        resultBuilder.content = mergeJsonMapStrings(aggregate.content, nextValue.content)
      }
      else if (nextValue.content) {
        resultBuilder.content = nextValue.content
      }

      return resultBuilder.build()
    }
  }

  static String mergeJsonMapStrings(String a, String b) {
    def slurper = new JsonSlurper()
    return JsonOutput.toJson((slurper.parseText(a) as Map) + (slurper.parseText(b) as Map))
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
}
