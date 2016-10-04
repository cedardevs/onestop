import '../specHelper'
import * as actions from '../../src/search/map/MapActions'

describe('The geometry action', function () {

  const geoJSON = {geometry: 'test object'}

  it('set geoJSON', function () {
    const mapAction = actions.newGeometry(geoJSON)
    const expectedAction = { type: 'new_geometry', geoJSON: {geometry: 'test object'} }

    mapAction.should.deep.equal(expectedAction)
  })
})
