import Immutable from 'seamless-immutable'
import {
  granuleResult,
  initialState,
} from '../../../src/reducers/search/granuleResult'
import {
  granuleNewSearchResultsReceived,
  granuleMoreResultsReceived,
  granuleSearchError,
} from '../../../src/actions/routing/GranuleSearchStateActions'

describe('The granuleResult reducer', function(){
  it('has a default state', function(){
    const initialAction = {type: 'init'}
    const result = granuleResult(initialState, initialAction)

    expect(result).toEqual({
      granules: {},
      facets: {},
      totalGranuleCount: 0,
      loadedGranuleCount: 0,
    })
  })

  it('can reset existing granule state on error', function(){
    const stateWithGranules = Immutable({
      granules: {A: {id: 123}},
      facets: {
        science: {
          Oceans: {
            count: 5,
          },
        },
      },
      totalGranuleCount: 1,
      loadedGranuleCount: 1,
    })
    const result = granuleResult(stateWithGranules, granuleSearchError())
    expect(result.granules).toEqual({})
    expect(result.facets).toEqual({})
    expect(result.totalGranuleCount).toBe(0)
    expect(result.loadedGranuleCount).toBe(0)
  })

  it('can update granules on recieving more results', function(){
    const resultsPage1LoadedState = Immutable({
      granules: {A: {title: 'title A'}},
      totalGranuleCount: 3,
      loadedGranuleCount: 1,
      facets: {},
    })

    const result = granuleResult(
      resultsPage1LoadedState,
      granuleMoreResultsReceived([
        {id: 'B', attributes: {title: 'title B'}},
        {id: 'C', attributes: {title: 'title C'}},
      ])
    )

    expect(result.granules).toEqual({
      A: {title: 'title A'},
      B: {title: 'title B'},
      C: {title: 'title C'},
    })
    expect(result.totalGranuleCount).toBe(3)
    expect(result.loadedGranuleCount).toBe(3)
  })

  it('can reset existing granule state on new search', function(){
    const stateWithGranules = Immutable({
      granules: {A: {title: 'title A'}},
      facets: {
        science: {
          Oceans: {
            count: 5,
          },
        },
      },
      totalGranuleCount: 1,
      loadedGranuleCount: 1,
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

    const result = granuleResult(
      stateWithGranules,
      granuleNewSearchResultsReceived(
        [
          {id: 'B', attributes: {title: 'title B'}},
          {id: 'C', attributes: {title: 'title C'}},
        ],
        facets,
        30
      )
    )
    expect(result.granules).toEqual({
      B: {title: 'title B'},
      C: {title: 'title C'},
    })
    expect(result.facets).toEqual(facets)
    expect(result.totalGranuleCount).toBe(30)
    expect(result.loadedGranuleCount).toBe(2)
  })
})
