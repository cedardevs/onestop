import React from 'react'
import AnimateHeight from 'react-animate-height'
import {Key} from '../utils/keyboardUtils'

const ANIMATION_DURATION = 200

const styleHeadingDefault = (open, borderRadius) => {
  const borderRadiusEffective = open
    ? `${borderRadius} ${borderRadius} 0 0`
    : `${borderRadius}`

  return {
    display: 'flex',
    textAlign: 'left',
    alignItems: 'center',
    color: '#FFFFFF',
    cursor: 'pointer',
    borderRadius: borderRadius ? borderRadiusEffective : 'none',
    borderBottom: 0,
    outline: 'none', // focus is shown on an interior element instead
    transition: `border-radius ${ANIMATION_DURATION}ms ease`,
  }
}

const styleArrow = {
  userSelect: 'none',
}

const styleContentDefault = (open, display, borderRadius) => {
  const borderRadiusContentOpen = `0 0 ${borderRadius} ${borderRadius}`

  return {
    textAlign: 'left',
    borderRadius: borderRadius ? borderRadiusContentOpen : 'none',
  }
}

const styleFocusDefault = (open, borderRadius) => {
  const borderRadiusEffective = open
    ? `${borderRadius} 0 0 0`
    : `${borderRadius} 0 0 ${borderRadius}`

  return {
    outline: '2px dashed white',
    borderRadius: borderRadius ? borderRadiusEffective : 'none',
  }
}

export default class Expandable extends React.Component {
  constructor(props) {
    super(props)
    this.state = {
      open: props.open || false,
      showArrow: props.showArrow,
      focusing: false,
      display: props.open ? 'block' : 'none',
    }
  }

  componentWillReceiveProps(nextProps) {
    if (nextProps.open !== this.state.open) {
      this.setState(prevState => {
        return {
          ...prevState,
          open: !!nextProps.open,
        }
      })
    }
  }

  toggle = () => {
    const {onToggle, value, disabled} = this.props

    if (disabled) {
      return
    }

    this.setState(prevState => {
      const newOpen = !prevState.open

      // if provided, tell the caller of expandable we're updating the open state
      if (onToggle) {
        onToggle({open: newOpen, value: value})
      }

      return {
        ...prevState,
        open: newOpen,
      }
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

  handleAnimationStart = open => {
    this.setState(prevState => {
      return {
        ...prevState,
        display: 'block',
      }
    })
  }

  handleAnimationEnd = open => {
    if (!open) {
      this.setState(prevState => {
        return {
          ...prevState,
          display: 'none',
        }
      })
    }
  }

  render() {
    const {
      showArrow,
      styleFocus,
      styleWrapper,
      styleHeading,
      heading,
      styleContent,
      content,
      borderRadius,
    } = this.props
    const {open, display, focusing} = this.state

    const arrow = showArrow ? open ? (
      <span>&nbsp;&#9660;</span>
    ) : (
      <span>&nbsp;&#9654;</span>
    ) : null

    const ariaHidden = display === 'none'
    const tabbable = !(this.props.tabbable === false)
    const tabIndex = tabbable ? '0' : '-1'
    const role = tabbable ? 'button' : undefined
    const ariaExpanded = tabbable ? open : undefined

    const stylesHeadingMerged = {
      ...styleHeadingDefault(open, borderRadius),
      ...styleHeading,
    }

    const styleFocused = {
      ...(focusing
        ? {...styleFocusDefault(open, borderRadius), ...styleFocus}
        : {}),
    }

    const styleContentMerged = {
      ...styleContentDefault(open, display, borderRadius),
      ...styleContent,
    }

    const headingEffective = heading ? (
      <div
        style={stylesHeadingMerged}
        onClick={this.handleClick}
        onKeyDown={this.handleKeyPressed}
        onFocus={this.handleFocus}
        onBlur={this.handleBlur}
        tabIndex={tabIndex}
        role={role}
        aria-expanded={ariaExpanded}
      >
        <div style={styleFocused}>{heading}</div>
        <div aria-hidden="true" style={styleArrow}>
          {arrow}
        </div>
      </div>
    ) : null

    return (
      <div style={styleWrapper}>
        {headingEffective}

        <div style={styleContentMerged} aria-hidden={ariaHidden}>
          <AnimateHeight
            duration={ANIMATION_DURATION}
            height={open ? 'auto' : 0}
            onAnimationStart={() => this.handleAnimationStart(open)}
            onAnimationEnd={() => this.handleAnimationEnd(open)}
          >
            {content}
          </AnimateHeight>
        </div>
      </div>
    )
  }
}
