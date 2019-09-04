import React from 'react'
import {Key} from '../../../utils/keyboardUtils'

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
  border: '1px solid #333',
}

const styleDisabled = {
  background: '#b2b2b2',
  border: '1px solid #636363',
}

const styleInput = {
  visibility: 'hidden',
  width: '1px',
  margin: 0,
  padding: 0,
}

const styleLabelDefault = {
  marginLeft: '0.618em',
}

const styleCheckmark = {
  opacity: '0',
  position: 'absolute',
  width: '0.5em',
  height: '0.25em',
  background: 'transparent',
  top: '0.2em',
  left: '0.2em',
  border: '3px solid #333',
  borderTop: 'none',
  borderRight: 'none',
  transform: 'rotate(-45deg)',
}

const styleCheckboxHover = {
  // background: 'yellow',
  transform: 'rotate(-45deg) scale(1.618)',
  top: 0,
}

const styleCheckmarkChecked = {
  opacity: '1',
}

class Checkbox extends React.Component {
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

  toggle = () => {
    const {value, onChange} = this.props
    if (onChange) {
      onChange({checked: !this.state.checked, value: value})
    }
    this.setState(prevState => ({
      checked: !prevState.checked,
      hovering: prevState.hovering,
      pressing: false,
    }))
  }

  handleChange = event => {
    if (this.props.disabled) {
      return
    }
    // prevent parent click from propagating (only fire onClick of checkbox (not parent component onClicks too)
    event.stopPropagation()
    this.toggle()
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

  handleKeyUp = e => {
    if (e.keyCode === Key.SPACE) {
      e.preventDefault() // prevent scrolling down on space press
      e.stopPropagation()
      this.toggle()
    }
    if (e.keyCode === Key.ENTER) {
      e.stopPropagation()
      this.toggle()
    }
  }

  handleKeyDown = e => {
    // prevent the default behavior for control keys
    const controlKeys = [ Key.SPACE, Key.ENTER ]
    if (
      !e.metaKey &&
      !e.shiftKey &&
      !e.ctrlKey &&
      !e.altKey &&
      controlKeys.includes(e.keyCode)
    ) {
      e.preventDefault()
    }
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
      ...(this.state.hovering ? styleCheckboxHover : {}),
    }

    const styleLabel = {
      ...styleLabelDefault,
      ...this.props.styleLabel,
    }

    return (
      <div style={styleContainer}>
        <div
          role="checkbox"
          title={this.props.title}
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
          onKeyUp={this.handleKeyUp}
          onKeyDown={this.handleKeyDown}
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
        <label style={styleLabel} htmlFor={this.props.id}>
          {this.props.label}
        </label>
      </div>
    )
  }
}

export default Checkbox
