import React, {Component} from 'react'

const styleContainer = {
  display: 'flex',
  alignItems: 'center',
}

const styleCheckboxContainer = {
  width: '1em',
  height: '1em',
  position: 'relative',
  minWidth: '1em',
  cursor: 'pointer',
  background: '#eee',
  border: '1px solid #ddd',
}

const styleDisabled = {
  background: '#cbcbcb',
  border: '1px solid #cbcbcb',
}

const styleInput = {
  visibility: 'hidden',
}

const styleCheckmark = {
  opacity: '0.1',
  position: 'absolute',
  width: '0.5em',
  height: '0.25em',
  background: 'transparent',
  top: '0.25em',
  left: '0.25em',
  border: '3px solid #333',
  borderTop: 'none',
  borderRight: 'none',
  transform: 'rotate(-45deg)',
}

const styleCheckmarkHover = {
  opacity: '0.2',
}

const styleCheckmarkChecked = {
  opacity: '1',
}

class Checkbox extends Component {
  constructor(props) {
    super(props)
    this.state = {checked: !!props.checked, hovering: false, pressing: false}
  }

  componentWillReceiveProps(nextProps) {
    // keep checkbox checked state in sync with props passed in
    if (nextProps.checked !== this.props.checked) {
      this.setState(prevState => ({
        ...prevState,
        checked: nextProps.checked,
      }))
    }
  }

  handleChange = event => {
    if (this.props.disabled) {
      return
    }
    const {value, onChange} = this.props
    if (onChange) {
      onChange({checked: !this.state.checked, value: value})
    }
    // prevent parent click from propagating (only fire onClick of checkbox (not parent component onClicks too)
    event.stopPropagation()
    this.setState(prevState => ({
      checked: !prevState.checked,
      hovering: prevState.hovering,
      pressing: false,
    }))
  }

  handleMouseOver = event => {
    if (this.props.disabled) {
      return
    }
    this.setState(prevState => ({
      checked: prevState.checked,
      hovering: true,
      pressing: prevState.pressing,
    }))
  }

  handleMouseOut = event => {
    if (this.props.disabled) {
      return
    }
    this.setState(prevState => ({
      checked: prevState.checked,
      hovering: false,
      pressing: false,
    }))
  }

  handleMouseDown = event => {
    if (this.props.disabled) {
      return
    }
    this.setState(prevState => ({
      checked: prevState.checked,
      hovering: prevState.hovering,
      pressing: true,
    }))
  }

  handleFocus = e => {
    this.setState(prevState => {
      return {
        ...prevState,
        focused: true,
      }
    })
  }

  handleBlur = e => {
    this.setState(prevState => {
      return {
        ...prevState,
        focused: false,
      }
    })
  }

  render() {
    const styleCheckbox = {
      ...styleCheckboxContainer,
      ...(this.state.focused && this.props.styleFocus
        ? this.props.styleFocus
        : {}),
      ...(this.props.disabled ? styleDisabled : {}),
    }

    const styleCheck = {
      ...styleCheckmark,
      ...(this.state.checked || (this.state.hovering && this.state.pressing)
        ? styleCheckmarkChecked
        : {}),
      ...(this.state.hovering ? styleCheckmarkHover : {}),
    }

    // TODO merge checkmark style changes with disabled stuff

    return (
      <div style={styleContainer}>
        <div
          role="checkbox"
          aria-checked={this.state.checked}
          aria-label={this.props.label}
          aria-disabled={this.props.disabled}
          tabIndex={this.props.tabIndex || 0}
          style={styleCheckbox}
          onClick={this.handleChange}
          onMouseOver={this.handleMouseOver}
          onMouseOut={this.handleMouseOut}
          onMouseDown={this.handleMouseDown}
          onBlur={this.handleBlur}
          onFocus={this.handleFocus}
        >
          <div style={styleCheck} />
        </div>
        <input
          id={this.props.id}
          type="checkbox"
          name={this.props.name}
          value={this.props.value}
          disabled={this.props.disabled}
          checked={this.state.checked}
          onChange={() => {}}
          style={styleInput}
        />
        <label htmlFor={this.props.id}>{this.props.label}</label>
      </div>
    )
  }
}

export default Checkbox
