import Immutable from 'seamless-immutable'
import {
  granuleResult,
  initialState,
} from '../../../src/reducers/search/granuleResult'
import {
  granuleNewSearchResultsRecieved,
  granuleMoreResultsRecieved,
  granuleSearchError,
} from '../../../src/actions/search/GranuleRequestActions'

describe('The granuleResult reducer', function(){
  it('has a default state', function(){
    const initialAction = {type: 'init'}
    const result = granuleResult(initialState, initialAction)

    expect(result).toEqual({
      granules: {},
      facets: {},
      totalGranules: 0,
      loadedGranules: 0,
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
      totalGranules: 1,
      loadedGranules: 1,
    })
    const result = granuleResult(stateWithGranules, granuleSearchError())
    expect(result.granules).toEqual({})
    expect(result.facets).toEqual({})
    expect(result.totalGranules).toBe(0)
    expect(result.loadedGranules).toBe(0)
  })

  it('can update granules on recieving more results', function(){
    const resultsPage1LoadedState = Immutable({
      granules: {A: {title: 'title A'}},
      totalGranules: 3,
      loadedGranules: 1,
      facets: {},
    })

    const result = granuleResult(
      resultsPage1LoadedState,
      granuleMoreResultsRecieved([
        {id: 'B', attributes: {title: 'title B'}},
        {id: 'C', attributes: {title: 'title C'}},
      ])
    )

    expect(result.granules).toEqual({
      A: {title: 'title A'},
      B: {title: 'title B'},
      C: {title: 'title C'},
    })
    expect(result.totalGranules).toBe(3)
    expect(result.loadedGranules).toBe(3)
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
      totalGranules: 1,
      loadedGranules: 1,
    })

    const metadata = {
      facets: {
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
      },
    }

    const result = granuleResult(
      stateWithGranules,
      granuleNewSearchResultsRecieved(
        30,
        [
          {id: 'B', attributes: {title: 'title B'}},
          {id: 'C', attributes: {title: 'title C'}},
        ],
        metadata
      )
    )
    expect(result.granules).toEqual({
      B: {title: 'title B'},
      C: {title: 'title C'},
    })
    expect(result.facets).toEqual(metadata.facets)
    expect(result.totalGranules).toBe(30)
    expect(result.loadedGranules).toBe(2)
  })
})
