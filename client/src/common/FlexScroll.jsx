import React, {Component} from 'react'

const styleContainer = {
  display: 'flex',
}

const styleLeftContainer = {
  flex: '1 1 auto'
}

const styleRightContainer = {
  flex: '0 1 auto',
  display: 'flex',
  flexDirection: 'column',
}

const styleFixed = {
  flex: '0 0 auto',
}

const styleContentWrapper = {
  position: 'relative',
  flex: '1 1 auto'
}
const styleContentScroll = {
  position: 'absolute',
  top: 0,
  left: 0,
  right: 0,
  bottom: 0,
  overflowY: 'auto',
}

export default class FlexScroll extends Component {
  render() {
    // style: overrides or enhances styles to the container element for more customization
    // controlContent: left flex item whose height determines scroll height of right flex item
    // fixedContent: first content in right flex item which should not be scrolled
    // scrollContent: content under fixedContent which should scroll
    const { style, styleLeft, styleRight, left, rightTop, rightScroll, rightBottom } = this.props
    const styles = Object.assign({}, styleContainer, style)
    const stylesLeft = Object.assign({}, styleLeftContainer, styleLeft)
    const stylesRight = Object.assign({}, styleRightContainer, styleRight)
    return (
      <div style={styles}>
        <div style={stylesLeft}>
          {left}
        </div>
        <div style={stylesRight}>
          <div style={styleFixed}>
            {rightTop}
          </div>
          <div style={styleContentWrapper}>
            <div style={styleContentScroll}>
              {rightScroll}
            </div>
          </div>
          <div style={styleFixed}>
            {rightBottom}
          </div>
        </div>
      </div>
    )
  }
}
