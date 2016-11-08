import '../specHelper'
import Immutable from 'immutable'
import { granules, initialState } from '../../src/granules/GranulesReducer'
import { toggleGranuleFocus, toggleCollectionSelection, fetchingGranules, fetchedGranules, clearGranules } from '../../src/granules/GranulesActions'

describe('The granules reducer', function() {
  it('has a default state', function () {
    const initialAction = {type: 'init'}
    const result = granules(initialState, initialAction)

    result.has('focusedGranules').should.equal(true)
  })

  it('toggles focused granules', function () {
    const toggleA = toggleGranuleFocus('A')
    const toggleB = toggleGranuleFocus('B')
    // toggle A --> ['A']
    const addedAResult = granules(initialState, toggleA)
    addedAResult.get('focusedGranules').should.equal(Immutable.Set(['A']))
    // toggle B --> ['A', 'B']
    const addedBResult = granules(addedAResult, toggleB)
    addedBResult.get('focusedGranules').should.equal(Immutable.Set(['A', 'B']))
    // toggle A --> ['B']
    const removedAResult = granules(addedBResult, toggleA)
    removedAResult.get('focusedGranules').should.equal(Immutable.Set(['B']))
  })

  it('toggles selected collections', function () {
    const toggleA = toggleCollectionSelection('A')
    const toggleB = toggleCollectionSelection('B')
    // toggle A --> ['A']
    const addedAResult = granules(initialState, toggleA)
    addedAResult.get('selectedCollections').should.equal(Immutable.Set(['A']))
    // toggle B --> ['A', 'B']
    const addedBResult = granules(addedAResult, toggleB)
    addedBResult.get('selectedCollections').should.equal(Immutable.Set(['A', 'B']))
    // toggle A --> ['B']
    const removedAResult = granules(addedBResult, toggleA)
    removedAResult.get('selectedCollections').should.equal(Immutable.Set(['B']))
  })

  it('marks inFlight true while retrieving granules', function () {
    const initial = Immutable.Map({inFlight: false})
    const result = granules(initial, fetchingGranules())
    result.get('inFlight').should.equal(true)
  })

  it('marks inFlight false while receiving granules', function () {
    const initial = Immutable.Map({inFlight: true, granules: Immutable.Map()})
    const result = granules(initial, fetchedGranules([{id: 'A'}]))
    result.get('inFlight').should.equal(false)
  })

  it('merges received granules into the map of granules', function () {
    const firstRoundData = [{id: 'A', version: 1}, {id: 'B', version: 1}]
    const firstRoundMap = {A: {id: 'A', version: 1}, B: {id: 'B', version: 1}}
    const firstRoundResult = granules(initialState, fetchedGranules(firstRoundData))
    firstRoundResult.get('granules').should.equal(Immutable.fromJS(firstRoundMap))

    const secondRoundData = [{id: 'B', version: 2}, {id: 'C', version: 1}]
    const secondRoundMap = {A: {id: 'A', version: 1}, B: {id: 'B', version: 2}, C: {id: 'C', version: 1}}
    const secondRoundResult = granules(firstRoundResult, fetchedGranules(secondRoundData))
    secondRoundResult.get('granules').should.equal(Immutable.fromJS(secondRoundMap))
  })

  it('can clear existing granule state', function () {
    const stateWithGranules = Immutable.fromJS({granules: {A: {id: 'A'}}})
    const result = granules(stateWithGranules, clearGranules())
    result.get('granules').should.equal(Immutable.Map())
  })
})