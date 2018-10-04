import React from 'react'
import { boxShadow } from '../common/defaultStyles'
import cart from 'fa/cart-arrow-down.svg'

import AnimateHeight from 'react-animate-height'
const ANIMATION_DURATION = 200

const styleExtraMenu = {
  position: 'absolute',
  right: 0
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
  paddingInlineStart: 0
}

const styleCartButton = {
  fontSize: '0.618em'
}

const styleCartIcon = {
  width: '1.618em',
  height: '1.618em',
  marginRight: '0.309em'
}

const styleSeparatorWrapper = {
  display: 'flex',
  justifyContent: 'flex-end',
  margin: '0 0 0.618em 0'
}

const styleSeparator = {
  height: '1px',
  background: 'white',
  width: '0%',
  transition: 'width 0.2s'
}

const styleSeparatorOpen = {
  width: '100%'
}

export default class HeaderDropdownMenu extends React.Component {

  handleRedirectToCart = () => {
    const { history } = this.props
    history.push('/cart')
  }

  handleAnimationStart = open => {
    if(!open) {
      // animate separator invisible
    }
  }

  handleAnimationEnd = open => {
    if(open) {
      // animate separator visible
    }
  }

  render() {

    const {open} = this.props

    const stylesSeparatorMerged = {
      ...styleSeparator,
      ...(open ? styleSeparatorOpen : {}),
    }

    const extraMenuContent = (
        <div style={styleExtraMenuContent}>

          <div style={styleSeparatorWrapper}>
            <div style={stylesSeparatorMerged} />
          </div>

          <ul style={styleExtraMenuList}>
            <li>item 1</li>
            <li>item 2</li>
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