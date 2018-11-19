package org.cedar.psi.common.avro

import org.cedar.psi.common.util.MockSchemaRegistrySerde
import spock.lang.Specification


class GeometryAvroSpec extends Specification {

  def testSerde = new MockSchemaRegistrySerde()


  def 'supports points'() {
    def builder = Geometry.newBuilder()
    builder.type = GeometryType.Point
    builder.coordinates = [1.0 as Double, 2 as Double]
    def geometry = builder.build()

    expect:
    geometry instanceof Geometry
    geometry.type == GeometryType.Point
    geometry.coordinates instanceof List
    geometry.coordinates[0] == 1
    geometry.coordinates[1] == 2

    when:
    def bytes = testSerde.serializer().serialize('test', geometry)

    then:
    noExceptionThrown()
  }

  def 'supports lines'() {
    def builder = Geometry.newBuilder()
    builder.type = GeometryType.LineString
    builder.coordinates = [[1 as Double, 2 as Double], [3 as Double, 4 as Double]]
    def geometry = builder.build()

    expect:
    geometry instanceof Geometry
    geometry.coordinates instanceof List
    geometry.coordinates.every { it instanceof List }
    geometry.coordinates[0] == [1, 2]
    geometry.coordinates[1] == [3, 4]

    when:
    def bytes = testSerde.serializer().serialize('test', geometry)

    then:
    noExceptionThrown()
  }

  def 'supports poloygons'() {
    def builder = Geometry.newBuilder()
    builder.type = GeometryType.Polygon
    builder.coordinates = [[[0 as Double, 0 as Double], [1 as Double, 0 as Double], [1 as Double, 1 as Double], [0 as Double, 1 as Double], [0 as Double, 0 as Double]]]
    def geometry = builder.build()

    expect:
    geometry instanceof Geometry
    geometry.coordinates instanceof List
    geometry.coordinates.every {
      it instanceof List && it.every { it instanceof List }
    }
    geometry.coordinates[0] == [[0, 0], [1, 0], [1, 1], [0, 1], [0, 0]]
    geometry.coordinates[0][0] == [0, 0]
    geometry.coordinates[0][1] == [1, 0]
    geometry.coordinates[0][2] == [1, 1]
    geometry.coordinates[0][3] == [0, 1]
    geometry.coordinates[0][4] == [0, 0]

    when:
    def bytes = testSerde.serializer().serialize('test', geometry)

    then:
    noExceptionThrown()
  }

}
