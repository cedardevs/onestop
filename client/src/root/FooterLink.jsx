import React from 'react'
import A from '../common/link/Link'

const styleLink = {
  color: 'white',
  backgroundColor: 'transparent',
  textDecoration: 'none',
  padding: '0px 10px',
}

const styleLinkHover = {
  textDecoration: 'underline',
}

class FooterLink extends React.Component {
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

  render() {
    const {href, target, title, style} = this.props

    const styleLinkMerged = {
      ...styleLink,
      ...style,
      ...(this.state.hovering ? styleLinkHover : {}),
    }

    return (
      <A
        style={styleLinkMerged}
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
