import React from 'react'
import {Link} from 'react-router'

const styleLink = {
  textDecoration: 'none',
  color: '#d7d7d7',
  fontWeight: 300,
  transition: 'color 0.3s ease'
}

const styleLinkHover = {
  color: '#277cb2'
}

export default class HeaderLink extends React.Component
{
  componentWillMount() {
    this.setState({
      hovering: false,
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

  render()
  {
    const { title, to } = this.props

    const styleLinkMerged = {
        ...styleLink,
        ...(this.state.hovering ? styleLinkHover : {})
    }

    return (
        <Link title={title} to={to} style={styleLinkMerged} onMouseOver={this.handleMouseOver} onMouseOut={this.handleMouseOut}>
          {this.props.children}
        </Link>
    )
  }
}