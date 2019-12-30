import React from 'react'

import {boxShadow, FilterColors} from '../../style/defaultStyles'

const defaultWidth = '128px'
const defaultBackgroundColor = FilterColors.MEDIUM
const defaultColor = '#111'

const styleOpen = width => {
  return {
    color: defaultColor,
    backgroundColor: defaultBackgroundColor,
    transition: 'flex 0.2s linear',
    flex: '0 0 ' + width,
    width: width,
    minWidth: '3.236em',
    position: 'relative',
    overflow: 'hidden',
    boxShadow: boxShadow,
  }
}

const styleClosed = width => {
  return {
    backgroundColor: defaultBackgroundColor,
    transition: 'flex 0.2s linear',
    flex: '0 0 ' + width,
    width: width,
    position: 'relative',
    overflow: 'initial',
    boxShadow: boxShadow,
  }
}

export default class Right extends React.Component {
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
    const {content, open, visible} = this.props
    const width = this.props.width ? this.props.width : defaultWidth

    if (!visible) {
      return null
    }

    return (
      <div style={open ? styleOpen(width) : styleClosed(width)}>{content}</div>
    )
  }
}
