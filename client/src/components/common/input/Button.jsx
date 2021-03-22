import React from 'react'
import {FilterColors} from '../../../style/defaultStyles'

const styleDefault = iconAndText => {
  return {
    display: 'flex',
    justifyContent: 'center',
    alignItems: 'center',
    color: 'white',
    background: '#277CB2', // $color_primary
    borderRadius: '0.309em',
    border: 'transparent',
    textAlign: 'center',
    fontSize: '1.25em',
    margin: 0,
    padding: iconAndText ? '0.309em' : '0.618em',
  }
}

const styleHoverDefault = {
  background: 'linear-gradient(#277CB2, #28323E)',
}

const stylePressDefault = {}

const styleFocusDefault = {
  outline: '2px dashed white',
}

const styleDisabledDefault = {
  background: FilterColors.DISABLED_BACKGROUND,
}

const styleIconPadding = {
  padding: '0 0.618em',
}

export default class Button extends React.Component {
  UNSAFE_componentWillMount() {
    this.setState({
      hovering: false,
      pressing: false,
      pressingGlobal: false,
      focusing: false,
    })
  }

  componentDidMount() {
    document.addEventListener('mouseup', this.handleGlobalMouseUp, false)
    document.addEventListener('mousedown', this.handleGlobalMouseDown, false)
  }

  componentWillUnmount() {
    document.removeEventListener('mouseup', this.handleGlobalMouseUp, false)
    document.removeEventListener('mousedown', this.handleGlobalMouseDown, false)
  }

  handleMouseOver = event => {
    this.setState(prevState => {
      return {
        ...prevState,
        hovering: true,
        pressing: prevState.pressingGlobal,
      }
    })
  }

  handleMouseOut = event => {
    this.setState(prevState => {
      return {
        ...prevState,
        hovering: false,
        pressing: false,
      }
    })
  }

  handleGlobalMouseUp = event => {
    this.setState(prevState => {
      return {
        ...prevState,
        pressingGlobal: false,
      }
    })
  }

  handleGlobalMouseDown = event => {
    this.setState(prevState => {
      return {
        ...prevState,
        pressingGlobal: true,
      }
    })
  }

  handleMouseDown = event => {
    this.setState(prevState => {
      return {
        ...prevState,
        pressing: true,
      }
    })
  }

  handleMouseUp = event => {
    this.setState(prevState => {
      return {
        ...prevState,
        pressing: false,
      }
    })
  }

  handleFocus = event => {
    const {onFocus} = this.props
    if (onFocus) {
      onFocus(event)
    }
    this.setState(prevState => {
      return {
        ...prevState,
        focusing: true,
      }
    })
  }

  handleBlur = event => {
    const {onBlur} = this.props
    if (onBlur) {
      onBlur(event)
    }
    this.setState(prevState => {
      return {
        ...prevState,
        focusing: false,
      }
    })
  }

  render() {
    const {
      id,
      text,
      styleText,
      icon,
      iconAfter,
      iconPadding,
      styleIcon,
      onClick,
      style,
      styleHover,
      stylePress,
      styleFocus,
      title,
      ariaExpanded,
      ariaSelected,
      ariaCurrent,
      disabled,
      styleDisabled,
      role,
      type,
    } = this.props

    const iconAndText = icon && text

    const stylesMerged = {
      ...styleDefault(iconAndText),
      ...style,
      ...(this.state.hovering ? {...styleHoverDefault, ...styleHover} : {}),
      ...(this.state.pressing ? {...stylePressDefault, ...stylePress} : {}),
      ...(this.state.focusing ? {...styleFocusDefault, ...styleFocus} : {}),
      ...(disabled ? {...styleDisabledDefault, ...styleDisabled} : {}),
      ...(icon && !text
        ? iconPadding ? {padding: iconPadding} : styleIconPadding
        : {}),
    }

    const styleIconResolved = styleIcon
      ? styleIcon
      : {width: '2em', height: '2em', marginRight: iconAndText ? '0.618em' : 0}

    return (
      <button
        id={id}
        role={role}
        style={stylesMerged}
        onClick={onClick}
        onMouseOver={this.handleMouseOver}
        onMouseOut={this.handleMouseOut}
        onMouseDown={this.handleMouseDown}
        onMouseUp={this.handleMouseUp}
        onFocus={this.handleFocus}
        onBlur={this.handleBlur}
        title={title}
        aria-expanded={ariaExpanded}
        aria-selected={ariaSelected}
        aria-current={ariaCurrent}
        aria-label={title || text}
        disabled={disabled}
        type={type || 'submit'}
      >
        {icon && !iconAfter && !this.props.children ? (
          <img src={icon} style={styleIconResolved} aria-hidden={true} alt="" />
        ) : null}
        {text && !this.props.children ? (
          <span style={styleText}>{text}</span>
        ) : null}
        {icon && iconAfter && !this.props.children ? (
          <img src={icon} style={styleIconResolved} aria-hidden={true} alt="" />
        ) : null}
        {this.props.children ? <div>{this.props.children}</div> : null}
      </button>
    )
  }
}
