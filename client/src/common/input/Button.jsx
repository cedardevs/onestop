import React, { Component } from 'react'

const styleDefault = {
  color: 'white',
  background: '#277CB2', // $color_primary
  borderRadius: '0.309em',
  border: 'transparent',
  textAlign: 'center',
  fontSize: '1.25em',
  margin: 0,
  padding: '0.618em',
}

const styleHoverDefault = {
  background: 'linear-gradient(#277CB2, #28323E)',
}

const stylePressDefault = {
}

const styleFocusDefault = {
    outline: '2px dashed white',
}

const styleIconPadding = {
  padding: "0 0.618em",
}

export default class Button extends Component {
  componentWillMount() {
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
    this.setState(prevState => {
      return {
        ...prevState,
        focusing: true,
      }
    })
  }

  handleBlur = event => {
    this.setState(prevState => {
      return {
        ...prevState,
        focusing: false,
      }
    })
  }

  render() {
    const {
      text,
      icon,
      styleIcon,
      onClick,
      style,
      styleHover,
      stylePress,
      styleFocus,
      title
    } = this.props

    const stylesMerged = {
      ...styleDefault,
      ...style,
      ...(this.state.hovering ? { ...styleHoverDefault, styleHover } : {}),
      ...(this.state.pressing ? { ...stylePressDefault, stylePress } : {}),
      ...(this.state.focusing ? { ...styleFocusDefault, styleFocus } : {}),
      ...(icon && !text ? styleIconPadding : {}),
    }

    const styleIconResolved = styleIcon ? styleIcon : { width: '2em', height: '2em' }

    return (
      <button
        style={stylesMerged}
        onClick={onClick}
        onMouseOver={this.handleMouseOver}
        onMouseOut={this.handleMouseOut}
        onMouseDown={this.handleMouseDown}
        onMouseUp={this.handleMouseUp}
        onFocus={this.handleFocus}
        onBlur={this.handleBlur}
        title={title}
      >
        {icon ? (
          <img src={icon} style={styleIconResolved} aria-hidden={true} alt={title}/>
        ) : null}
        {text}
      </button>
    )
  }
}
