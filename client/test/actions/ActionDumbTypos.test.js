import store from '../../src/store' // create Redux store with appropriate middleware

import {submitCollectionDetail} from '../../src/actions/routing/CollectionDetailRouteActions'
import {submitCollectionSearch} from '../../src/actions/routing/CollectionSearchRouteActions'
import {submitGranuleSearch} from '../../src/actions/routing/GranuleSearchRouteActions'
import {
  loadCollections,
  loadDetails,
  loadGranulesList,
} from '../../src/actions/InitActions'

const mockHistoryPush = input => {} // don't care, just don't want it to puke
const mockHistory = {
  push: mockHistoryPush,
  location: {pathname: 'test', search: null},
}

describe('actions execute without dumb typos', function(){
  it('submitCollectionDetail', function(){
    // introduced to catch when parameter `collectionId` is referenced as `id` within the function
    store.dispatch(submitCollectionDetail(mockHistory, 'uuid-ABC', {}))
  })
  it('submitCollectionSearch', function(){
    store.dispatch(submitCollectionSearch(mockHistory))
  })
  it('submitGranuleSearch', function(){
    store.dispatch(submitGranuleSearch(mockHistory, 'uuid-ABC'))
  })
  it('loadCollections', function(){
    // introduced to catch when import name changed for a function this depends on
    store.dispatch(loadCollections(mockHistory, '?q=test'))
  })
  it('loadDetails', function(){
    // introduced to catch when import name changed for a function this depends on
    store.dispatch(
      loadDetails(mockHistory, '/collections/details/uuid-ABC', '?q=test')
    )
  })
  it('loadGranulesList', function(){
    // introduced to catch when import name changed for a function this depends on
    store.dispatch(
      loadGranulesList(mockHistory, '/collections/granules/uuid-ABC', '?q=test')
    )
  })
})
