import React from 'react'

const styleSearchField = {
  color: 'black',
  maxWidth: '32em',
  minWidth: '22em',
  display: 'inline-block',
  marginRight: '0.309em'
}

const styleTextField = {
  width: '100%',
  padding: '0.618em',
  border: '1px solid #ccc',
  boxShadow: 'inset 0 1px 3px #ddd',
  borderRadius: '2px',
  boxSizing: 'border-box',
}

const styleClearButton = {
  marginLeft: '-1.618em',
  height: '1em',
  color: 'grey',
  border: 'none',
  fontSize: '1.2em',
  textShadow: 'none',
  backgroundColor: 'white',
}

const styleClearButtonHover = {
  color: '#5a5a5a',
}

class TextSearchField extends React.Component {
  constructor(props) {
    super(props)
    this.state = { value: props.value }
  }

  componentWillMount() {
    this.setState({
      value: this.props.value,
      hoveringClear: false,
    })
  }

  componentWillReceiveProps(nextProps) {
    this.setState(prevState => {
      return {
        ...prevState,
        value: nextProps.value,
      }
    })
  }

  handleChange = event => {
    const { onChange } = this.props
    this.setState({ value: event.target.value })
    onChange(event.target.value)
  }

  handleKeyDown = event => {
    const { onEnterKeyDown } = this.props
    if (event.keyCode === 13) {
      event.preventDefault()
      onEnterKeyDown(event.target.value)
    }
  }

  handleMouseOverClear = event => {
    this.setState(prevState => {
      return {
        ...prevState,
        hoveringClear: true,
      }
    })
  }

  handleMouseOutClear = event => {
    this.setState(prevState => {
      return {
        ...prevState,
        hoveringClear: false,
      }
    })
  }

  render() {
    const { onClear } = this.props

    const styleClearButtonMerged = {
      ...styleClearButton,
      ...(this.state.hoveringClear ? styleClearButtonHover : {}),
    }

    return (
      <div>
        <div style={styleSearchField}>
          <input
            style={styleTextField}
            placeholder="Enter any term here to search NCEI data"
            onKeyDown={this.handleKeyDown}
            onChange={this.handleChange}
            value={this.state.value}
            aria-label="Search Text"
          />
        </div>
        <button
          style={styleClearButtonMerged}
          onClick={onClear}
          onMouseOver={this.handleMouseOverClear}
          onMouseOut={this.handleMouseOutClear}
          aria-label="Clear Search Text"
        >
          x
        </button>
      </div>
    )
  }
}

export default TextSearchField
