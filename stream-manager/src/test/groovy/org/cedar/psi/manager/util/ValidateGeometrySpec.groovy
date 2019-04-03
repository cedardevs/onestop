package org.cedar.psi.manager.util

import org.cedar.schemas.avro.geojson.*
import org.cedar.schemas.avro.psi.Discovery
import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class ValidateGeometrySpec extends Specification {
  static final pointCoords = [30.0, 10.0]
  static final multiPointCoords = [
      [100.0, 0.0], [101.0, 1.0]
  ]
  static final lineStringCoords = [
      [30.0, 10.0], [10.0, 30.0], [40.0, 40.0]
  ]
  static final multiLineStringCoords = [
      [
          [1.0, 1.0], [3.0, 3.0]
      ],
      [
          [102.0, 2.0], [103.0, 3.0]
      ]
  ]
  static final polygonCoords = [
      [
          [0.0, 0.0], [10.0, 10.0], [10.0, 0.0], [0.0, 0.0]
      ]
  ]
  static final polygonWithHoleCoords = [
      [
          [[0.0, 0.0], [10.0, 10.0], [10.0, 0.0], [0.0, 0.0]]
      ],
      [
          [[20.0, 30.0], [35.0, 35.0], [30.0, 20.0], [20.0, 30.0]]
      ]
  ]

  static final multiPolygonCoords = [
      [
          [[102.0, 2.0], [103.0, 2.0], [103.0, 3.0], [102.0, 3.0], [102.0, 2.0]]
      ],
      [
          [[100.0, 0.0], [101.0, 0.0], [101.0, 1.0], [100.0, 1.0], [100.0, 0.0]],
          [[100.2, 0.2], [100.8, 0.2], [100.8, 0.8], [100.2, 0.8], [100.2, 0.2]]
      ]
  ]
  static final multiPolygonWithHolesCoords = [
      [
          [[40.0, 40.0], [20.0, 45.0], [45.0, 30.0], [40.0, 40.0]]
      ],
      [
          [[20.0, 35.0], [10.0, 30.0], [10.0, 10.0], [30.0, 5.0], [45.0, 20.0], [20.0, 35.0]],
          [[30.0, 20.0], [20.0, 15.0], [20.0, 25.0], [30.0, 20.0]]
      ]
  ]


  static final nestedShells = [
      [
          [[0.0, 0.0], [10.0, 0.0], [10.0, 10.0], [0.0, 10.0], [0.0, 0.0]]
      ],
      [
          [[2.0, 2.0], [2.0, 8.0], [8.0, 8.0], [8.0, 2.0], [2.0, 2.0]],
          [[3.0, 3.0], [3.0, 7.0], [7.0, 7.0], [7.0, 3.0], [3.0, 3.0]]
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
      [[-69.3, -33.6], [-69.5, -33.8], [-68.9, -33.7], [-69.1, -33.9], [-69.3, -33.6]]
  ]

  static final duplicateRings =[
      [
          [[0.0, 0.0], [10.0, 0.0], [10.0, 10.0], [0.0, 10.0], [0.0, 0.0]]
      ],
      [
          [[0.0, 0.0], [10.0, 0.0], [10.0, 10.0],[0.0, 10.0], [0.0, 0.0]]
      ]
  ]

  static final holeOutsideShell =[
      [
          [[0.0, 0.0], [10.0, 0.0], [10.0, 10.0], [0.0, 10.0], [0.0, 0.0]]
      ],
      [
          [[0.0, 0.0], [10.0, 0.0], [10.0, 10.0],[0.0, 10.0], [0.0, 0.0]]
      ]
  ]

  def "valid #testCase instance"() {
    given:
    def metadata = Discovery.newBuilder().setSpatialBounding(value).build()

    when:
    def result = ValidateGeometry.validateGeometry(metadata)

    then:
    result == expected

    where:
    testCase                | value                        | expected
    'Point'                 | buildPoint()                 | 'validGeoJSON : true'
    'MultiPoint'            | buildMultiPoint()            | 'validGeoJSON : true'
    'LineString'            | buildLineString()            | 'validGeoJSON : true'
    'MultiLineString'       | buildMultiLineString()       | 'validGeoJSON : true'
    'Polygon'               | buildPolygon()               | 'validGeoJSON : true'
    'MultiPolygon'          | buildMultiPolygon()          | 'validGeoJSON : true'
    'PolygonWithHole'       | buildPolygonWithHole()       | 'validGeoJSON : true'
    'MultiPolygonWithHoles' | buildMultiPolygonWithHoles() | 'validGeoJSON : true'
  }

  def "#testCase"() {
    given:
    def metadata = Discovery.newBuilder().setSpatialBounding(value).build()

    when:
    def result = ValidateGeometry.validateGeometry(metadata)

    then:
    result == expected

    where:
    testCase                     | value                      | expected
    'Invalid Coordinate'    | buildInvalidCoordinate()    | "error : Invalid Coordinate"
    'Duplicated Rings'      | buildDuplicatedRings()      | 'error : Duplicate Rings'
    'Too Few Points'        | buildTooFewPoints()         | "error : Too few distinct points in geometry component"
    'Nested Shells'         | buildNastedShells()         | 'error : Nested shells'
    'Ring Not Closed'       | buildRingNotClosed()        | 'error : Points of LinearRing do not form a closed linestring'
    'Self Intersection'     | buildSelfIntersection()     | 'error : Self-intersection'
    //TODO
//    'Hole Outside Shell'    | buildHoleOutsideShell()     | 'error : null'
//    'Disconnected Interior' | buildHoleOutsideShell()     | 'error : null'

  }

  // valid geometry
  static buildPoint() {
    Point.newBuilder()
        .setCoordinates(pointCoords)
        .build()
  }

  static buildMultiPoint() {
    MultiPoint.newBuilder()
        .setCoordinates(multiPointCoords)
        .build()
  }

  static buildLineString() {
    LineString.newBuilder()
        .setCoordinates(lineStringCoords)
        .build()
  }

  static buildMultiLineString() {
    MultiLineString.newBuilder()
        .setCoordinates(multiLineStringCoords)
        .build()
  }
  static buildPolygon() {
    Polygon.newBuilder()
        .setCoordinates(polygonCoords)
        .build()
  }

  static buildMultiPolygon() {
    MultiPolygon.newBuilder()
        .setCoordinates(multiPolygonCoords)
        .build()
  }

  static buildPolygonWithHole() {
    MultiPolygon.newBuilder()
        .setCoordinates(polygonWithHoleCoords)
        .build()
  }

  static buildMultiPolygonWithHoles() {
    MultiPolygon.newBuilder()
        .setCoordinates(multiPolygonWithHolesCoords)
        .build()
  }

  // invalid geometry
  static buildInvalidCoordinate() {
    Point.newBuilder()
        .setCoordinates([Double.NaN, 3.0])
        .build()
  }

  static buildDuplicatedRings() {
    MultiPolygon.newBuilder()
        .setCoordinates(duplicateRings)
        .build()
  }

  static buildTooFewPoints() {
    Polygon.newBuilder()
        .setCoordinates([[[0.0, 0.0], [10.0, 10.0], [10.0, 10.0], [0.0, 0.0]]])
        .build()

  }
  static buildNastedShells() {
    MultiPolygon.newBuilder()
        .setCoordinates(nestedShells)
        .build()
  }

  static buildHoleOutsideShell() {
    MultiPolygon.newBuilder()
        .setCoordinates(holeOutsideShell)
        .build()

  }

  static buildSelfIntersection() {
    Polygon.newBuilder()
        .setCoordinates(selfIntersection)
        .build()

  }

  static buildRingNotClosed() {
    MultiPolygon.newBuilder()
        .setCoordinates(ringNotClosed)
        .build()
  }

}
