import '../../specHelper'
import Immutable from 'seamless-immutable'
import { collectionSelect, initialState } from '../../../src/reducers/behavior/collectionSelect'
import { toggleSelection, clearSelections } from '../../../src/result/collections/CollectionsActions'

describe('The collectionSelect reducer', function () {
  it('has a default state', function () {
    const initialAction = {type: 'init'}
    const result = collectionSelect(initialState, initialAction)

    result.selectedIds.should.be.an.instanceOf(Array)
  })

  it('toggles selected collections', function () {
    const toggleA = toggleSelection('A')
    const toggleB = toggleSelection('B')
    // toggle A --> ['A']
    const addedAResult = collectionSelect(initialState, toggleA)
    addedAResult.selectedIds.should.deep.equal(['A'])
    // toggle B --> ['A', 'B']
    const addedBResult = collectionSelect(addedAResult, toggleB)
    addedBResult.selectedIds.should.deep.equal(['A', 'B'])
    // toggle A --> ['B']
    const removedAResult = collectionSelect(addedBResult, toggleA)
    removedAResult.selectedIds.should.deep.equal(['B'])
  })

  it('can clear existing collection selections', function () {
    const stateWithCollections = Immutable({selectedIds: ['ABC']})
    const result = collectionSelect(stateWithCollections, clearSelections())
    result.selectedIds.should.deep.equal([])
  })
})
