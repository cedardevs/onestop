import '../specHelper'
import { map, initialState } from '../../src/search/map/MapReducer'
import { newGeometry, removeGeometry } from '../../src/search/map/MapActions'

describe('The map reducer', function () {
  const validGeoJSON = {
    type: 'Feature',
    geometry: {
      type: 'Polygon',
      coordinates: [[[100.0, 0.0], [101.0, 0.0], [101.0, 1.0], [100.0, 1.0], [100.0, 0.0]]]
    },
    properties: {
      description: 'Valid test GeoJSON'
    }
  }
  it('defaults to the initial state', function () {
    const DOESNT_EXIST = 'DOESNT_EXIST'
    const fakeAction = ()=> {type: DOESNT_EXIST}
    const result = map(initialState, fakeAction)
    result.should.equal(initialState)
  })

  it('updates the state for a new geometry', function () {
    const newGeomAction = newGeometry(validGeoJSON)
    const result = map(initialState, newGeomAction)
    result.should.not.equal(initialState)
  })

  it('defaults back to initial state for geometry removal', function () {
    const removeGeomAction = removeGeometry()
    const result = map(initialState, removeGeomAction)
    result.should.equal(initialState)
  })
})
