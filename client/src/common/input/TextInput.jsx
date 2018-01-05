import React from 'react'
import PropTypes from 'prop-types'

const phi_2 = '0.308em'
const phi_4 = '0.154em'
const phi = '0.616em'
const white = '#FFF'
const black = '#000'

const styleContainerDefault = {
  display: 'flex',
  alignItems: 'stretch',
  padding: '0',
  boxSizing: 'border-box',
}

const styleContainerHoverDefault = {
  backgroundColor: 'red',
}
const styleContainerPressDefault = {
  backgroundColor: 'green',
}
const styleContainerFocusDefault = {
  backgroundColor: 'blue',
}

const styleLabelDefault = {
  // flexDirection: "column",
  // justifyContent: "center",
  padding: [phi, phi_2, phi_4, phi_4].join(' '),
  fontWeight: 700,
}

const styleLabelHoverDefault = {
  backgroundColor: 'orange',
}
const styleLabelPressDefault = {
  backgroundColor: 'gray',
}
const styleLabelFocusDefault = {
  backgroundColor: 'maroon',
}

const styleInputDefault = {
  flexGrow: 1,
  padding: phi_2,
  boxSizing: 'border-box',
  color: black,
}

const styleInputHoverDefault = {
  backgroundColor: 'cyan',
}
const styleInputPressDefault = {
  backgroundColor: 'magenta',
}
const styleInputFocusDefault = {
  backgroundColor: 'yellow',
}

const styleInputErrorDefault = {
  backgroundColor: 'red',
}

// component
class TextInput extends React.Component {
  componentWillMount() {
    const { value } = this.props

    // initial/default state
    let defaultValue = ''
    let initialValue = value ? value : defaultValue

    this.setState({
      value: initialValue,
      valid: true,
      hovering: false,
      pressing: false,
      pressingGlobal: false,
      focusing: false,
      touched: false,
    })
  }

  componentDidMount() {
    document.addEventListener('mouseup', this.handleGlobalMouseUp, false)
    document.addEventListener('mousedown', this.handleGlobalMouseDown, false)
  }

  componentWillUnmount() {
    document.removeEventListener('mouseup', this.handleGlobalMouseUp, false)
    document.removeEventListener('mousedown', this.handleGlobalMouseDown, false)
  }

  componentWillReceiveProps(nextProps) {
    if (nextProps.value !== this.props.value) {
      this.setState(prevProps => {
        return {
          ...prevProps,
          value: nextProps.value,
          // if we don't provide a validate function, then we should always be valid (don't show error styling)
          valid: nextProps.validate
            ? nextProps.validate(this.state.value, nextProps.id)
            : true,
        }
      })
    }
  }

  // keyboard events
  // boolean altKey
  // number charCode
  // boolean ctrlKey
  // boolean getModifierState(key)
  // string key
  // number keyCode
  // string locale
  // number location
  // boolean metaKey
  // boolean repeat
  // boolean shiftKey
  // number which
  handleKeyDown = event => {
    // console.log(':::handleKeyDown:::\n', event)
  }

  handleKeyPress = event => {
    // console.log(':::handleKeyPress:::\n', event)
  }

  handleKeyUp = event => {
    // console.log(':::handleKeyUp:::\n', event)
  }

  // focus events
  // DOMEventTarget relatedTarget
  handleFocus = event => {
    // console.log(':::handleFocus:::\n', event)
    this.setState(prevState => {
      return {
        ...prevState,
        focusing: true,
      }
    })
  }

  handleBlur = event => {
    // console.log(':::handleBlur:::\n', event)
    this.setState(prevState => {
      return {
        ...prevState,
        focusing: false,
        touched: true,
      }
    })
  }

  // form events
  handleChange = event => {
    // console.log(':::handleChange:::\n', event)
    const { onChange, validate, id } = this.props
    const newValue = event.currentTarget.value
    this.setState(
      {
        value: newValue,
        // if we don't provide a validate function, then we should always be valid (don't show error styling)
        valid: validate
          ? validate(newValue, id)
          : true,
      },
      () => {
        if (onChange) {
          onChange(event)
        }
      }
    )
  }

  handleSubmit = event => {
    // console.log(':::handleSubmit:::\n', event)
    // console.log('input was submitted: ' + this.state.value)
    event.preventDefault()
  }

  // mouse events
  // boolean altKey
  // number button
  // number buttons
  // number clientX
  // number clientY
  // boolean ctrlKey
  // boolean getModifierState(key)
  // boolean metaKey
  // number pageX
  // number pageY
  // DOMEventTarget relatedTarget
  // number screenX
  // number screenY
  // boolean shiftKey

  handleClick = event => {
    // console.log(':::handleClick:::\n', event)
  }

  handleMouseOver = event => {
    this.setState(prevState => {
      return {
        ...prevState,
        hovering: true,
        pressing: prevState.pressingGlobal,
      }
    })
  }

  handleMouseOut = event => {
    this.setState(prevState => {
      return {
        ...prevState,
        hovering: false,
        pressing: false,
      }
    })
  }

  handleGlobalMouseUp = event => {
    this.setState(prevState => {
      return {
        ...prevState,
        pressingGlobal: false,
      }
    })
  }

