import React, {Component} from 'react'

const styleMiddle = () => {
  return {
    display: 'flex',
    overflowX: 'hidden',
    overflowY: 'auto',
    boxSizing: 'border-box',
    margin: '0 auto',
  }
}

export default class Middle extends Component {
  render() {
    const {content} = this.props
    const contentElement = <div style={styleMiddle()}>{content}</div>
    return contentElement
  }
}
