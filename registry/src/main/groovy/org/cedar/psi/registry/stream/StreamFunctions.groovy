package org.cedar.psi.registry.stream

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.apache.kafka.streams.kstream.Reducer
import org.apache.kafka.streams.kstream.ValueJoiner
import org.cedar.schemas.avro.psi.Input

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
      if (nextValue.method) { resultBuilder.method = nextValue.method }

      if (aggregate?.contentType == 'application/json' && nextValue?.contentType == 'application/json' ) {
        resultBuilder.content = mergeJsonMapStrings(aggregate.content, nextValue.content)
      }
      else if (nextValue.content) {
        resultBuilder.content = nextValue.content
      }

      return resultBuilder.build()
    }
  }

  static Reducer<Input> updateInputs = new Reducer<Input>() {
    @Override
    Input apply(Input aggregate, Input nextValue) {
      log.debug("Deleteing existing aggregate ${aggregate} with ${nextValue.method}")
      def resultBuilder = Input.newBuilder(aggregate)
      if (nextValue.method) { resultBuilder.method = nextValue.method }

      return resultBuilder.build()
    }
  }

  static Reducer<Input> publishInputs = new Reducer<Input>() {
    @Override
    Input apply(Input aggregate, Input nextValue) {
      log.debug("Published with method ${nextValue.method}")

      String method = nextValue.method
      if (method == 'PATCH'){return mergeInputs.apply(aggregate,nextValue)}
      if (method == 'PUT' || method == 'POST'){return nextValue}
      if (method == 'DELETE'){return updateInputs.apply(aggregate,nextValue)}
    }
  }

  static String mergeJsonMapStrings(String a, String b) {
    def slurper = new JsonSlurper()
    return JsonOutput.toJson((slurper.parseText(a) as Map) + (slurper.parseText(b) as Map))
  }

}
