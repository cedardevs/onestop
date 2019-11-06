import React from 'react'
import A from '../common/link/Link'
import {Link} from 'react-router-dom'

const styleLink = {
  color: 'white',
  backgroundColor: 'transparent',
  textDecoration: 'none',
  padding: '0px 10px',
}

const styleLinkHover = {
  textDecoration: 'underline',
}

const styleAFocus = {
  outline: '2px dashed white',
}

class FooterLink extends React.Component {
  UNSAFE_componentWillMount() {
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

  render() {
    const {href, to, target, title, style} = this.props

    const styleLinkMerged = {
      ...styleLink,
      ...style,
      ...(this.state.hovering ? styleLinkHover : {}),
    }

    if (to) {
      return (
        <Link
          style={styleLinkMerged}
          to={to}
          title={title ? title : null}
          onMouseOver={this.handleMouseOver}
          onMouseOut={this.handleMouseOut}
        >
          {this.props.children}
        </Link>
      )
    }
    return (
      <A
        style={styleLinkMerged}
        styleFocus={styleAFocus}
        href={href}
        target={target ? target : null}
        title={title ? title : null}
        onMouseOver={this.handleMouseOver}
        onMouseOut={this.handleMouseOut}
      >
        {this.props.children}
      </A>
    )
  }
}

export default FooterLink
