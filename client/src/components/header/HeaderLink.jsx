import React from 'react'
import {Link, withRouter} from 'react-router-dom'
import {Key} from '../../utils/keyboardUtils'
import {goTo} from '../../utils/urlUtils'

export const HEADER_LINK_CLASS = 'headerLinkClass'

const styleLink = {
  textDecoration: 'none',
  color: '#d7d7d7',
  fontWeight: 300,
  transition: 'color 0.3s ease',
  paddingRight: '0.309em',
  paddingLeft: '0.309em',
}

const styleLinkHover = {
  color: '#21ABE2',
}

const styleLinkFocusing = {
  outline: '2px dashed #d7d7d7',
}

const styleLinkKeying = {
  color: '#277cb2',
}

class HeaderLink extends React.Component {
  UNSAFE_componentWillMount() {
    this.setState({
      hovering: false,
      focusing: false,
      keying: false,
    })
  }

  handleMouseOver = event => {
    const {onMouseOver} = this.props
    if (onMouseOver) {
      onMouseOver(event)
    }
    this.setState(prevState => {
      return {
        ...prevState,
        hovering: true,
      }
    })
  }

  handleMouseOut = event => {
    const {onMouseOut} = this.props
    if (onMouseOut) {
      onMouseOut(event)
    }
    this.setState(prevState => {
      return {
        ...prevState,
        hovering: false,
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

  setKeying = isKeying => {
    this.setState(prevState => {
      return {
        ...prevState,
        keying: isKeying,
      }
    })
  }

  handleKeyDown = e => {
    if (e.keyCode === Key.SPACE) {
      e.preventDefault() // prevent scrolling down on space press
      this.setKeying(true)
    }
    if (e.keyCode === Key.ENTER) {
      this.setKeying(true)
    }
  }

  handleKeyUp = e => {
    const {history, location, to} = this.props
    if (e.keyCode === Key.SPACE) {
      e.preventDefault() // prevent scrolling down on space press
      this.setKeying(false)
      if (location.pathname !== to) {
        goTo(history, {pathname: to})
      }
    }
    if (e.keyCode === Key.ENTER) {
      this.setKeying(false)
      if (location.pathname !== to) {
        goTo(history, {pathname: to})
      }
    }
  }

  render() {
    const {to, isExternalLink, title} = this.props

    const styleLinkMerged = {
      ...styleLink,
      ...(this.state.hovering ? styleLinkHover : {}),
      ...(this.state.focusing ? styleLinkFocusing : {}),
      ...(this.state.keying ? styleLinkKeying : {}),
    }

    const link = isExternalLink ? (
      <a
        href={to}
        style={styleLinkMerged}
        onMouseOver={this.handleMouseOver}
        onMouseOut={this.handleMouseOut}
        onFocus={this.handleFocus}
        onBlur={this.handleBlur}
        onKeyDown={this.handleKeyDown}
        onKeyUp={this.handleKeyUp}
        className={HEADER_LINK_CLASS}
      >
        {title}
      </a>
    ) : (
      <Link
        to={to}
        style={styleLinkMerged}
        onMouseOver={this.handleMouseOver}
        onMouseOut={this.handleMouseOut}
        onFocus={this.handleFocus}
        onBlur={this.handleBlur}
        onKeyDown={this.handleKeyDown}
        onKeyUp={this.handleKeyUp}
        className={HEADER_LINK_CLASS}
        title={title}
      >
        {this.props.children}
      </Link>
    )

    return link
  }
}

export default withRouter(HeaderLink)
