import React, { Component } from 'react'

import arrowRight from "../../img/font-awesome/white/svg/arrow-right.svg"
import arrowLeft from "../../img/font-awesome/white/svg/arrow-left.svg"

const defaultWidth = 128
const defaultBackgroundColor = '#3E7BAD'
const defaultColor = '#111'

const styleVisible = (width) => {
  return {
    color: defaultColor,
    backgroundColor: defaultBackgroundColor,
    transition: 'padding-left 0.1s linear 0s, padding-right 0.1s linear 0.1s, flex 0.5s ease-out 0.2s',
    flex: '0 0 ' + width + 'px',
    width: width + 'px',
    minWidth: "3.236em",
    position: 'relative',
    overflow: 'hidden'
  }
}

// const styleVisible

const styleHidden = (width) => {
  return {
    backgroundColor: defaultBackgroundColor,
    transition: 'flex 0.5s ease-in 0s, padding-right 0.1s linear 0.5s, padding-left 0.1s linear 0.6s',
    flex: '0 1 0',
    width: width + 'px',
    minWidth: "2em",
    position: 'relative',
    overflow: 'initial'
  }
}

const styleHideContentArrowWrapper = {
  backgroundColor: "#242C36",
  position: "absolute",
  top: "1px",
  right: 0,
  padding: "4px"
}

const styleHideContentArrow = {
  // position: "absolute",
  top: "6px",
  right: "6px",
  backgroundColor: "#5396CC",
  padding: "0.105em 0.618em",
  cursor: "pointer",
  borderRadius: "0.105em"
}

const styleHideContentArrowHover = {
  backgroundColor: "#277CB2"
}

const styleHideContentArrowImage = {
  width: "2em",
  height: "31px",
}

const styleHiddenContent = {
  display: "flex",
  flexDirection: "column",
  justifyContent: "flex-start",
  alignItems: "center",
  alignSelf: "center",
  height: "100%",
  padding: "0.618em",
  cursor: "pointer",
}

const styleHiddentContentImage = {
  height: "1em",
}

const styleHiddentContentVerticalText = {
  fontSize: "1.309em",
  transform: "rotate(-90deg)",
  display: "block"
}

export default class Left extends Component {

  componentWillMount() {
    this.setState({
      visible: this.props.visible,
      hovering: false
    })
  }

  componentWillReceiveProps(nextProps) {
    if (nextProps.visible !== this.state.visible) {
      this.setState(prevState => {
        return {
          ...prevState,
          visible: nextProps.visible
        }
      })
    }
  }

  handleOpen = (event) => {
    event.stopPropagation()
    if (!this.state.visible) {
      this.setState(prevState => {
          return {
              ...prevState,
              visible: true
          }
      })
    }
  }

  handleClose = (event) => {
    event.stopPropagation()
    if (this.state.visible) {
      this.setState({
        visible: false,
        hovering: false
      })
    }
  }

  handleMouseOver = (event) => {
      this.setState(prevState => {
        return {
            ...prevState,
            hovering: true
        }
      })
  }

  handleMouseOut = (event) => {
      this.setState(prevState => {
          return {
              ...prevState,
              hovering: false
          }
      })
  }

  render() {
    const width = this.props.width ? this.props.width : defaultWidth
    const classVisible = styleVisible(width)
    const classHidden = styleHidden(width)
    const classes = this.state.visible ? classVisible : classHidden
    const stylesArrow = {
        ...styleHideContentArrow,
        ...(this.state.hovering ? styleHideContentArrowHover : {})
    }
    const hideContentArrow = (
        <div style={styleHideContentArrowWrapper}>
          <div style={stylesArrow} onClick={this.handleClose} onMouseOver={this.handleMouseOver} onMouseOut={this.handleMouseOut}>
            <img style={styleHideContentArrowImage} src={arrowLeft}/>
          </div>
        </div>
    )
    const hiddenContent = (
        <div style={styleHiddenContent}>
          <img style={styleHiddentContentImage} src={arrowRight}/>
          <span style={styleHiddentContentVerticalText}>S</span>
          <span style={styleHiddentContentVerticalText}>R</span>
          <span style={styleHiddentContentVerticalText}>E</span>
          <span style={styleHiddentContentVerticalText}>T</span>
          <span style={styleHiddentContentVerticalText}>L</span>
          <span style={styleHiddentContentVerticalText}>I</span>
          <span style={styleHiddentContentVerticalText}>F</span>
          <img style={styleHiddentContentImage} src={arrowRight}/>
        </div>
    )
    return (
        <div style={classes} onClick={this.handleOpen}>
          {this.state.visible ? hideContentArrow : null}
          {this.state.visible ? this.props.content : hiddenContent}
        </div>
    )
  }
}
