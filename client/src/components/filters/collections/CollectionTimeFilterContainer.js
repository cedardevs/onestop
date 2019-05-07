import {connect} from 'react-redux'
import {withRouter} from 'react-router'
import TimeFilter from '../time/TimeFilter'
import {
  collectionRemoveDateRange,
  collectionUpdateDateRange,
} from '../../../actions/search/CollectionFilterActions'
import {collectionClearResults} from '../../../actions/search/CollectionResultActions'
import {
  triggerSearch,
  showCollections,
} from '../../../actions/search/CollectionSearchActions'

const mapStateToProps = state => {
  const {startDateTime, endDateTime} = state.search.collectionFilter
  return {
    startDateTime: startDateTime,
    endDateTime: endDateTime,
  }
}

const mapDispatchToProps = (dispatch, ownProps) => {
  return {
    updateDateRange: (startDate, endDate) => {
      dispatch(collectionUpdateDateRange(startDate, endDate))
    },
    removeDateRange: () => {
      dispatch(collectionRemoveDateRange())
    },
    submit: () => {
      dispatch(collectionClearResults())
      dispatch(triggerSearch())
      dispatch(showCollections(ownProps.history))
    },
  }
}

const CollectionTimeFilterContainer = withRouter(
  connect(mapStateToProps, mapDispatchToProps)(TimeFilter)
)

export default CollectionTimeFilterContainer
