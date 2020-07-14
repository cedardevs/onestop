import {connect} from 'react-redux'
import {withRouter} from 'react-router'

import {submitCollectionSearchWithPage} from '../../../actions/routing/CollectionSearchRouteActions'
import {submitCollectionDetail} from '../../../actions/routing/CollectionDetailRouteActions'
import {saveSearch} from '../../../actions/SavedSearchActions'

import Collections from './Collections'

const mapStateToProps = state => {
  const {
    collections,
    totalCollectionCount,
    loadedCollectionCount,
    pageSize,
  } = state.search.collectionResult

  const {user, config, search} = state
  const savedSearchUrl = config.auth ? config.auth.savedSearchEndpoint : null
  const isAuthenticatedUser = user && user.isAuthenticated ? true : false
  const filter = search.collectionFilter

  return {
    results: collections,
    totalHits: totalCollectionCount,
    returnedHits: loadedCollectionCount,
    searchTerms: state.search.collectionFilter.queryText,
    loading: state.search.collectionRequest.inFlight,
    collectionDetailFilter: state.search.collectionFilter, // just used to submit collection detail correctly
    pageSize,
    savedSearchUrl: savedSearchUrl,
    isAuthenticatedUser : isAuthenticatedUser,
    user: user,
    filter: filter,
  }
}

const mapDispatchToProps = (dispatch, ownProps) => {
  return {
    saveSearch: (savedSearchUrl, urlToSave, saveName, filter) => {
      dispatch(saveSearch(savedSearchUrl, urlToSave, saveName, filter))
    },
    selectCollection: (id, filterState) => {
      dispatch(submitCollectionDetail(ownProps.history, id, filterState))
    },
    fetchResultPage: (offset, max) => {
      dispatch(submitCollectionSearchWithPage(offset, max))
    },
  }
}

const CollectionsContainer = withRouter(
  connect(mapStateToProps, mapDispatchToProps)(Collections)
)

export default CollectionsContainer
