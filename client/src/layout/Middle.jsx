import React, { Component } from 'react'
import FlexColumn from '../common/FlexColumn'
import Tabs from './Tabs'

const styleMiddle = {
  display: 'flex',
  width: '100%',
  minWidth: 'min-content',
  overflowY: 'auto',
}

const styleMiddleContent = padding => {
  return {
    padding: 0,
  }
}

export default class Middle extends Component {
  render() {
    const tabs = (
      <Tabs
        key={'middle(tabs)'}
        titles={this.props.tabs}
        activeTitle={this.props.currentTab}
        onChange={this.props.onTabChange}
        padding={this.props.padding}
      />
    )

    const content = (
      <div
        key={'middle(content)'}
        style={styleMiddleContent(this.props.padding)}
      >
        {this.props.content}
      </div>
    )

    return <FlexColumn style={styleMiddle} items={[tabs, content]} />
  }
}
