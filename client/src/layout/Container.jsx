import React, {Component} from 'react'

import FlexColumn from '../common/FlexColumn'
import Header from './Header'
import Earth from './Earth'
import Content from './Content'
import Footer from './Footer'

const defaultPadding = '1em'

const styleContainer = {
  minHeight: '100vh',
  width: '100%',
  overflow: 'hidden',
}

export default class Container extends Component {
  render() {
    const {
      style,
      header,
      left,
      leftWidth,
      leftVisible,
      middle,
      middleMaxWidth,
      middleBackgroundColor,
      onHomePage,
      right,
      rightWidth,
      rightVisible,
      footer,
    } = this.props
    const earth = onHomePage ? <Earth key={'earth'} /> : null
    const styles = Object.assign({}, styleContainer, style)
    return (
      <div>
        <FlexColumn
          items={[
            <Header content={header} padding={defaultPadding} key={'header'} />,
            earth,
            <Content
              left={left}
              leftWidth={leftWidth}
              leftVisible={leftVisible}
              middle={middle}
              middleMaxWidth={middleMaxWidth}
              middleBackgroundColor={middleBackgroundColor}
              onHomePage={onHomePage}
              right={right}
              rightWidth={rightWidth}
              rightVisible={rightVisible}
              padding={defaultPadding}
              key={'content'}
            />,
            <Footer content={footer} padding={defaultPadding} key={'footer'} />,
          ]}
          style={styles}
        />
      </div>
    )
  }
}
