import React, {Component} from 'react'

import arrowRight from '../../../img/font-awesome/white/svg/arrow-right.svg'
import arrowLeft from '../../../img/font-awesome/white/svg/arrow-left.svg'

const defaultOpenText = 'OPEN'
const defaultWidth = 128
const defaultBackgroundColor = '#3E7BAD'
const defaultColor = '#111'

const styleOpen = width => {
  return {
    color: defaultColor,
    backgroundColor: defaultBackgroundColor,
    transition:
      'padding-left 0.1s linear 0s, padding-right 0.1s linear 0.1s, flex 0.5s ease-out 0.2s',
    flex: '0 0 ' + width + 'px',
    width: width + 'px',
    minWidth: '3.236em',
    position: 'relative',
    overflow: 'hidden',
  }
}

const styleClosed = width => {
  return {
    backgroundColor: defaultBackgroundColor,
    transition:
      'flex 0.5s ease-in 0s, padding-right 0.1s linear 0.5s, padding-left 0.1s linear 0.6s',
    flex: '0 1 0',
    width: width + 'px',
    minWidth: '2em',
    position: 'relative',
    overflow: 'initial',
  }
}

const styleCloseContentArrow = {
  position: 'absolute',
  top: 0,
  left: 0,
  backgroundColor: '#4B7AA8',
  paddingLeft: '0.618em',
  paddingRight: '0.618em',
  cursor: 'pointer',
}

const styleCloseContentArrowImage = {
  width: '2em',
  height: '42px',
}

const styleClosedContent = {
  display: 'flex',
  flexDirection: 'column',
  justifyContent: 'flex-start',
  alignItems: 'center',
  alignSelf: 'center',
  height: '100%',
  padding: '0.618em',
  cursor: 'pointer',
}

const styleClosedContentImage = {
  height: '1em',
}

const styleHiddentContentVerticalText = {
  fontSize: '1.309em',
  transform: 'rotate(-90deg)',
  display: 'block',
}

const verticalText = text => {
  return [ ...text ].reverse().map((c, index) => {
    return (
      <span key={index} style={styleHiddentContentVerticalText}>
        {c}
      </span>
    )
  })
}

export default class Right extends Component {
  componentWillMount() {
    this.setState({
      open: this.props.open,
    })
  }

  componentWillReceiveProps(nextProps) {
    if (nextProps.open !== this.state.open) {
      this.setState({
        open: nextProps.open,
      })
    }
  }

  handleOpen = event => {
    const {open} = this.state
    const {toggle} = this.props
    event.stopPropagation()
    if (!open) {
      toggle()
    }
  }

  handleClose = event => {
    const {open} = this.state
    const {toggle} = this.props
    event.stopPropagation()
    if (open) {
      toggle()
    }
  }

  render() {
    const width = this.props.width ? this.props.width : defaultWidth
    const visible = this.props.visible ? this.props.visible : false

    if (!visible) {
      return null
    }

    const classOpen = styleOpen(width)
    const classClosed = styleClosed(width)
    const classes = this.state.open ? classOpen : classClosed
    const hideContentArrow = (
      <div style={styleCloseContentArrow} onClick={this.handleClose}>
        <img
          style={styleCloseContentArrowImage}
          alt="close right panel"
          src={arrowRight}
        />
      </div>
    )
    const closedContent = (
      <div style={styleClosedContent}>
        <img
          style={styleClosedContentImage}
          alt="expand right panel"
          src={arrowLeft}
        />
        {verticalText(
          this.props.openText ? this.props.openText : defaultOpenText
        )}
        <img
          style={styleClosedContentImage}
          alt="expand right panel"
          src={arrowLeft}
        />
      </div>
    )
    return (
      <div style={classes} onClick={this.handleOpen}>
        {this.state.open ? hideContentArrow : null}
        {this.state.open ? this.props.content : closedContent}
      </div>
    )
  }
}
