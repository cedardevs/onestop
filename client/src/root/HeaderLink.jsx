import React from 'react'
import {Link, withRouter} from 'react-router-dom'
import { Key } from '../utils/keyboardUtils'

const styleLink = {
  textDecoration: 'none',
  color: '#d7d7d7',
  fontWeight: 300,
  transition: 'color 0.3s ease',
  paddingRight: '0.309em',
  paddingLeft: '0.309em',
}

const styleLinkHover = {
  color: '#277cb2',
}

const styleLinkFocusing = {
  outline: '2px dashed #d7d7d7',
}

const styleLinkKeying = {
  color: '#277cb2',
}

class HeaderLink extends React.Component {
  componentWillMount() {
    this.setState({
      hovering: false,
      focusing: false,
      keying: false
    })
  }

  handleMouseOver = event => {
    this.setState(prevState => {
      return {
        ...prevState,
        hovering: true,
      }
    })
  }

  handleMouseOut = event => {
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

  setKeying = (isKeying) => {
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
    const { history, location, to } = this.props;
    if (e.keyCode === Key.SPACE) {
      e.preventDefault() // prevent scrolling down on space press
      this.setKeying(false)
      if(location.pathname !== to) {
        history.push(this.props.to)
      }
    }
    if (e.keyCode === Key.ENTER) {
      this.setKeying(false)
      if(location.pathname !== to) {
        history.push(this.props.to)
      }
    }
  }

  render() {
    const {to} = this.props

    const styleLinkMerged = {
      ...styleLink,
      ...(this.state.hovering ? styleLinkHover : {}),
      ...(this.state.focusing ? styleLinkFocusing : {}),
      ...(this.state.keying ? styleLinkKeying : {})
    }

    return (
      <Link
        to={to}
        style={styleLinkMerged}
        onMouseOver={this.handleMouseOver}
        onMouseOut={this.handleMouseOut}
        onFocus={this.handleFocus}
        onBlur={this.handleBlur}
        onKeyDown={this.handleKeyDown}
        onKeyUp={this.handleKeyUp}
      >
        {this.props.children}
      </Link>
    )
  }
}

export default withRouter(HeaderLink)