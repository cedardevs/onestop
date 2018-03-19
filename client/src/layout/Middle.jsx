import React, {Component} from 'react'

const styleMiddle = (maxWidth, border) => {
  return {
    display: 'flex',
    alignItems: 'stretch',
    minWidth: 'min-content',
    width: maxWidth,
    maxWidth: maxWidth,
    overflowX: 'hidden',
    overflowY: 'auto',
    boxSizing: 'border-box',
    margin: '0 auto',
    justifyContent: 'center',
  }
}

const styleMiddleContent = backgroundColor => {
  return {
    backgroundColor: backgroundColor,
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
          <div style={{width: '100%', flex: 'initial'}}>{content}</div>
        </div>
      </div>
    )
    return contentElement
  }
}
