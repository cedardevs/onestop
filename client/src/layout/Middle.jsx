import React, {Component} from 'react'
import {boxShadow} from '../common/defaultStyles'

const styleMiddle = onHomePage => {
  return {
    display: 'flex',
    overflowX: 'hidden',
    overflowY: 'auto',
    boxSizing: 'border-box',
    margin: '0 auto',
    boxShadow: onHomePage ? 'none' : boxShadow,
    justifyContent: 'center',
  }
}

const styleMiddleContent = (maxWidth, backgroundColor) => {
  return {
    backgroundColor: backgroundColor,
    width: maxWidth,
    maxWidth: maxWidth,
  }
}

export default class Middle extends Component {
  render() {
    const {content, maxWidth, backgroundColor, onHomePage} = this.props
    const contentElement = (
      <div style={styleMiddle(onHomePage)}>
        <div
          key={'middle(content)'}
          style={styleMiddleContent(maxWidth, backgroundColor)}
        >
          <div>{content}</div>
        </div>
      </div>
    )
    return contentElement
  }
}
