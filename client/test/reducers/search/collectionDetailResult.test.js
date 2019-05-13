import Immutable from 'seamless-immutable'
import {
  collectionDetailResult,
  initialState,
} from '../../../src/reducers/search/collectionDetailResult'
import {
  collectionGetDetailComplete,
  collectionGetDetailError,
} from '../../../src/actions/get/CollectionDetailRequestActions'

describe('The collectionDetailResult reducer', function(){
  it('has a default state', function(){
    const initialAction = {type: 'init'}
    const result = collectionDetailResult(initialState, initialAction)

    expect(result).toEqual({
      collectionDetail: null,
    })
  })

  it('can reset existing collection state on error', function(){
    const stateWithLoadedCollection = Immutable({
      collectionDetail: {
        title: 'A collection',
      },
    })
    const result = collectionDetailResult(
      stateWithLoadedCollection,
      collectionGetDetailError()
    )
    expect(result).toEqual({
      collectionDetail: null,
    })
  })

  it('sets the collection response when GET returns', function(){
    const data = Immutable({
      title: 'Some sort of data',
    })

    const result = collectionDetailResult(
      initialState,
      collectionGetDetailComplete(data, {totalGranuleCount: 30})
    )
    expect(result.collectionDetail).toEqual({
      collection: data,
      totalGranuleCount: 30,
    })
  })

  it('can override existing collection state on new GET response', function(){
    const stateWithLoadedCollection = Immutable({
      collectionDetail: {
        title: 'A collection',
        description: 'This one has a description',
      },
    })
    const data = Immutable({
      title: 'Some sort of data',
    })
    const result = collectionDetailResult(
      stateWithLoadedCollection,
      collectionGetDetailComplete(data, {totalGranuleCount: 0})
    )
    expect(result.collectionDetail).toEqual({
      collection: data,
      totalGranuleCount: 0,
    })
  })
})
