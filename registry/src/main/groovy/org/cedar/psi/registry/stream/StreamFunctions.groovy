package org.cedar.psi.registry.stream

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.apache.kafka.streams.kstream.Reducer
import org.apache.kafka.streams.kstream.ValueJoiner

@Slf4j
@CompileStatic
class StreamFunctions {

  static Reducer<String> reduceJsonStrings = new Reducer<String>() {
    @Override
    String apply(String aggregate, String newValue) {
      log.debug("Merging new value $newValue into existing aggregate ${aggregate}")
      def slurper = new JsonSlurper()
      def slurpedAggregate = aggregate ? slurper.parseText(aggregate as String) as Map : [:]
      def slurpedNewValue = slurper.parseText(newValue as String) as Map
      def result = slurpedAggregate + slurpedNewValue
      return JsonOutput.toJson(result)
    }
  }

  /**
   * Returns a ValueJoiner which returns json with the left value under the given left key
   * and the right value under the given right key. For example:
   *
   * def joiner = buildKeyedJsonJoiner('left', 'right')
   * joiner.apply('{"hello": "world"}', '{"answer": 42}')
   * >> '{"left": {"hello": "world"}, "right": {"answer": 42}}'
   *
   * @param leftKey  The key to put the left value under
   * @param rightKey The key to put the right value under
   * @return         The combined result
   */
  static ValueJoiner<String, String, String> buildKeyedJsonJoiner(String leftKey, String rightKey) {
    return new ValueJoiner<String, String, String>() {
      @Override
      String apply(String leftValue, String rightValue) {
        log.debug("Joining left value $leftValue with right value ${rightValue}")
        def slurper = new JsonSlurper()
        def leftSlurped = leftValue ? slurper.parseText(leftValue) as Map : null
        def rightSlurped = rightValue ? slurper.parseText(rightValue) as Map : null
        def result = [(leftKey): leftSlurped, (rightKey): rightSlurped]
        return JsonOutput.toJson(result)
      }
    }
  }

}
