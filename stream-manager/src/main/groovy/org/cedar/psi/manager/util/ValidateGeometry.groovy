package org.cedar.psi.manager.util

import org.locationtech.jts.geom.Geometry
import org.locationtech.jts.io.ParseException
import org.locationtech.jts.operation.valid.IsValidOp
import org.locationtech.jts.operation.valid.TopologyValidationError
import groovy.util.logging.Slf4j
import org.cedar.schemas.avro.psi.Discovery
import org.locationtech.jts.io.geojson.GeoJsonReader

@Slf4j
class ValidateGeometry {
  static Map validateGeometry(Discovery metadata) {
    // The avro objects will deserialize to JSON on toString(); in particular spatialBounding will be in
    // a valid GeoJSON format
    String geoJson = metadata?.spatialBounding
    GeoJsonReader reader = new GeoJsonReader()

    try {
      Geometry geometry = reader.read(geoJson)
      IsValidOp validOp = new IsValidOp(geometry)
      TopologyValidationError err = validOp.getValidationError()

      def isValid = err == null ? true : false
      def errorMsg = isValid ? null : err.message

      return ["isValid": isValid, "error": errorMsg]
    }

    catch (ParseException e) {
      // This error parsing is done since JTS is masking their own detailed error messages and all
      // RuntimeExceptions in a generic "can't parse this" way. We're also returning a more specific message
      // when ClassCastExceptions are thrown, instead of the default java message given by JTS
      def message
      if (e.cause instanceof IllegalArgumentException) {
        message = e.cause.message
      }
      else if (e.cause instanceof ClassCastException) {
        message = 'Non-numeric Coordinate'
      }
      else {
        // Just default to the generic JTS message. It's plausible this is unreachable code looking at our use
        // of GeoJsonReader & the source code (https://github.com/locationtech/jts/blob/1.16.x/modules/io/common/src/main/java/org/locationtech/jts/io/geojson/GeoJsonReader.java)
        message = e.message
      }

      return ["isValid": false, "error": message]
    }
  }
}
