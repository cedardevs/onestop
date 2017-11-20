import React, { Component } from 'react'

const styleHeader = {
  flex: '0 0 auto',
  zIndex: 2,
}

export default class Header extends Component {
  render() {
    return <div style={styleHeader}>{this.props.content}</div>
  }
}
