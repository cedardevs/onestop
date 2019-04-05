package org.cedar.psi.manager.util

import com.vividsolutions.jts.geom.Geometry
import com.vividsolutions.jts.operation.valid.IsValidOp
import com.vividsolutions.jts.operation.valid.TopologyValidationError
import groovy.util.logging.Slf4j
import org.cedar.schemas.avro.psi.Discovery
import org.wololo.jts2geojson.GeoJSONReader

@Slf4j
class ValidateGeometry {
  static Map validateGeometry(Discovery metadata) {
    String geoJson = metadata?.spatialBounding
    GeoJSONReader reader = new GeoJSONReader()

    try {
      Geometry geometry = reader.read(geoJson)
      IsValidOp validOp = new IsValidOp(geometry)
      TopologyValidationError err = validOp.getValidationError()

      if (err != null) {
        throw new IllegalStateException(err.getMessage())
      }

      return ["isValid": true, "error": " "]

    } catch (Exception e) {

      return ["isValid": false, "error": e.getMessage()]
    }
  }
}