  handleGlobalMouseDown = event => {
    this.setState(prevState => {
      return {
        ...prevState,
        pressingGlobal: true,
      }
    })
  }

  handleMouseDown = event => {
    this.setState(prevState => {
      return {
        ...prevState,
        pressing: true,
      }
    })
  }

  handleMouseUp = event => {
    this.setState(prevState => {
      return {
        ...prevState,
        pressing: false,
      }
    })
  }

  // selection events
  handleSelect = event => {
    // console.log(':::handleSelect:::\n', event)
  }

  render() {
    const {
      id,
      name,
      ref,
      label,
      autoComplete,
      autoFocus,
      disabled,
      list,
      maxLength,
      pattern,
      placeholder,
      readOnly,
      required,
      size,
      validate,
    } = this.props

    const {
      styleContainer,
      styleContainerHover,
      styleContainerPress,
      styleContainerFocus,
      styleLabel,
      styleLabelHover,
      styleLabelPress,
      styleLabelFocus,
      styleInput,
      styleInputHover,
      styleInputPress,
      styleInputFocus,
      styleInputError,
    } = this.props

    const stylesContainerMerged = {
      ...styleContainerDefault,
      ...styleContainer,
      ...(this.state.hovering
        ? { ...styleContainerHoverDefault, ...styleContainerHover }
        : {}),
      ...(this.state.pressing
        ? { ...styleContainerPressDefault, ...styleContainerPress }
        : {}),
      ...(this.state.focusing
        ? { ...styleContainerFocusDefault, ...styleContainerFocus }
        : {}),
    }

    const stylesLabelMerged = {
      ...styleLabelDefault,
      ...styleLabel,
      ...(this.state.hovering
        ? { ...styleLabelHoverDefault, ...styleLabelHover }
        : {}),
      ...(this.state.pressing
        ? { ...styleLabelPressDefault, ...styleLabelPress }
        : {}),
      ...(this.state.focusing
        ? { ...styleLabelFocusDefault, ...styleLabelFocus }
        : {}),
    }

    const stylesInputMerged = {
      ...styleInputDefault,
      ...styleInput,
      ...(this.state.hovering
        ? { ...styleInputHoverDefault, ...styleInputHover }
        : {}),
      ...(this.state.pressing
        ? { ...styleInputPressDefault, ...styleInputPress }
        : {}),
      ...(this.state.focusing
        ? { ...styleInputFocusDefault, ...styleInputFocus }
        : {}),
      ...(!this.state.valid
        ? { ...styleInputErrorDefault, ...styleInputError }
        : {}),
    }

    let labelElement = null
    if (label) {
      labelElement = (
        <label htmlFor={id} style={stylesLabelMerged}>
          {label}
        </label>
      )
    }

    return (
      <div style={stylesContainerMerged}>
        {labelElement}
        <input
          type="text"
          id={id}
          ref={ref}
          style={stylesInputMerged}
          name={name} /* identifying string of input */
          value={this.state.value} /* the text value itself! */
          autoComplete={autoComplete} /* "on"|"off" */
          autoFocus={autoFocus} /* true|false */
          disabled={disabled} /* true|false */
          list={
            list
          } /* string correspponding to a <datalist id={list}><option value="a"><option value="b">...</datalist> */
          maxLength={maxLength} /* max number of characters typeable */
          pattern={
            pattern
          } /* specify a regex that the input value is checked against will use {title} as format description, or use "event.currentTarget.setCustomValidity('cusotm message here') when input.onInvalid event" */
          placeholder={
            placeholder
          } /* placeholder text to hint at expected value */
          readOnly={readOnly} /* true|false -> prevents user interaction */
          required={
            required
          } /* true|false -> defaults to "please fill out this field", but the message can be customized in the same setCustomValidity callback as {pattern} when it fails to match the value */
          size={
            size
          } /* default=20: number of characters to show (affects width visible) */
          /* keyboard events */
          onKeyDown={this.handleKeyDown}
          onKeyPress={this.handleKeyPress}
          onKeyUp={this.handleKeyUp}
          /* focus events */
          onFocus={this.handleFocus}
          onBlur={this.handleBlur}
          /* form events */
          onChange={this.handleChange}
          onSubmit={this.handleSubmit}
          /* mouse events */
          onClick={this.handleClick}
          onMouseOver={this.handleMouseOver}
          onMouseOut={this.handleMouseOut}
          onMouseDown={this.handleMouseDown}
          onMouseUp={this.handleMouseUp}
          /* selection events */
          onSelect={this.handleSelect} /* selected text */
        />
      </div>
    )
  }
}

TextInput.propTypes = {
  /* values */
  id: PropTypes.string.isRequired,
  ref: PropTypes.string,
  name: PropTypes.string.isRequired,
  label: PropTypes.string,
  value: PropTypes.string.isRequired,
  autoComplete: PropTypes.string,
  autoFocus: PropTypes.bool,
  disabled: PropTypes.bool,
  list: PropTypes.string,
  maxLength: PropTypes.number,
  pattern: PropTypes.string,
  placeholder: PropTypes.string,
  readOnly: PropTypes.bool,
  required: PropTypes.bool,
  size: PropTypes.number,
  validate: PropTypes.func,
}

export default TextInput
