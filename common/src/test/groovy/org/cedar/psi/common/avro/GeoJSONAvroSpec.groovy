package org.cedar.psi.common.avro

import org.cedar.psi.common.util.MockSchemaRegistrySerde
import org.apache.kafka.common.errors.SerializationException
import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class GeoJSONAvroSpec extends Specification {

  static final pointCoords = [30D, 10D]
  static final multiPointCoords = [[10D, 40D], [40D, 30D], [20D, 20D], [30D, 10D]]
  static final lineStringCoords = [[30D, 10D], [10D, 30D], [40D, 40D]]
  static final multiLineStringCoords = [
      [[10D, 10D], [20D, 20D], [10D, 40D]],
      [[40D, 40D], [30D, 30D], [40D, 20D], [30D, 10D]]
  ]
  static final polygonCoords = [
      [[30D, 10D], [40D, 40D], [20D, 40D], [10D, 20D], [30D, 10D]]
  ]
  static final polygonWithHoleCoords = [
      [[35D, 10D], [45D, 45D], [15D, 40D], [10D, 20D], [35D, 10D]],
      [[20D, 30D], [35D, 35D], [30D, 20D], [20D, 30D]]
  ]
  static final multiPolygonCoords = [
      [
          [[30D, 20D], [45D, 40D], [10D, 40D], [30D, 20D]]
      ],
      [
          [[15D, 5D], [40D, 10D], [10D, 20D], [5D, 10D], [15D, 5D]]
      ]
  ]
  static final multiPolygonWithHolesCoords = [
      [
          [[40D, 40D], [20D, 45D], [45D, 30D], [40D, 40D]]
      ],
      [
          [[20D, 35D], [10D, 30D], [10D, 10D], [30D, 5D], [45D, 20D], [20D, 35D]],
          [[30D, 20D], [20D, 15D], [20D, 25D], [30D, 20D]]
      ]
  ]

  def testSerde = new MockSchemaRegistrySerde()

  def "#testType successfully serializes when coordinates true to type"() {
    when:
    def builder = testBuilder
    builder.type = testType
    builder.coordinates = testCoordinates
    def geometry = builder.build()
    testSerde.serializer().serialize('test', geometry)

    then:
    noExceptionThrown()

    where:
    testType                            | testBuilder                  | testCoordinates
    PointType.Point                     | Point.newBuilder()           | pointCoords
    MultiPointType.MultiPoint           | MultiPoint.newBuilder()      | multiPointCoords
    LineStringType.LineString           | LineString.newBuilder()      | lineStringCoords
    MultiLineStringType.MultiLineString | MultiLineString.newBuilder() | multiLineStringCoords
    PolygonType.Polygon                 | Polygon.newBuilder()         | polygonCoords
    PolygonType.Polygon                 | Polygon.newBuilder()         | polygonWithHoleCoords
    MultiPolygonType.MultiPolygon       | MultiPolygon.newBuilder()    | multiPolygonCoords
    MultiPolygonType.MultiPolygon       | MultiPolygon.newBuilder()    | multiPolygonWithHolesCoords
  }

  def "#testType won't serialize when coordinates not true to type"() {
    when:
    def builder = testBuilder
    builder.type = testType
    builder.coordinates = testCoordinates
    def geometry = builder.build()
    testSerde.serializer().serialize('test', geometry)

    then:
    thrown(SerializationException)

    where:
    // Coordinates formats same for LineString & MultiPoint and Polygon & MultiLineString types
    testType        | testBuilder        | testCoordinates
    PointType.Point | Point.newBuilder() | multiPointCoords
    PointType.Point | Point.newBuilder() | polygonWithHoleCoords
    PointType.Point | Point.newBuilder() | multiPolygonCoords
    // TODO finish type combos
  }

  def "#testType represents valid correlated GeoJSON"() {
    // TODO
  }

}
