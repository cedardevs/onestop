import {connect} from 'react-redux'
import GranuleListNavigation from './GranuleListNavigation'
import {withRouter} from 'react-router'

const mapStateToProps = state => {
  const focusedItem = state.search.collectionDetailResult.collection
  return {
    collectionId: focusedItem ? focusedItem.id : null,
    collectionTitle: focusedItem ? focusedItem.attributes.title : null,
  }
}

const GranuleListNavigationContainer = withRouter(
  connect(mapStateToProps, null)(GranuleListNavigation)
)

export default GranuleListNavigationContainer
