import React, { Component } from 'react'

import FlexColumn from '../common/FlexColumn'
import Header from './Header'
import Content from './Content'
import Footer from './Footer'

const defaultPadding = '1em'

const styleContainer = {
  minHeight: '100vh',
  width: '100%',
  overflow: 'hidden',
  // userSelect: 'none',
}

export default class Container extends Component {
  render() {
    const styles = Object.assign({}, styleContainer, this.props.style)
    return (
      <div>
        <FlexColumn
          items={[
            <Header
              content={this.props.header}
              padding={defaultPadding}
              key={'header'}
            />,
            <Content
              left={this.props.left}
              leftWidth={this.props.leftWidth}
              leftVisible={this.props.leftVisible}
              middle={this.props.middle}
              right={this.props.right}
              rightWidth={this.props.rightWidth}
              rightVisible={this.props.rightVisible}
              padding={defaultPadding}
              key={'content'}
            />,
            <Footer
              content={this.props.footer}
              padding={defaultPadding}
              key={'footer'}
            />,
          ]}
          style={styles}
        />
      </div>
    )
  }
}
