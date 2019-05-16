import React from 'react'

const styleDisclaimer = {
  flex: '0 0 auto',
  zIndex: 2,
}

export default class Disclaimer extends React.Component {
  render() {
    const {content} = this.props
    return <div style={styleDisclaimer}>{content}</div>
  }
}
