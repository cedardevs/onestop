import '../specHelper'
import * as geoUtils from '../../src/utils/geoUtils'

describe('The geoUtils', function () {

  it('can shift a single coordinate', function () {
    const coord = [20, 45]
    geoUtils.shiftCoordinate(coord, 3).should.deep.equal([1100, 45])
    geoUtils.shiftCoordinate(coord, 1).should.deep.equal([380, 45])
    geoUtils.shiftCoordinate(coord, 0).should.deep.equal([20, 45])
    geoUtils.shiftCoordinate(coord, -1).should.deep.equal([-340, 45])
    geoUtils.shiftCoordinate(coord, -3).should.deep.equal([-1060, 45])
  })

  it('can shift an array of coordinates', function () {
    const coords = [[-10, 30], [20, 45]]
    geoUtils.shiftCoordinates(coords, 3).should.deep.equal([[1070, 30], [1100, 45]])
    geoUtils.shiftCoordinates(coords, 1).should.deep.equal([[350, 30], [380, 45]])
    geoUtils.shiftCoordinates(coords, 0).should.deep.equal([[-10, 30], [20, 45]])
    geoUtils.shiftCoordinates(coords, -1).should.deep.equal([[-370, 30], [-340, 45]])
    geoUtils.shiftCoordinates(coords, -3).should.deep.equal([[-1090, 30], [-1060, 45]])
  })

  it('can find how many rotations off-center an array of is', function () {
    geoUtils.findMaxRotations([[1070, 30], [1100, 45]]).should.equal(3)
    geoUtils.findMaxRotations([[350, 30], [380, 45]]).should.equal(1)
    geoUtils.findMaxRotations([[-10, 30], [20, 45]]).should.equal(0)
    geoUtils.findMaxRotations([[-370, 30], [-340, 45]]).should.equal(-1)
    geoUtils.findMaxRotations([[-1090, 30], [-1060, 45]]).should.equal(-3)
  })

  describe('can re-center', function () {

    it('a single-part polygon', function () {
      const input = {
        "type": "Polygon",
        coordinates:[
            [[350, 30], [350, 45], [380, 45], [380, 30], [350, 30]]
        ]
      }
      const expected = {
        "type": "Polygon",
        coordinates: [
          [[-10, 30], [-10, 45], [20, 45], [20, 30], [-10, 30]]
        ]
      }
      const actual = geoUtils.recenterGeometry(input)
      actual.should.deep.equal(expected)
    })

    it('a bbox over the date line after several rotations with decimals', function () {
      const input = {
        "type": "Polygon",
        coordinates: [
            [[-938.358,43.540],[-938.358,55.147],[-874.374,55.147],[-874.374,43.540],[-938.358,43.540]]
        ]
      }
      const expected = {
        "type": "Polygon",
        coordinates: [
          [[-218.358,43.540],[-218.358,55.147],[-154.374,55.147],[-154.374,43.540],[-218.358,43.540]]
        ]
      }
      const actual = geoUtils.recenterGeometry(input)
      actual.type.should.equal(input.type)
      // accommodate floating point precision issues
      // note: 0.0000000001 decimal degrees is approximately 1 micrometer... should be good enough for searching.
      actual.coordinates[0][0][0].should.be.approximately(expected.coordinates[0][0][0], 0.0000000001)
      actual.coordinates[0][1][0].should.be.approximately(expected.coordinates[0][1][0], 0.0000000001)
      actual.coordinates[0][2][0].should.be.approximately(expected.coordinates[0][2][0], 0.0000000001)
      actual.coordinates[0][3][0].should.be.approximately(expected.coordinates[0][3][0], 0.0000000001)
      actual.coordinates[0][4][0].should.be.approximately(expected.coordinates[0][4][0], 0.0000000001)
    })

  })

})
