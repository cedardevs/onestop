import React, {Component} from 'react'
import {boxShadow} from '../common/defaultStyles'

const defaultWidth = '128px'
const defaultBackgroundColor = '#3D97D2'
const defaultColor = '#111'

const styleVisible = width => {
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

const styleHidden = width => {
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

export default class Left extends Component {
  componentWillMount() {
    this.setState({
      visible: this.props.visible,
    })
  }

  componentWillReceiveProps(nextProps) {
    if (nextProps.visible !== this.state.visible) {
      this.setState(prevState => {
        return {
          ...prevState,
          visible: nextProps.visible,
        }
      })
    }
  }

  render() {
    const {content, visible} = this.props
    const width = this.props.width ? this.props.width : defaultWidth
    return (
      <div style={visible ? styleVisible(width) : styleHidden(width)}>
        {content}
      </div>
    )
  }
}
