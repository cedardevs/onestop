import React from 'react'
import {FEATURE_CART} from '../utils/featureUtils'
import {cart_arrow_down, SvgIcon} from '../common/SvgIcon'
import HeaderLink from './HeaderLink'

const styleCartMenuItem = {
  position: 'relative',
}

const styleCartBadge = {
  position: 'absolute',
  top: '-1em',
  right: '-1em',
  fontSize: '0.5em',
  width: '1.5em',
  height: '1.5em',
  lineHeight: '1.5em',
  borderRadius: '1em',
  padding: '0.25em',
  background: 'red',
  color: 'white',
  textAlign: 'center',
}

const styleCartIcon = {
  fill: 'white',
  transition: 'fill 0.3s ease',
}

const styleCartIconHover = {
  fill: '#277cb2',
}

export default class HeaderCartLink extends React.Component {
  componentWillMount() {
    this.setState({
      hovering: false,
    })
  }

  handleRedirectToCart = () => {
    const {history, location} = this.props
    if (location.pathname !== '/cart') {
      history.push('/cart')
    }
  }

  handleMouseOver = event => {
    this.setState(prevState => {
      return {
        ...prevState,
        hovering: true,
      }
    })
  }

  handleMouseOut = event => {
    this.setState(prevState => {
      return {
        ...prevState,
        hovering: false,
      }
    })
  }

  render() {
    const {featuresEnabled, abbreviatedNumberOfGranulesSelected} = this.props

    const cartBadge = (
      <div style={styleCartBadge}>{abbreviatedNumberOfGranulesSelected}</div>
    )

    const styleCartIconMerged = {
      ...styleCartIcon,
      ...(this.state.hovering ? styleCartIconHover : {}),
    }

    const cartIcon = (
      <SvgIcon
        key="header-cart-link-icon"
        size="1.309em"
        style={styleCartIconMerged}
        path={cart_arrow_down}
        verticalAlign="initial"
      />
    )

    const cartMenuItem = (
      <div key="cartMenuItem" style={styleCartMenuItem}>
        {cartIcon}
        {cartBadge}
      </div>
    )

    if (featuresEnabled.includes(FEATURE_CART)) {
      return (
        <HeaderLink
          title=""
          to="/cart"
          onMouseOver={this.handleMouseOver}
          onMouseOut={this.handleMouseOut}
        >
          {cartMenuItem}
        </HeaderLink>
      )
    }
    else {
      return null
    }
  }
}
