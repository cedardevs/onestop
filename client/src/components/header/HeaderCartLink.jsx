import React from 'react'
import {FEATURE_CART} from '../../utils/featureUtils'
import {cart_arrow_down, SvgIcon} from '../common/SvgIcon'
import HeaderLink from './HeaderLink'
import defaultStyles, {SiteColors} from '../../style/defaultStyles'

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
  background: SiteColors.WARNING,
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
    const {
      featuresEnabled,
      numberOfGranulesSelected,
      abbreviatedNumberOfGranulesSelected,
    } = this.props

    const cartBadge = (
      <div style={styleCartBadge} aria-hidden="true">
        {abbreviatedNumberOfGranulesSelected}
      </div>
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

    const cartLabel =
      numberOfGranulesSelected > 0
        ? `Download Cart ${numberOfGranulesSelected} items`
        : 'Download Cart empty'

    const cartMenuItem = (
      <div key="cartMenuItem" style={styleCartMenuItem}>
        {cartIcon}
        {numberOfGranulesSelected > 0 ? cartBadge : null}
      </div>
    )

    if (featuresEnabled.includes(FEATURE_CART)) {
      return (
        <div style={{display: 'flex'}}>
          <div
            aria-live="polite"
            aria-atomic="false"
            style={defaultStyles.hideOffscreen}
          >
            {numberOfGranulesSelected ? (
              `${numberOfGranulesSelected} items in cart`
            ) : (
              ''
            )}
          </div>
          <HeaderLink
            title={cartLabel}
            to="/cart"
            onMouseOver={this.handleMouseOver}
            onMouseOut={this.handleMouseOut}
            badgeAlert={true}
          >
            {cartMenuItem}
          </HeaderLink>
        </div>
      )
    }
    else {
      return null
    }
  }
}
