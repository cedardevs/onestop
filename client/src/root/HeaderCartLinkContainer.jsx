import {connect} from 'react-redux'
import HeaderCartLink from './HeaderCartLink'
import {withRouter} from 'react-router'
import {abbreviateNumber} from '../utils/readableUtils'
import {getSelectedGranulesFromStorage} from '../utils/localStorageUtil'

const mapStateToProps = state => {
  const numberOfGranulesSelected = Object.keys(
    getSelectedGranulesFromStorage(state)
  ).length
  const abbreviatedNumberOfGranulesSelected = abbreviateNumber(
    numberOfGranulesSelected
  )
  return {
    featuresEnabled: state.domain.config.featuresEnabled,
    numberOfGranulesSelected: numberOfGranulesSelected,
    abbreviatedNumberOfGranulesSelected: abbreviatedNumberOfGranulesSelected,
  }
}

const HeaderCartLinkContainer = withRouter(
  connect(mapStateToProps, null)(HeaderCartLink)
)

export default HeaderCartLinkContainer
