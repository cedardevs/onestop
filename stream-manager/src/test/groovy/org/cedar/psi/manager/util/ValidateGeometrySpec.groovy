package org.cedar.psi.manager.util

import org.cedar.schemas.avro.geojson.*
import org.cedar.schemas.avro.psi.Discovery
import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class ValidateGeometrySpec extends Specification {
  static final pointCoords = [30D, 10D]
  static final multiPointCoords = [ [-105.01621, 39.57422], [-80.666513, 35.053994] ]
  static final lineStringCoords = [ [30.0, 10.0], [10.0, 30.0], [40.0, 40.0]]
  static final polygonCoords = [
      [ [100, 0], [101, 0], [101, 1], [100, 1], [100, 0] ]
  ]
  static final multiLineStringCoords = [
      [
          [170.0, 45.0], [180.0, 45.0]
      ],
      [
          [-180.0, 45.0], [-170.0, 45.0]
      ]
  ]
  static final polygonWithHoleCoords = [
      [ [35.0, 10.0], [45.0, 45.0], [15.0, 40.0], [10.0, 20.0], [35.0, 10.0] ],
      [ [20.0, 30.0], [35.0, 35.0], [30.0, 20.0], [20.0, 30.0] ]
  ]
  static final multiPolygonCoords = [
      [
          [ [107, 7], [108, 7], [108, 8], [107, 8],[107, 7] ]
      ],
      [
          [ [100, 0], [101, 0], [101, 1], [100, 1],[100, 0] ]
      ]
  ]
  static final multiPolygonWithHolesCoords = [
      [
          [ [40.0, 40.0], [20.0, 45.0], [45.0, 30.0], [40.0, 40.0] ]
      ],
      [
          [ [20.0, 35.0], [10.0, 30.0], [10.0, 10.0], [30.0, 5.0], [45.0, 20.0], [20.0, 35.0] ],
          [ [30.0, 20.0], [20.0, 15.0], [20.0, 25.0], [30.0, 20.0] ]
      ]
  ]
  static final nestedShells = [
      [
          [ [0.0, 0.0], [10.0, 0.0], [10.0, 10.0], [0.0, 10.0], [0.0, 0.0] ]
      ],
      [
          [ [2.0, 2.0], [2.0, 8.0], [8.0, 8.0], [8.0, 2.0], [2.0, 2.0] ],
          [ [3.0, 3.0], [3.0, 7.0], [7.0, 7.0], [7.0, 3.0], [3.0, 3.0] ]
      ]
  ]
  static final ringNotClosed = [
      [
          [ [25.774, -80.190], [18.466, -66.118], [32.321, -64.757] ]
      ],
      [
          [ [28.745, -70.579], [29.570, -67.514], [27.339, -66.668] ]
      ]
  ]
  static final selfIntersection = [
      [ [-69.3, -33.6], [-69.5, -33.8], [-68.9, -33.7], [-69.1, -33.9], [-69.3, -33.6] ]
  ]
  static final duplicateRings = [
      [
          [ [0.0, 0.0], [10.0, 0.0], [10.0, 10.0], [0.0, 10.0], [0.0, 0.0] ]
      ],
      [
          [ [0.0, 0.0], [10.0, 0.0], [10.0, 10.0], [0.0, 10.0], [0.0, 0.0] ]
      ]
  ]
  static final disconnectedInterior = [
      [ [0.0, 0.0], [10.0, 0.0], [10.0, 10.0], [0.0, 10.0], [0.0, 0.0] ],
      [ [5.0, 0.0], [10.0, 5.0], [5.0, 10.0], [0.0, 5.0], [5.0, 0.0] ]
  ]
  static final InvalidCoordinate = [Double.NaN, 3.0]
  static final TooFewPoints = [[1.0, 3.0], [1.0, 3.0]]
  static final holeLiesOutsideShell = [
      [ [0.0, 0.0], [10.0, 10.0], [10.0, 0.0], [0.0, 0.0] ],
      [ [20.0, 30.0], [35.0, 35.0], [30.0, 20.0], [20.0, 30.0] ]
  ]

  def "valid #testCase instance"() {
    given:
    def metadata = Discovery.newBuilder().setSpatialBounding(value).build()

    when:
    def result = ValidateGeometry.validateGeometry(metadata)

    then:
    result.isValid == expected

    where:
    testCase                | value                                                     | expected
    'Point'                 | buildGeoJson('Point', pointCoords)                        | true
    'MultiPoint'            | buildGeoJson('MultiPoint', multiPointCoords)              | true
    'LineString'            | buildGeoJson('LineString', lineStringCoords)              | true
    'MultiLineString'       | buildGeoJson('MultiLineString', multiLineStringCoords)    | true
    'Polygon'               | buildGeoJson('Polygon', polygonCoords)                    | true
    'MultiPolygon'          | buildGeoJson('MultiPolygon', multiPolygonCoords)          | true
    'PolygonWithHole'       | buildGeoJson('Polygon', polygonWithHoleCoords)            | true
    'MultiPolygonWithHoles' | buildGeoJson('MultiPolygon', multiPolygonWithHolesCoords) | true
    'MultiPolygonWithHoles' | buildGeoJson('MultiPolygon', multiPolygonWithHolesCoords) | true
  }

  def "#testCase"() {
    given:
    def metadata = Discovery.newBuilder().setSpatialBounding(value).build()

    when:
    def result = ValidateGeometry.validateGeometry(metadata)

    then:
    result.error == expected

    where:
    testCase                | value                                           | expected
    'Invalid Coordinate'    | buildGeoJson('Point', InvalidCoordinate )       | "Invalid Coordinate"
    'Duplicated Rings'      | buildGeoJson('MultiPolygon', duplicateRings)    | 'Duplicate Rings'
    'Too Few Points'        | buildGeoJson('LineString', TooFewPoints)        | "Too few distinct points in geometry component"
    'Nested Shells'         | buildGeoJson('MultiPolygon', nestedShells)      | 'Nested shells'
    'Ring Not Closed'       | buildGeoJson('MultiPolygon', ringNotClosed)     | 'Points of LinearRing do not form a closed linestring'
    'Self Intersection'     | buildGeoJson('Polygon', selfIntersection)       | 'Self-intersection'
    'disconnected Interior' | buildGeoJson('Polygon', disconnectedInterior)   | 'Interior is disconnected'
    'hole Lies Outside Shell' | buildGeoJson('Polygon', holeLiesOutsideShell) | 'Hole lies outside shell'
  }

  static buildGeoJson(String type, ArrayList coor) {
    switch (type) {
      case "Point":
        Point.newBuilder()
            .setCoordinates(coor)
            .build()
        break
      case "MultiPoint":
        MultiPoint.newBuilder()
            .setCoordinates(coor)
            .build()
        break
      case "LineString":
        LineString.newBuilder()
            .setCoordinates(coor)
            .build()
        break
      case "MultiLineString":
        MultiLineString.newBuilder()
            .setCoordinates(coor)
            .build()
        break
      case "Polygon":
        Polygon.newBuilder()
            .setCoordinates(coor)
            .build()
        break
      case "MultiPolygon":
        MultiPolygon.newBuilder()
            .setCoordinates(coor)
            .build()
        break
      default:
        "unknown type"
        break
    }
  }

}
