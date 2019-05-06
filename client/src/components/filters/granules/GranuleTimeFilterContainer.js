import {connect} from 'react-redux'
import {withRouter} from 'react-router'
import TimeFilter from '../time/TimeFilter'
import {
  granuleRemoveDateRange,
  granuleUpdateDateRange,
} from '../../../actions/search/GranuleFilterActions'
import {granuleClearResults} from '../../../actions/search/GranuleResultActions'
import {
  triggerGranuleSearch,
  showGranules,
} from '../../../actions/search/GranuleSearchActions'

const mapStateToProps = state => {
  const focusedItem = state.search.collectionResult.collectionDetail
  const {startDateTime, endDateTime} = state.search.granuleFilter
  return {
    startDateTime: startDateTime,
    endDateTime: endDateTime,
    id: focusedItem ? focusedItem.collection.id : null,
  }
}

const mapDispatchToProps = (dispatch, ownProps) => {
  return {
    updateDateRange: (startDate, endDate) => {
      dispatch(granuleUpdateDateRange(startDate, endDate))
    },
    removeDateRange: () => {
      dispatch(granuleRemoveDateRange())
    },
    submit: collectionId => {
      dispatch(granuleClearResults())
      console.log('wtf mate?', triggerGranuleSearch)
      dispatch(triggerGranuleSearch())
      dispatch(showGranules(ownProps.history, collectionId))
    },
  }
}

const GranuleTimeFilterContainer = withRouter(
  connect(mapStateToProps, mapDispatchToProps)(TimeFilter)
)

export default GranuleTimeFilterContainer
