import React from 'react'

class TextSearchField extends React.Component {

  constructor(props) {
    super(props)

    this.onChange = props.onChange
    this.onEnterKeyDown = props.onEnterKeyDown
    this.state = {value: props.value}

    this.handleChange = this.handleChange.bind(this)
    this.handleKeyDown = this.handleKeyDown.bind(this)
  }

  render() {
    return <input
        style={{width: '100%'}}
        placeholder="Enter any term here to search NCEI data"
        onKeyDown={this.handleKeyDown}
        onChange={this.handleChange}
        value={this.state.value}
    />
  }

  handleChange(e) {
    this.setState({value: e.target.value})
    this.onChange(e.target.value)
  }

  handleKeyDown(e) {
    if (e.keyCode === 13) {
      e.preventDefault()
      this.onEnterKeyDown(e.target.value)
    }
  }

  componentWillReceiveProps(nextProps) {
    this.setState({'value': nextProps.value})
  }
}

export default TextSearchField
