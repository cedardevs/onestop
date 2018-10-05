import React from 'react'
import {withRouter} from 'react-router-dom'
import {boxShadow} from '../common/defaultStyles'
import cart from 'fa/cart-arrow-down.svg'

import AnimateHeight from 'react-animate-height'
import Button from '../common/input/Button'
import { Key } from '../utils/keyboardUtils'
import FocusManager from '../common/FocusManager'
const ANIMATION_DURATION = 200

const styleExtraMenu = {
  position: 'absolute',
  right: 0,
}

const styleExtraMenuContent = {
  backgroundColor: '#222C37',
  padding: ' 0 1em 1em 1em',
  borderBottomLeftRadius: '0.618em',
  boxShadow: boxShadow,
}

const styleExtraMenuList = {
  listStyleType: 'none',
  margin: 0,
  paddingInlineStart: 0,
}

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

const styleSeparatorWrapper = {
  display: 'flex',
  justifyContent: 'flex-end',
  margin: '0 0 0.618em 0',
}

const styleSeparator = {
  height: '1px',
  background: 'white',
  width: '0%',
  transition: 'width 0.2s',
}

const styleSeparatorOpen = {
  width: '100%',
}

class HeaderDropdownMenu extends React.Component {

  handleRedirectToCart = () => {
    const {history, location, setOpen} = this.props
    if (location.pathname !== '/cart') {
      history.push('/cart')
      setOpen(false)
    }
  }

  handleAnimationStart = open => {
    if (!open) {
      // animate separator invisible
    }
  }

  handleAnimationEnd = open => {
    if (open) {
      // animate separator visible
    }
  }

  handleKeyDown = e => {
    if (e.keyCode === Key.ESCAPE) {

    }
  }

  handleTotalFocus = e => {
    console.log("handleTotalFocus::e:", e)
  }

  handleTotalBlur = e => {
    console.log("handleTotalBlur::e:", e)
    const { setOpen } = this.props
    if(setOpen) {
      setOpen(false)
    }
  }

  render() {
    const {open, cartEnabled, abbreviatedNumberOfGranulesSelected} = this.props

    const stylesSeparatorMerged = {
      ...styleSeparator,
      ...(open ? styleSeparatorOpen : {}),
    }

    const shoppingCartMenuItem = (
      <div key="cartMenuItem">
        <span style={styleCartText} role="alert">
          Files for download
        </span>
        <Button
          key="cartButton"
          id="cartButton"
          style={styleCartButton}
          title={`${abbreviatedNumberOfGranulesSelected} Files for download`}
          text={abbreviatedNumberOfGranulesSelected}
          icon={cart}
          styleIcon={styleCartIcon}
          onClick={this.handleRedirectToCart}
        />
      </div>
    )

    const menuItems = []
    if (cartEnabled) {
      menuItems.push(shoppingCartMenuItem)
    }

    const extraMenuContent = (
      <div style={styleExtraMenuContent}>
        <div style={styleSeparatorWrapper}>
          <div style={stylesSeparatorMerged} />
        </div>
        <ul style={styleExtraMenuList}>{menuItems}</ul>
      </div>
    )

    return (
      <FocusManager onFocus={this.handleTotalFocus} onBlur={this.handleTotalBlur}>
        <AnimateHeight
          duration={ANIMATION_DURATION}
          height={open ? 'auto' : 0}
          style={styleExtraMenu}
          onAnimationStart={() => this.handleAnimationStart(open)}
          onAnimationEnd={() => this.handleAnimationEnd(open)}
        >
          {extraMenuContent}
        </AnimateHeight>
      </FocusManager>
    )
  }
}

export default withRouter(HeaderDropdownMenu)
