import {connect} from 'react-redux'
import Cart from './Cart'

import {withRouter} from 'react-router'

const mapStateToProps = state => {
  return {
  }
}

const mapDispatchToProps = dispatch => {
  return {}
}

const CartContainer = withRouter(
    connect(mapStateToProps, mapDispatchToProps)(Cart)
)

export default CartContainer
