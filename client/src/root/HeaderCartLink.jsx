import React from 'react'
import Button from '../common/input/Button'
import {FEATURE_CART} from '../utils/featureUtils'
import cart from 'fa/cart-arrow-down.svg'

const styleCartText = {
  margin: '0 1em 0 0',
}

const styleCartButton = {
  fontSize: '0.618em',
  display: 'inline-flex',
}

const styleCartIcon = {
  width: '1.618em',
  height: '1.618em',
  marginRight: '0.309em',
}

export default class HeaderCartLink extends React.Component {
  render() {
    const {featuresEnabled, abbreviatedNumberOfGranulesSelected} = this.props

    console.log('featuresEnabled', featuresEnabled)

    const cartMenuItem = (
      <div key="cartMenuItem">
        <Button
          key="cartButton"
          style={styleCartButton}
          title={`${abbreviatedNumberOfGranulesSelected} Files for download`}
          text={abbreviatedNumberOfGranulesSelected}
          icon={cart}
          styleIcon={styleCartIcon}
          onClick={this.handleRedirectToCart}
        />
      </div>
    )

    if (featuresEnabled.includes(FEATURE_CART)) {
      return cartMenuItem
    }
    else {
      return null
    }
  }
}
