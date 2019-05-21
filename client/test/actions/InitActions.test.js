import store from '../../src/store' // create Redux store with appropriate middleware
import {RESET_STORE} from '../../src/reducer'
import fetchMock from 'fetch-mock'

import {loadDetails} from '../../src/actions/InitActions'

let history_input = {}

const mockHistoryPush = input => {
  history_input = input
}
const mockHistory = {
  push: mockHistoryPush,
}

describe('init action', function(){
  const BASE_URL = '/-search'
  const resetStore = () => ({type: RESET_STORE})
  beforeEach(async () => {
    history_input = {}
    // reset store to initial conditions
    await store.dispatch(resetStore())
  })

  afterEach(() => {
    fetchMock.reset()
  })

  it('updates granule filters from URL when loading detail page', async () => {
    await store.dispatch(
      loadDetails(
        mockHistory,
        '/collections/details/uuid-ABC',
        '?q=co-ops&s=2017-01-01T00%3A00%3A00Z'
      )
    )

    expect(
      store.getState().search.collectionDetailFilter.startDateTime
    ).toEqual('2017-01-01T00:00:00Z')
  })
})
