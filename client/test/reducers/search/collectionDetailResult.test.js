import Immutable from 'seamless-immutable'
import {
  collectionDetailResult,
  initialState,
} from '../../../src/reducers/search/collectionDetailResult'
import {
  collectionDetailReceived,
  collectionDetailError,
  granuleMatchingCountReceived,
} from '../../../src/actions/routing/CollectionDetailStateActions'

describe('The collectionDetailResult reducer', function(){
  it('has a default state', function(){
    const initialAction = {type: 'init'}
    const result = collectionDetailResult(initialState, initialAction)

    expect(result).toEqual({
      collection: null,
      totalGranuleCount: 0,
      filteredGranuleCount: 0,
    })
  })

  it('can reset existing collection state on error', function(){
    const stateWithLoadedCollection = Immutable({
      collection: {
        title: 'A collection',
      },
    })
    const result = collectionDetailResult(
      stateWithLoadedCollection,
      collectionDetailError()
    )
    expect(result).toEqual(initialState)
  })

  it('sets the collection response when GET returns', function(){
    const data = Immutable({
      title: 'Some sort of data',
    })

    const result = collectionDetailResult(
      initialState,
      collectionDetailReceived(data, 30)
    )
    expect(result.collection).toEqual(data)
    expect(result.totalGranuleCount).toEqual(30)
  })

  it('sets the filtered count when background task returns', function(){
    const result = collectionDetailResult(
      initialState,
      granuleMatchingCountReceived(13)
    )
    expect(result.filteredGranuleCount).toEqual(13)
  })

  it('can override existing collection state on new GET response', function(){
    const stateWithLoadedCollection = Immutable({
      collection: {
        title: 'A collection',
        description: 'This one has a description',
      },
    })
    const data = Immutable({
      title: 'Some sort of data',
    })
    const result = collectionDetailResult(
      stateWithLoadedCollection,
      collectionDetailReceived(data, 0)
    )
    expect(result).toEqual({
      collection: data,
      totalGranuleCount: 0,
    })
  })
})
