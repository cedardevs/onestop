import '../specHelper'
import Immutable from 'seamless-immutable'
import { collections, initialState } from '../../src/result/collections/CollectionReducer'
import { toggleSelection, clearSelections } from '../../src/result/collections/CollectionsActions'

describe('The collections reducer', function () {
  it('has a default state', function () {
    const initialAction = {type: 'init'}
    const result = collections(initialState, initialAction)

    result.results.should.be.an.instanceOf(Object)
    result.selectedIds.should.be.an.instanceOf(Array)
  })

  it('toggles selected collections', function () {
    const toggleA = toggleSelection('A')
    const toggleB = toggleSelection('B')
    // toggle A --> ['A']
    const addedAResult = collections(initialState, toggleA)
    addedAResult.selectedIds.should.deep.equal(['A'])
    // toggle B --> ['A', 'B']
    const addedBResult = collections(addedAResult, toggleB)
    addedBResult.selectedIds.should.deep.equal(['A', 'B'])
    // toggle A --> ['B']
    const removedAResult = collections(addedBResult, toggleA)
    removedAResult.selectedIds.should.deep.equal(['B'])
  })

  it('can clear existing collection selections', function () {
    const stateWithCollections = Immutable({selectedIds: ['ABC']})
    const result = collections(stateWithCollections, clearSelections())
    result.selectedIds.should.deep.equal([])
  })
})
