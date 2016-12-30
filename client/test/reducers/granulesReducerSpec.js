import '../specHelper'
import Immutable from 'seamless-immutable'
import { granules, initialState } from '../../src/result/granules/GranulesReducer'
import { toggleGranuleFocus, fetchingGranules, fetchedGranules, clearGranules } from '../../src/result/granules/GranulesActions'

describe('The granules reducer', function() {
  it('has a default state', function () {
    const initialAction = {type: 'init'}
    const result = granules(initialState, initialAction)

    result.focusedGranules.should.deep.equal([])
  })

  it('toggles focused granules', function () {
    const toggleA = toggleGranuleFocus('A')
    const toggleB = toggleGranuleFocus('B')
    // toggle A --> ['A']
    const addedAResult = granules(initialState, toggleA)
    addedAResult.focusedGranules.should.deep.equal(['A'])
    // toggle B --> ['A', 'B']
    const addedBResult = granules(addedAResult, toggleB)
    addedBResult.focusedGranules.should.deep.equal(['A', 'B'])
    // toggle A --> ['B']
    const removedAResult = granules(addedBResult, toggleA)
    removedAResult.focusedGranules.should.deep.equal(['B'])
  })

  it('marks inFlight true while retrieving granules', function () {
    const initial = Immutable({inFlight: false})
    const result = granules(initial, fetchingGranules())
    result.inFlight.should.equal(true)
  })

  it('marks inFlight false while receiving granules', function () {
    const initial = Immutable({inFlight: true, granules: new Map()})
    const result = granules(initial, fetchedGranules([{id: 'A'}]))
    result.inFlight.should.equal(false)
  })

  it('merges received granules into the map of granules', function () {
    const firstRoundData = [{id: 'A', attributes: {version: 1}}, {id: 'B', attributes: {version: 1}}]
    const firstRoundMap = {A: {version: 1}, B: {version: 1}}
    const firstRoundResult = granules(initialState, fetchedGranules(firstRoundData))
    firstRoundResult.granules.should.deep.equal(firstRoundMap)

    const secondRoundData = [{id: 'B', attributes: {version: 2}}, {id: 'C', attributes: {version: 1}}]
    const secondRoundMap = {A: {version: 1}, B: {version: 2}, C: {version: 1}}
    const secondRoundResult = granules(firstRoundResult, fetchedGranules(secondRoundData))
    secondRoundResult.granules.should.deep.equal(secondRoundMap)
  })

  it('can clear existing granule state', function () {
    const stateWithGranules = Immutable({granules: {A: {id: 'A'}}})
    const result = granules(stateWithGranules, clearGranules())
    result.granules.should.deep.equal({})
  })

})
