import React from 'react'
import PropTypes from 'prop-types'
import {Key} from '../utils/keyboardUtils'

export default class FocusManager extends React.Component {
  constructor(props) {
    super(props)
    this._immediateID = null
    this.state = {
      keying: false,
      isManagingFocus: false,
    }
  }

  focus = event => {
    const {onFocus} = this.props
    const {isManagingFocus} = this.state
    clearImmediate(this._immediateID)
    if (!isManagingFocus) {
      this.setState(
        {
          isManagingFocus: true,
        },
        () => {
          if (onFocus) {
            onFocus(event)
          }
        }
      )
    }
  }

  blur = event => {
    const {onBlur} = this.props
    const {isManagingFocus} = this.state
    if (isManagingFocus) {
      this.setState(
        {
          isManagingFocus: false,
        },
        () => {
          if (onBlur) {
            onBlur(event)
          }
        }
      )
    }
  }

  setKeying = isKeying => {
    this.setState(prevState => {
      return {
        ...prevState,
        keying: isKeying,
      }
    })
  }

  handleKeyUp = event => {
    this.setKeying(false)
  }

  handleKeyDown = event => {
    const {blurOnEscape, blurOnShiftTab, blurOnTab} = this.props

    if (event.shiftKey && event.keyCode === Key.TAB && blurOnShiftTab) {
      this.blur(event)
    }
    else if (event.keyCode === Key.TAB && blurOnTab) {
      this.blur(event)
    }

    if (event.keyCode === Key.ESCAPE && blurOnEscape) {
      this.blur(event)
    }
    this.setKeying(true)
  }

  handleBlur = event => {
    event.preventDefault()
    this._immediateID = setImmediate(() => {
      this.blur(event)
    })
  }

  handleFocus = event => {
    event.preventDefault()
    this.focus(event)
  }

  render() {
    const {style} = this.props
    return (
      <div
        role="focusManager"
        onBlur={this.handleBlur}
        onFocus={this.handleFocus}
        onKeyDown={this.handleKeyDown}
        onKeyUp={this.handleKeyUp}
        style={style}
      >
        {this.props.children}
      </div>
    )
  }
}

// Specifies the default values for props:
FocusManager.defaultProps = {
  blurOnEscape: false,
  blurOnShiftTab: false,
  blurOnTab: false,
}
