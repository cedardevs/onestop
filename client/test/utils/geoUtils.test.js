import * as geoUtils from '../../src/utils/geoUtils'

describe('The geoUtils', function(){
  it('Query shifts bbox coordinates', function(){
    // query version shifts the west coordinate
    let queryCoordinates = geoUtils.convertBboxToQueryGeoJson(120, 0, 7, 63)
      .geometry.coordinates
    expect(queryCoordinates).toEqual([
      [ [ -240, 0 ], [ 7, 0 ], [ 7, 63 ], [ -240, 63 ], [ -240, 0 ] ],
    ])
    // regular bbox to geojson (such as for drawing on a map) does not
    let coordinates = geoUtils.convertBboxToGeoJson(120, 0, 7, 63).geometry
      .coordinates
    expect(coordinates).toEqual([
      [ [ 120, 0 ], [ 7, 0 ], [ 7, 63 ], [ 120, 63 ], [ 120, 0 ] ],
    ])
  })

  it('Query shifts bbox coordinates within API longitude bounds', function(){
    // shifts the east coordinate instead of the west
    // this is important, because longitudes outside the bounds of |360| are invalid in the API
    let queryCoordinates = geoUtils.convertBboxToQueryGeoJson(-73, -50, -83, 43)
      .geometry.coordinates
    expect(queryCoordinates).toEqual([
      [ [ -73, -50 ], [ 277, -50 ], [ 277, 43 ], [ -73, 43 ], [ -73, -50 ] ],
    ])
  })

  describe('single coordinate', function(){
    const coord = [ 20, 45 ]

    it('shifts 3 rotations', function(){
      expect(geoUtils.shiftCoordinate(coord, 3)).toEqual([ 1100, 45 ])
    })
    it('shifts 1 rotation', function(){
      expect(geoUtils.shiftCoordinate(coord, 1)).toEqual([ 380, 45 ])
    })
    it('shifts 0 rotations', function(){
      expect(geoUtils.shiftCoordinate(coord, 0)).toEqual([ 20, 45 ])
    })
    it('shifts -1 rotations', function(){
      expect(geoUtils.shiftCoordinate(coord, -1)).toEqual([ -340, 45 ])
    })
    it('shifts -3 rotations', function(){
      expect(geoUtils.shiftCoordinate(coord, -3)).toEqual([ -1060, 45 ])
    })
  })

  describe('an array of coordinates', function(){
    const coords = [ [ -10, 30 ], [ 20, 45 ] ]
    it('shifts 3 rotations', function(){
      expect(geoUtils.shiftCoordinates(coords, 3)).toEqual([
        [ 1070, 30 ],
        [ 1100, 45 ],
      ])
    })
    it('shifts 1 rotation', function(){
      expect(geoUtils.shiftCoordinates(coords, 1)).toEqual([
        [ 350, 30 ],
        [ 380, 45 ],
      ])
    })
    it('shifts 0 rotations', function(){
      expect(geoUtils.shiftCoordinates(coords, 0)).toEqual([
        [ -10, 30 ],
        [ 20, 45 ],
      ])
    })
    it('shifts -1 rotations', function(){
      expect(geoUtils.shiftCoordinates(coords, -1)).toEqual([
        [ -370, 30 ],
        [ -340, 45 ],
      ])
    })
    it('shifts -3 rotations', function(){
      expect(geoUtils.shiftCoordinates(coords, -3)).toEqual([
        [ -1090, 30 ],
        [ -1060, 45 ],
      ])
    })
  })

  describe('findMaxRotations', function(){
    it('can find 3 rotations for an off-center array', function(){
      expect(geoUtils.findMaxRotations([ [ 1070, 30 ], [ 1100, 45 ] ])).toBe(3)
    })
    it('can find 1 rotation for an off-center array', function(){
      expect(geoUtils.findMaxRotations([ [ 350, 30 ], [ 380, 45 ] ])).toBe(1)
    })
    it('can find 0 rotations for an off-center array (small region)', function(){
      expect(geoUtils.findMaxRotations([ [ -10, 30 ], [ 20, 45 ] ])).toBe(0)
    })
    it('can find 0 rotations for an off-center array (large region))', function(){
      expect(geoUtils.findMaxRotations([ [ -180, -90 ], [ 180, 90 ] ])).toBe(0)
    })
    it('can find -1 rotations for an off-center array', function(){
      expect(geoUtils.findMaxRotations([ [ -370, 30 ], [ -340, 45 ] ])).toBe(-1)
    })
    it('can find -3 rotations for an off-center array', function(){
      expect(geoUtils.findMaxRotations([ [ -1090, 30 ], [ -1060, 45 ] ])).toBe(
        -3
      )
    })
  })

  describe('can re-center', function(){
    it('a single-part polygon', function(){
      const input = {
        type: 'Polygon',
        coordinates: [
          [ [ 350, 30 ], [ 350, 45 ], [ 380, 45 ], [ 380, 30 ], [ 350, 30 ] ],
        ],
      }
      const expected = {
        type: 'Polygon',
        coordinates: [
          [ [ -10, 30 ], [ -10, 45 ], [ 20, 45 ], [ 20, 30 ], [ -10, 30 ] ],
        ],
      }
      expect(geoUtils.recenterGeometry(input)).toEqual(expected)
    })

    it('a bbox over the date line after several rotations with decimals', function(){
      const input = {
        type: 'Polygon',
        coordinates: [
          [
            [ -938.358, 43.54 ],
            [ -938.358, 55.147 ],
            [ -874.374, 55.147 ],
            [ -874.374, 43.54 ],
            [ -938.358, 43.54 ],
          ],
        ],
      }
      const expected = {
        type: 'Polygon',
        coordinates: [
          [
            [ 141.642, 43.54 ],
            [ 141.642, 55.147 ],
            [ 205.626, 55.147 ],
            [ 205.626, 43.54 ],
            [ 141.642, 43.54 ],
          ],
        ],
      }
      const actual = geoUtils.recenterGeometry(input)
      expect(actual.type).toBe(input.type)
      // accommodate floating point precision issues
      // note: 0.0000000001 decimal degrees is approximately 1 micrometer... should be good enough for searching.
      expect(actual.coordinates[0][0][0]).toBeCloseTo(
        expected.coordinates[0][0][0],
        0.0000000001
      )
      expect(actual.coordinates[0][1][0]).toBeCloseTo(
        expected.coordinates[0][1][0],
        0.0000000001
      )
      expect(actual.coordinates[0][2][0]).toBeCloseTo(
        expected.coordinates[0][2][0],
        0.0000000001
      )
      expect(actual.coordinates[0][3][0]).toBeCloseTo(
        expected.coordinates[0][3][0],
        0.0000000001
      )
      expect(actual.coordinates[0][4][0]).toBeCloseTo(
        expected.coordinates[0][4][0],
        0.0000000001
      )
    })
  })
})
