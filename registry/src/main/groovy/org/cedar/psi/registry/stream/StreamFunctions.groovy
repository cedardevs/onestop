package org.cedar.psi.registry.stream

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.apache.kafka.streams.kstream.Reducer
import org.cedar.schemas.avro.psi.Input
import org.cedar.schemas.avro.psi.Method

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

  static Reducer<Input> mergeInputContent = new Reducer<Input>() {
    @Override
    Input apply(Input aggregate, Input nextValue) {
      log.debug("Merging new input $nextValue into existing aggregate ${aggregate}")

      def resultBuilder = Input.newBuilder(aggregate)
      if (nextValue.contentType) {
        resultBuilder.contentType = nextValue.contentType
      }
      if (nextValue.method) {
        resultBuilder.method = nextValue.method
      }

      if (nextValue.content) {
        resultBuilder.content = mergeJsonMapStrings(aggregate.content, nextValue.content)
      }

      return resultBuilder.build()
    }
  }

  static Reducer<Input> replaceInputMethod = new Reducer<Input>() {
    @Override
    Input apply(Input aggregate, Input nextValue) {
      log.debug("${nextValue.method == Method.DELETE ? "Deleting" : "Resurrecting"} existing aggregate ${aggregate}")
      def resultBuilder = Input.newBuilder(aggregate)
      if (nextValue.method) {
        resultBuilder.method = nextValue.method
      }

      return resultBuilder.build()
    }
  }

  static Reducer<Input> reduceInputs = new Reducer<Input>() {
    @Override
    Input apply(Input aggregate, Input nextValue) {
      log.debug("Reducing inputs with method ${nextValue.method}")

      switch (nextValue.method) {
        case Method.PATCH:
          return mergeInputContent.apply(aggregate, nextValue)

        case Method.DELETE:
        case Method.GET:
          return replaceInputMethod.apply(aggregate, nextValue)

        case Method.PUT:
        case Method.POST:
        default:
          return nextValue
      }
    }
  }

  static String mergeJsonMapStrings(String a, String b) {
    def slurper = new JsonSlurper()
    return JsonOutput.toJson((slurper.parseText(a) as Map) + (slurper.parseText(b) as Map))
  }

}
