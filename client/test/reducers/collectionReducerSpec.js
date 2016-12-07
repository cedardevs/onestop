import '../specHelper'
import Immutable from 'immutable'
import { collections, initialState } from '../../src/result/collections/CollectionReducer'
import { toggleSelection, clearSelections } from '../../src/result/collections/CollectionsActions'

describe('The collections reducer', function () {
  it('has a default state', function () {
    const initialAction = {type: 'init'}
    const result = collections(initialState, initialAction)

    result.get('results').should.be.an.instanceOf(Immutable.Map)
    result.get('selectedIds').should.be.an.instanceOf(Immutable.Set)
  })

  it('toggles selected collections', function () {
    const toggleA = toggleSelection('A')
    const toggleB = toggleSelection('B')
    // toggle A --> ['A']
    const addedAResult = collections(initialState, toggleA)
    addedAResult.get('selectedIds').should.equal(Immutable.Set(['A']))
    // toggle B --> ['A', 'B']
    const addedBResult = collections(addedAResult, toggleB)
    addedBResult.get('selectedIds').should.equal(Immutable.Set(['A', 'B']))
    // toggle A --> ['B']
    const removedAResult = collections(addedBResult, toggleA)
    removedAResult.get('selectedIds').should.equal(Immutable.Set(['B']))
  })

  it('can clear existing collection selections', function () {
    const stateWithCollections = Immutable.fromJS({selectedIds: ['ABC']})
    const result = collections(stateWithCollections, clearSelections())
    result.get('selectedIds').should.equal(Immutable.Set())
  })
})