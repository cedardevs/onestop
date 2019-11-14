import React from 'react'
import {buildGovExternalOnClick} from '../../../utils/urlUtils'

const styleLink = {
  cursor: 'pointer',
}

const styleHoverDefault = {}

const stylePressDefault = {}

const styleFocusDefault = {}

class Link extends React.Component {
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
      href,
      target,
      onClick,
      style,
      styleHover,
      stylePress,
      styleFocus,
      ...others
    } = this.props

    const stylesMerged = {
      ...styleLink,
      ...style,
      ...(this.state.hovering ? {...styleHoverDefault, ...styleHover} : {}),
      ...(this.state.pressing ? {...stylePressDefault, ...stylePress} : {}),
      ...(this.state.focusing ? {...styleFocusDefault, ...styleFocus} : {}),
    }

    return (
      <a
        href={href}
        target={target}
        onClick={buildGovExternalOnClick(href, target, onClick)}
        style={stylesMerged}
        onMouseOver={this.handleMouseOver}
        onMouseOut={this.handleMouseOut}
        onMouseDown={this.handleMouseDown}
        onMouseUp={this.handleMouseUp}
        onFocus={this.handleFocus}
        onBlur={this.handleBlur}
        {...others}
      >
        {this.props.children}
      </a>
    )
  }
}

export default Link
