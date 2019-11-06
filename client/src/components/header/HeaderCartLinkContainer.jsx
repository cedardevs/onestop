import {connect} from 'react-redux'
import HeaderCartLink from './HeaderCartLink'
import {withRouter} from 'react-router'
import {abbreviateNumber} from '../../utils/readableUtils'

const mapStateToProps = state => {
  const selectedGranules = state.cart.selectedGranules
  const numberOfGranulesSelected = Object.keys(selectedGranules).length
  const abbreviatedNumberOfGranulesSelected = abbreviateNumber(
    numberOfGranulesSelected
  )
  return {
    featuresEnabled: state.config.featuresEnabled,
    numberOfGranulesSelected: numberOfGranulesSelected,
    abbreviatedNumberOfGranulesSelected: abbreviatedNumberOfGranulesSelected,
  }
}

const HeaderCartLinkContainer = withRouter(
  connect(mapStateToProps, null)(HeaderCartLink)
)

export default HeaderCartLinkContainer
