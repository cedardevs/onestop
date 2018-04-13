import React from 'react'
import {times_circle, SvgIcon} from '../common/SvgIcon'

const styleSearchField = {
  background: 'white',
  color: 'black',
  height: '100%',
  marginRight: '0.309em',
  boxShadow: 'inset 0 1px 1px rgba(0, 0, 0, 0.5)',
  borderRadius: '0.309em',
  display: 'flex',
  position: 'relative',
}

const styleSearchFieldFocused = {
  boxShadow: 'inset 0 2px 4px rgba(0, 0, 0, 0.5)',
}

const styleTextField = {
  margin: '0.618em',
  border: 'none',
  borderBottom: '2px solid transparent',
  background: 'none',
  boxSizing: 'border-box',
  minWidth: '17em',
  maxWidth: '30em',
  outline: 'none',
}

const styleTextFieldFocus = {
  borderBottom: '2px dotted #777',
}

const styleClearButton = {
  alignSelf: 'center',
  background: 'none',
  border: 'none',
  outline: 'none',
  padding: '0.618em',
}

const styleClearButtonHover = {
  color: '#5a5a5a',
}

const styleClearButtonFocus = {}

class TextSearchField extends React.Component {
  constructor(props) {
    super(props)
    this.state = {value: props.value}
  }

  componentWillMount() {
    this.setState({
      value: this.props.value,
      focusingText: false,
      hoveringClear: false,
      focusingClear: false,
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
    const {onChange} = this.props
    this.setState({value: event.target.value})
    onChange(event.target.value)
  }

  handleKeyDown = event => {
    const {onEnterKeyDown} = this.props
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

  handleTextFocus = e => {
    this.setState(prevState => {
      return {
        ...prevState,
        focusingText: true,
      }
    })
  }

  handleTextBlur = e => {
    this.setState(prevState => {
      return {
        ...prevState,
        focusingText: false,
      }
    })
  }
  handleClearFocus = e => {
    this.setState(prevState => {
      return {
        ...prevState,
        focusingClear: true,
      }
    })
  }

  handleClearBlur = e => {
    this.setState(prevState => {
      return {
        ...prevState,
        focusingClear: false,
      }
    })
  }

  render() {
    const {onClear, warning} = this.props

    const styleSearchFieldMerged = {
      ...styleSearchField,
      ...(this.state.focusingText ? styleSearchFieldFocused : {}),
    }

    const styleTextFieldMerged = {
      ...styleTextField,
      ...(this.state.focusingText ? styleTextFieldFocus : {}),
    }

    const styleClearButtonMerged = {
      ...styleClearButton,
      ...(this.state.hoveringClear ? styleClearButtonHover : {}),
      ...(this.state.focusingClear ? styleClearButtonFocus : {}),
    }

    const styleSvgIcon = {
      outline: this.state.focusingClear ? '2px dashed #777' : 'none',
    }
    const svgFillColor = this.state.focusingClear ? '#2c71a2' : '#777'

    return (
      <div style={styleSearchFieldMerged}>
        {warning}

        <input
          style={styleTextFieldMerged}
          placeholder="Enter any term here to search NCEI data"
          onKeyDown={this.handleKeyDown}
          onChange={this.handleChange}
          onFocus={this.handleTextFocus}
          onBlur={this.handleTextBlur}
          value={this.state.value}
          aria-label="Search Text"
          ref={input => {
            this.searchInput = input
          }}
        />
        <button
          style={styleClearButtonMerged}
          onClick={onClear}
          onMouseOver={this.handleMouseOverClear}
          onMouseOut={this.handleMouseOutClear}
          onFocus={this.handleClearFocus}
          onBlur={this.handleClearBlur}
          aria-label="Clear Search Text"
        >
          <SvgIcon
            size="2em"
            style={styleSvgIcon}
            path={times_circle(svgFillColor)}
          />
        </button>
      </div>
    )
  }
}

export default TextSearchField
