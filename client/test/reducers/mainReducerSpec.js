import '../specHelper'
import Immutable from 'seamless-immutable'
import reducer from '../../src/reducers/reducer'


describe('The main reducer', function() {

  it('returns a composed initial state', function() {
    const initialState = new Map()
    const initialAction = {type: 'init'}
    const result = reducer(initialState, initialAction)

    result.should.be.an.instanceOf(Object)
    result.domain.should.be.an.instanceOf(Object)
    result.behavior.should.be.an.instanceOf(Object)
    result.behavior.search.should.be.an.instanceOf(Object)
    result.ui.should.be.an.instanceOf(Object)
    result.errors.should.be.an.instanceOf(Object)
    result.routing.should.be.an.instanceOf(Object)
  })
})
