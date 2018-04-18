import React from 'react'
import {Link} from 'react-router'

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

export default class HeaderLink extends React.Component {
  componentWillMount() {
    this.setState({
      hovering: false,
      focusing: false,
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

  render() {
    const {title, href} = this.props

    const styleLinkMerged = {
      ...styleLink,
      ...(this.state.hovering ? styleLinkHover : {}),
      ...(this.state.focusing ? styleLinkFocusing : {}),
    }

    return (
      <Link
        href={href}
        style={styleLinkMerged}
        onMouseOver={this.handleMouseOver}
        onMouseOut={this.handleMouseOut}
        onFocus={this.handleFocus}
        onBlur={this.handleBlur}
      >
        {this.props.children}
      </Link>
    )
  }
}
