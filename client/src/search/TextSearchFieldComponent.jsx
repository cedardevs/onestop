import React from 'react'
import styles from './search.css'

class TextSearchField extends React.Component {

  constructor(props) {
    super(props)

    this.state = {value: props.value}

    this.handleChange = this.handleChange.bind(this)
    this.handleKeyDown = this.handleKeyDown.bind(this)
  }

  render() {
    return <input
        className={styles.textField}
        placeholder="Enter Search Term"
        onKeyDown={this.handleKeyDown}
        onChange={this.handleChange}
        value={this.state.value}
    />
  }

  handleChange(e) {
    this.setState({value: e.target.value})
  }

  handleKeyDown(e) {
    if (e.keyCode === 13) {
      e.preventDefault()
      this.props.onEnterKeyDown(e.target.value)
    }
  }

  componentWillReceiveProps(nextProps) {
    this.setState({'value': nextProps.value})
  }
}

export default TextSearchField
 