import React, {Component} from 'react'

const styleMiddle = (maxWidth, border) => {
  return {
    display: 'flex',
    minWidth: 'min-content',
    maxWidth: maxWidth,
    overflowX: 'hidden',
    overflowY: 'auto',
    border: border,
    boxSizing: 'border-box',
    margin: '0 auto',
  }
}

const styleMiddleContent = backgroundColor => {
  return {
    backgroundColor: backgroundColor,
    margin: '0 auto',
  }
}

export default class Middle extends Component {
  render() {
    const {content, maxWidth, border, backgroundColor} = this.props
    const contentElement = (
      <div style={styleMiddle(maxWidth, border)}>
        <div
          key={'middle(content)'}
          style={styleMiddleContent(backgroundColor)}
        >
          {content}
        </div>
      </div>
    )
    return contentElement
  }
}
