import '../specHelper'
import Immutable from 'immutable'
import reducer from '../../src/reducer'


describe('The main reducer', function() {

  it('returns a composed initial state', function() {
    const initialState = Immutable.Map()
    const initialAction = {type: 'init'}
    const result = reducer(initialState, initialAction)

    result.should.be.an.instanceOf(Immutable.Map)
    result.get('search').should.be.an.instanceOf(Immutable.Map)
    result.get('facets').should.be.an.instanceOf(Immutable.Map)
    result.get('collections').should.be.an.instanceOf(Object)
    result.get('details').should.be.an.instanceOf(Immutable.Map)
    result.get('routing').should.be.an.instanceOf(Immutable.Map)
  })
})
