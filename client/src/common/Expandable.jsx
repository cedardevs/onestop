import React from 'react'
import {Key} from '../utils/keyboardUtils'

const styleHideFocus = {
  outline: 'none',
}

const styleHeading = {
  display: 'flex',
  textAlign: 'left',
  alignItems: 'center',
  color: '#FFFFFF',
  cursor: 'pointer',
}

const styleHeadingShown = {}

const styleHeadingHidden = {
  borderBottom: 0,
}

const styleArrow = {
  userSelect: 'none',
}

const styleContent = {
  textAlign: 'left',
  overflow: 'hidden',
}

const styleContentShown = {
  transition: 'max-height 1.25s ease-in',
}

const styleContentHidden = {
  transition: 'max-height 1.25s ease-out',
  transitionDelay: '-1s',
}

const styleFocusDefault = {
  outline: '2px dashed white',
}

export default class Expandable extends React.Component {
  constructor(props) {
    super(props)
    this.state = {
      open: props.open || false,
      showArrow: props.showArrow,
      maxHeight: props.open ? '10000px' : 0,
      display: props.open ? 'block' : 'none',
      focusing: false,
    }
  }

  componentWillReceiveProps(nextProps) {
    if (nextProps.open !== this.state.open) {
      this.setState({
        ...this.state,
        open: nextProps.open,
        maxHeight: nextProps.open ? '10000px' : 0,
        display: nextProps.open ? 'block' : 'none',
      })
    }
  }

  toggle = () => {
    this.setState(prevState => {
      const isOpen = prevState.open
      const isDisplayed = prevState.display === 'block'
      const shouldClose = isOpen && isDisplayed
      const shouldOpen = !isOpen && !isDisplayed

      if (this.props.onToggle) {
        this.props.onToggle({open: !isOpen, value: this.props.value})
      }

      // these transitions do occasionally have timing issues, but I've only seen them when rapidly toggling a single element on and off..
      if (shouldOpen) {
        setTimeout(() => this.setState({maxHeight: '10000px'}), 15)
      }
      if (shouldClose) {
        setTimeout(() => this.setState({display: 'none'}), 500)
      }

      const immediateTransition = shouldOpen
        ? {display: 'block'}
        : shouldClose ? {maxHeight: 0} : {}
      return {open: !isOpen, ...immediateTransition}
    })
  }

  handleClick = event => {
    event.preventDefault()
    this.toggle()
  }

  handleKeyPressed = e => {
    if (e.keyCode === Key.SPACE) {
      e.preventDefault() // prevent scrolling down on space press
      this.toggle()
    }
    if (e.keyCode === Key.ENTER) {
      this.toggle()
    }
  }

  handleFocus = e => {
    this.setState(prevState => {
      return {
        ...prevState,
        focusing: true,
      }
    })
  }

  handleBlur = e => {
    this.setState(prevState => {
      return {
        ...prevState,
        focusing: false,
      }
    })
  }

  render() {
    const arrow = this.props.showArrow ? this.state.open ? (
      <span>&nbsp;&#9660;</span>
    ) : (
      <span>&nbsp;&#9654;</span>
    ) : null

    const styleHeadingHide = this.state.open
      ? styleHeadingShown
      : styleHeadingHidden
    const styleContentVisibilityTransition = this.state.open
      ? styleContentShown
      : styleContentHidden
    const styleContentVisibility = {
      maxHeight: this.state.maxHeight,
      display: this.state.display,
    }

    const ariaHidden = this.state.display === 'none'
    const tabbable = !(this.props.tabbable === false)
    const tabIndex = tabbable ? '0' : '-1'
    const role = tabbable ? 'button' : undefined
    const ariaExpanded = tabbable ? this.state.open : undefined

    const {styleFocus, styleWrapper} = this.props

    const stylesMerged = {
      ...styleHeading,
      ...this.props.styleHeading,
      ...styleHeadingHide,
      ...styleHideFocus, // focus is shown on an interior element instead
    }

    const styleFocused = {
      ...(this.state.focusing ? {...styleFocusDefault, ...styleFocus} : {}),
    }

    const styleContentMerged = {
      ...styleContent,
      ...this.props.styleContent,
      ...styleContentVisibilityTransition,
      ...styleContentVisibility,
    }

    return (
      <div style={styleWrapper}>
        <div
          style={stylesMerged}
          onClick={this.handleClick}
          onKeyDown={this.handleKeyPressed}
          onFocus={this.handleFocus}
          onBlur={this.handleBlur}
          tabIndex={tabIndex}
          role={role}
          aria-expanded={ariaExpanded}
        >
          <div style={styleFocused}>{this.props.heading}</div>
          <div aria-hidden="true" style={styleArrow}>
            {arrow}
          </div>
        </div>

        <div style={styleContentMerged} aria-hidden={ariaHidden}>
          {this.props.content}
        </div>
      </div>
    )
  }
}
