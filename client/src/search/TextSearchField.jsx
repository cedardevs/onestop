import React from 'react'

// import iconClear from '../../img/font-awesome/white/svg/times-circle.svg'
import {times_circle_o, times_circle, SvgIcon} from '../common/SvgIcon'

const styleSearchField = {
  color: 'black',
  maxWidth: '32em',
  minWidth: '22em',
  display: 'inline-flex',
  marginRight: '0.309em',
}

const styleTextField = {
  width: '100%',
  padding: '0.618em',
  border: '1px solid #ccc',
  boxShadow: 'inset 0 1px 1px rgba(0, 0, 0, 0.5)',
  borderRadius: '2px 0 0 2px',
  boxSizing: 'border-box',
}

const styleTextFieldFocus = {
  boxShadow: 'inset 0 1px 2px rgba(0, 0, 0, 0.5)',
}

const styleClearButton = {
  width: '2em',
  fill: 'white',
  backgroundColor: '#327cac',
  border: 'none',
  borderRadius: '0 2px 2px 0',
  padding: '0.105em 0.309em',
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'center',
}

const styleClearButtonHover = {
  color: '#5a5a5a',
}

const styleClearButtonFocus = {
  backgroundColor: '#3d97d2',
}

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

  handleClearClick = e => {}

  render() {
    const {onClear} = this.props

    const styleTextFieldMerged = {
      ...styleTextField,
      ...(this.state.focusingText ? styleTextFieldFocus : {}),
    }

    const styleClearButtonMerged = {
      ...styleClearButton,
      ...(this.state.hoveringClear ? styleClearButtonHover : {}),
      ...(this.state.focusingClear ? styleClearButtonFocus : {}),
    }

    return (
      <div>
        <div style={styleSearchField}>
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
            <SvgIcon size="1.2em" path={times_circle} />
          </button>
        </div>
      </div>
    )

    // <img
    //   src={iconClear}
    //   style={styleIconClear}
    //   aria-hidden={true}
    //   alt="clear search icon"
    // />
  }
}

export default TextSearchField
