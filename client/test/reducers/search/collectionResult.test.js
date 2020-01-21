import Immutable from 'seamless-immutable'
import {
  collectionResult,
  initialState,
} from '../../../src/reducers/search/collectionResult'
import {
  collectionNewSearchResultsReceived,
  collectionResultsPageReceived,
  collectionSearchError,
} from '../../../src/actions/routing/CollectionSearchStateActions'

describe('The collectionResult reducer', function(){
  it('has a default state', function(){
    const initialAction = {type: 'init'}
    const result = collectionResult(initialState, initialAction)

    expect(result).toEqual({
      collections: {},
      facets: {},
      totalCollectionCount: 0,
      loadedCollectionCount: 0,
    })
  })

  it('can reset existing collection state on error', function(){
    const stateWithCollections = Immutable({
      collections: {A: {id: 123}},
      facets: {
        science: {
          Oceans: {
            count: 5,
          },
        },
      },
      totalCollectionCount: 1,
      loadedCollectionCount: 1,
    })
    const result = collectionResult(
      stateWithCollections,
      collectionSearchError()
    )
    expect(result.collections).toEqual({})
    expect(result.facets).toEqual({})
    expect(result.totalCollectionCount).toBe(0)
    expect(result.loadedCollectionCount).toBe(0)
  })

  it('can update collections on recieving a new page of results', function(){
    const resultsPage1LoadedState = Immutable({
      collections: {A: {title: 'title A'}},
      totalCollectionCount: 3,
      loadedCollectionCount: 1,
      facets: {},
    })

    const result = collectionResult(
      resultsPage1LoadedState,
      collectionResultsPageReceived(3, [
        {id: 'B', attributes: {title: 'title B'}},
        {id: 'C', attributes: {title: 'title C'}},
      ])
    )

    expect(result.collections).toEqual({
      B: {title: 'title B'},
      C: {title: 'title C'},
    })
    expect(result.totalCollectionCount).toBe(3)
    expect(result.loadedCollectionCount).toBe(2)
  })

  it('can reset existing collection state on new search', function(){
    const stateWithCollections = Immutable({
      collections: {A: {title: 'title A'}},
      facets: {
        science: {
          Oceans: {
            count: 5,
          },
        },
      },
      totalCollections: 1,
      loadedCollections: 1,
    })

    const facets = {
      science: {
        Oceans: {
          count: 5,
        },
        'Oceans > Ocean Temperature': {
          count: 5,
        },
        'Oceans > Ocean Temperature > Sea Surface Temperature': {
          count: 5,
        },
        dataResolution: {},
      },
    }

    const result = collectionResult(
      stateWithCollections,
      collectionNewSearchResultsReceived(
        30,
        [
          {id: 'B', attributes: {title: 'title B'}},
          {id: 'C', attributes: {title: 'title C'}},
        ],
        facets
      )
    )
    expect(result.collections).toEqual({
      B: {title: 'title B'},
      C: {title: 'title C'},
    })
    expect(result.facets).toEqual(facets)
    expect(result.totalCollectionCount).toBe(30)
    expect(result.loadedCollectionCount).toBe(2)
  })

  // it('merges received granules into the map of granules', function(){
  //   const firstRoundData = [
  //     {id: 'A', attributes: {version: 1}},
  //     {id: 'B', attributes: {version: 1}},
  //   ]
  //   const firstRoundMap = {A: {version: 1}, B: {version: 1}}
  //   const firstRoundResult = collectionResult(
  //     initialState,
  //     collectionDetailGranulesSuccess(firstRoundData)
  //   )
  //   expect(firstRoundResult.granules).toEqual(firstRoundMap)
  //
  //   const secondRoundData = [
  //     {id: 'B', attributes: {version: 2}},
  //     {id: 'C', attributes: {version: 1}},
  //   ]
  //   const secondRoundMap = {A: {version: 1}, B: {version: 2}, C: {version: 1}}
  //   const secondRoundResult = collectionResult(
  //     firstRoundResult,
  //     collectionDetailGranulesSuccess(secondRoundData)
  //   )
  //   expect(secondRoundResult.granules).toEqual(secondRoundMap)
  // })
})
