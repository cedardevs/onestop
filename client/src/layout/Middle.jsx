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

const styleMiddleContent = backgroundColor => {
  return {
    backgroundColor: backgroundColor,
  }
}

export default class Middle extends Component {
  render() {
    const {content, backgroundColor, onHomePage} = this.props
    const contentElement = (
      <div style={styleMiddle(onHomePage)}>
        <div
          key={'middle(content)'}
          style={styleMiddleContent(backgroundColor)}
        >
          <div>{content}</div>
        </div>
      </div>
    )
    return contentElement
  }
}
