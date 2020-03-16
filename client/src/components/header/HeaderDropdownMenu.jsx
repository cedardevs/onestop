import React from 'react'
import {withRouter} from 'react-router-dom'
import {boxShadow} from '../../style/defaultStyles'
import cart from 'fa/cart-arrow-down.svg'
import {goTo} from '../../utils/urlUtils'

import AnimateHeight from 'react-animate-height/lib/index'
import Button from '../common/input/Button'
import {FEATURE_CART, HEADER_DROPDOWN_FEATURES} from '../../utils/featureUtils'
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
  constructor(props) {
    super(props)
    this.insideRef = React.createRef()
    this.menuListRef = React.createRef()
  }

  componentDidUpdate(prevProps, prevState) {
    if (this.menuListRef && this.props.open) {
      const menuListElement = this.menuListRef.current
      if (menuListElement) {
        const focusable = menuListElement.querySelectorAll(
          'button, [href], input, select, textarea, [tabindex]:not([tabindex="-1"])'
        )
        if (focusable.length > 0) {
          const firstFocusable = focusable[0]
          firstFocusable.focus()
        }
      }
    }
  }

  handleRedirectToCart = () => {
    const {history, location, setOpen} = this.props
    if (location.pathname !== '/cart') {
      goTo(history, {pathname: to})
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

  handleOnBlur = event => {
    const {setOpen} = this.props
    let delegateSetOpen = false
    if (event.relatedTarget) {
      if (event.relatedTarget.id === 'headerDropdownMenuButton') {
        delegateSetOpen = true
      }
    }
    if (setOpen && !delegateSetOpen) {
      setOpen(false)
    }
  }

  render() {
    const {
      open,
      featuresEnabled,
      abbreviatedNumberOfGranulesSelected,
    } = this.props

    const stylesSeparatorMerged = {
      ...styleSeparator,
      ...(open ? styleSeparatorOpen : {}),
    }

    const cartMenuItem = (
      <div key="cartMenuItem">
        <span style={styleCartText} role="alert">
          Files for download
        </span>
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

    const potentialMenuItems = {
      [FEATURE_CART]: cartMenuItem,
    }

    const enabledMenuFeatures = featuresEnabled.filter(f =>
      HEADER_DROPDOWN_FEATURES.includes(f)
    )

    const menuItems = enabledMenuFeatures.map(feature => {
      return potentialMenuItems[feature]
    })

    const extraMenuContent = (
      <div
        style={styleExtraMenuContent}
        ref={this.insideRef}
        onBlur={this.handleOnBlur}
      >
        <div style={styleSeparatorWrapper}>
          <div style={stylesSeparatorMerged} />
        </div>
        <ul style={styleExtraMenuList} ref={this.menuListRef}>
          {menuItems}
        </ul>
      </div>
    )

    return (
      <AnimateHeight
        duration={ANIMATION_DURATION}
        height={open ? 'auto' : 0}
        style={styleExtraMenu}
        onAnimationStart={() => this.handleAnimationStart(open)}
        onAnimationEnd={() => this.handleAnimationEnd(open)}
      >
        {extraMenuContent}
      </AnimateHeight>
    )
  }
}

export default withRouter(HeaderDropdownMenu)
