import React, { Component } from 'react'
import Button from '../common/input/Button'

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
      })
    }
  }

  render() {
    const width = this.props.width ? this.props.width : defaultWidth
    const classVisible = styleVisible(width)
    const classHidden = styleHidden(width)
    const classes = this.state.visible ? classVisible : classHidden
    const hideContentArrow = (
        <div style={styleHideContentArrowWrapper}>
            <Button
                icon={arrowLeft}
                styleIcon={{width:"1em", height:"31px"}}
                onClick={this.handleClose}
                ariaLabel={'Hide Filter Menu'}
            />
        </div>
    )
    const hiddenContent = (
        <div style={styleHiddenContent}>
          <img style={styleHiddentContentImage} alt='Show Filter Menu' src={arrowRight}/>
          <span style={styleHiddentContentVerticalText}>S</span>
          <span style={styleHiddentContentVerticalText}>R</span>
          <span style={styleHiddentContentVerticalText}>E</span>
          <span style={styleHiddentContentVerticalText}>T</span>
          <span style={styleHiddentContentVerticalText}>L</span>
          <span style={styleHiddentContentVerticalText}>I</span>
          <span style={styleHiddentContentVerticalText}>F</span>
          <img style={styleHiddentContentImage} alt='Show Filter Menu' src={arrowRight}/>
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
