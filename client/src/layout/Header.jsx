import React, {Component} from 'react'

const styleHeader = {
  flex: '0 0 auto',
  zIndex: 2,
}

export default class Header extends Component {
  render() {
    const {content} = this.props
    return <div style={styleHeader}>{content}</div>
  }
}
