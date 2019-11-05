import React from 'react'
import {boxShadow} from '../../style/defaultStyles'

import {FilterColors} from '../../style/defaultStyles'

const defaultWidth = '128px'
const defaultBackgroundColor = FilterColors.MEDIUM
const defaultColor = '#111'

const styleOpen = (width, customStyle) => {
  return {
    color: defaultColor,
    backgroundColor: defaultBackgroundColor,
    transition: 'flex 0.2s linear',
    flex: '0 0 ' + width,
    width: width,
    minWidth: '3.236em',
    position: 'relative',
    boxShadow: boxShadow,
    ...customStyle,
  }
}

const styleClosed = (width, customStyle) => {
  return {
    backgroundColor: defaultBackgroundColor,
    transition: 'flex 0.2s linear',
    flex: '0 0 ' + width,
    width: width,
    position: 'relative',
    overflow: 'initial',
    boxShadow: boxShadow,
    ...customStyle,
  }
}

export default class Left extends React.Component {
  UNSAFE_componentWillMount() {
    this.setState({
      open: this.props.open,
    })
  }

  UNSAFE_componentWillReceiveProps(nextProps) {
    if (nextProps.open !== this.state.open) {
      this.setState(prevState => {
        return {
          ...prevState,
          open: nextProps.open,
        }
      })
    }
  }

  render() {
    const {content, open, visible, style} = this.props
    const width = this.props.width ? this.props.width : defaultWidth

    if (!visible) {
      return null
    }

    return (
      <div style={open ? styleOpen(width, style) : styleClosed(width, style)}>
        {content}
      </div>
    )
  }
}
