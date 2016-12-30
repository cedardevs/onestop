import '../specHelper'
import Immutable from 'seamless-immutable'
import reducer from '../../src/reducer'


describe('The main reducer', function() {

  it('returns a composed initial state', function() {
    const initialState = new Map()
    const initialAction = {type: 'init'}
    const result = reducer(initialState, initialAction)

    result.should.be.an.instanceOf(Object)
    result.search.should.be.an.instanceOf(Object)
    result.facets.should.be.an.instanceOf(Object)
    result.collections.should.be.an.instanceOf(Object)
    result.details.should.be.an.instanceOf(Object)
    result.routing.should.be.an.instanceOf(Object)
  })
})
