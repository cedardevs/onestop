import store from '../../src/store' // create Redux store with appropriate middleware
import {RESET_STORE} from '../../src/reducer'
import fetchMock from 'fetch-mock'
import history from '../../src/history'
import {loadDetails} from '../../src/actions/InitActions'

describe('init action', function(){
  const BASE_URL = '/onestop/api/search'
  const resetStore = () => ({type: RESET_STORE})
  beforeEach(async () => {
    // reset store to initial conditions
    await store.dispatch(resetStore())
  })

  afterEach(() => {
    fetchMock.reset()
  })

  it('updates granule filters from URL when loading detail page', async () => {
    await store.dispatch(
      loadDetails(
        history,
        '/collections/details/uuid-ABC',
        '?q=co-ops&s=2017-01-01T00%3A00%3A00Z'
      )
    )

    expect(
      store.getState().search.collectionDetailFilter.startDateTime
    ).toEqual('2017-01-01T00:00:00Z')
  })
})

// TODO it('do not dispatch a transition to the collections view, just a collectionClearselectedCollectionIds action, when no search params are present', function () {})

// TODO   it('initialize triggers config, version info, total counts, and data loading'...
