import '../../specHelper'
import Immutable from 'seamless-immutable'
import { granules, initialState } from '../../../src/reducers/appState/granuleRequest'
import { fetchingGranules, fetchedGranules } from '../../../src/result/granules/GranulesActions'

describe('The granuleRequest reducer', function() {
  it('has a default state', function () {
    const initialAction = {type: 'init'}
    const result = granules(initialState, initialAction)

    result.inFlight.should.equal(false)
  })

  it('marks inFlight true while retrieving granules', function () {
    const initial = Immutable({inFlight: false})
    const result = granules(initial, fetchingGranules())
    result.inFlight.should.equal(true)
  })

  it('marks inFlight false while receiving granules', function () {
    const initial = Immutable({inFlight: true})
    const result = granules(initial, fetchedGranules([{id: 'A'}]))
    result.inFlight.should.equal(false)
  })

})
