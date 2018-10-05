import React from 'react'

export default class FocusManager extends React.Component {

  constructor(props) {
    super(props)
    this._immediateID = null
    this.state = {
      isManagingFocus: false,
    }
  }

  handleBlur = event => {
    const { onBlur } = this.props
    const { isManagingFocus } = this.state
    this._immediateID = setImmediate(() => {
      if (isManagingFocus) {
        this.setState({
          isManagingFocus: false,
        }, () => {
          if (onBlur) {
            onBlur(event)
          }
        })
      }
    })
  }

  handleFocus = event => {
    const { onFocus } = this.props
    const { isManagingFocus } = this.state
    clearImmediate(this._immediateID)
    if (!isManagingFocus) {
      this.setState({
        isManagingFocus: true,
      }, () => {
        if (onFocus) {
          onFocus(event)
        }
      })
    }
  }

  render() {
    return (
        <div
            role="focusManager"
            onBlur={this.handleBlur}
            onFocus={this.handleFocus}
        >
          {this.props.children}
        </div>
    )
  }
}