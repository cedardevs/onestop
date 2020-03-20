import React from 'react'
import FlexRow from '../common/ui/FlexRow'
import Left from './Left'
import Middle from './Middle'
import Right from './Right'

const styleContent = {
  display: 'flex',
  flex: '1 1 auto',
  position: 'relative',
  justifyContent: 'space-between',
  alignItems: 'stretch',
  width: '100%',
}
export default class Content extends React.Component {
  render() {
    const {
      style,
      padding,
      left,
      leftWidth,
      leftOpen,
      leftVisible,
      leftStyle,
      right,
      rightWidth,
      rightOpen,
      rightVisible,
      middle,
      middleMaxWidth,
    } = this.props
    const styles = Object.assign({}, styleContent, style)
    return (
      <FlexRow
        items={[
          <Left
            content={left}
            width={leftWidth}
            padding={padding}
            open={leftOpen}
            visible={leftVisible}
            style={leftStyle}
            key={'left'}
          />,
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
              open={rightOpen}
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
