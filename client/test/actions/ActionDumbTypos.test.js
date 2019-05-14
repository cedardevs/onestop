import Immutable from 'seamless-immutable'
import {submitCollectionDetail} from '../../src/actions/routing/CollectionDetailRouteActions'
import {submitCollectionSearch} from '../../src/actions/routing/CollectionSearchRouteActions'
import {submitGranuleSearch} from '../../src/actions/routing/GranuleSearchRouteActions'
import {loadDetails} from '../../src/actions/InitActions'

import store from '../../src/store' // create Redux store with appropriate middleware

const mockHistoryPush = () => {} // don't care, just don't want it to puke
const mockHistory = {
  push: mockHistoryPush,
}

describe('actions execute without dumb typos', function(){
  it('submitCollectionDetail', function(){
    // introduced to catch when parameter `collectionId` is referenced as `id` within the function
    store.dispatch(submitCollectionDetail(mockHistory, 'uuid-ABC'))
  })
  it('submitCollectionSearch', function(){
    store.dispatch(submitCollectionSearch(mockHistory))
  })
  it('submitGranuleSearch', function(){
    store.dispatch(submitGranuleSearch(mockHistory, 'uuid-ABC'))
  })
  it('loadDetails', function(){
    // introduced to catch when import name changed for a function this depends on
    store.dispatch(loadDetails(mockHistory, '/collections/details/uuid-ABC'))
  })
})
