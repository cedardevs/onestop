import React, {Component} from 'react'

import FlexColumn from '../common/FlexColumn'
import Header from './Header'
import Earth from './Earth'
import Content from './Content'
import Footer from './Footer'
import {Route, Switch} from 'react-router'

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
      right,
      rightWidth,
      rightVisible,
      footer,
    } = this.props
    const earth = (
      <Switch key={'earth-switch'}>
        <Route path="/" exact>
          <Earth key={'earth'} />
        </Route>
      </Switch>
    )
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
