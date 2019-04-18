import React, {Component} from 'react'
import FlexRow from '../common/FlexRow'
import Left from './Left'
import Middle from './Middle'
import Right from './Right'

import {Route, Switch} from 'react-router'
import {ROUTE} from '../utils/urlUtils'

const styleContent = {
  display: 'flex',
  flex: '1 1 auto',
  position: 'relative',
  justifyContent: 'space-between',
  alignItems: 'stretch',
  width: '100%',
}

export default class Content extends Component {
  render() {
    const {
      style,
      padding,
      left,
      leftWidth,
      leftVisible,
      right,
      rightWidth,
      rightVisible,
      middle,
      middleMaxWidth,
    } = this.props
    const styles = Object.assign({}, styleContent, style)
    return (
      <FlexRow
        items={[
          <Switch key="left">
            <Route path={ROUTE.search.path} exact>
              <Left
                content={left}
                width={leftWidth}
                padding={padding}
                visible={leftVisible}
              />
            </Route>
          </Switch>,
          <Middle
            content={middle}
            maxWidth={middleMaxWidth}
            padding={padding}
            key={'middle'}
          />,
          right ? (
            <Right
              content={right}
              width={rightWidth}
              padding={padding}
              visible={rightVisible}
              key={'right'}
            />
          ) : null,
        ]}
        style={styles}
      />
    )
  }
}
