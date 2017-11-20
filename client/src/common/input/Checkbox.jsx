import React, { Component } from 'react'

const styleCheckbox = {
  width: '1em',
  height: '1em',
  position: 'relative',
}

const styleInput = {
  visibility: 'hidden',
}

const styleLabel = {
  cursor: 'pointer',
  position: 'absolute',
  width: '100%',
  height: '100%',
  top: 0,
  left: 0,
  background: '#eee',
  border: '1px solid #ddd',
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
    this.state = { checked: !!props.checked, hovering: false, pressing: false }

    this.handleChange = this.handleChange.bind(this)
    this.handleMouseOver = this.handleMouseOver.bind(this)
    this.handleMouseOut = this.handleMouseOut.bind(this)
    this.handleMouseDown = this.handleMouseDown.bind(this)
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

  handleChange(event) {
    const { value, onChange } = this.props
    if (onChange) {
      onChange({ checked: !this.state.checked, value: value })
    }
    // prevent parent click from propagating (only fire onClick of checkbox (not parent component onClicks too)
    event.stopPropagation()
    this.setState(prevState => ({
      checked: !prevState.checked,
      hovering: prevState.hovering,
      pressing: false,
    }))
  }

  handleMouseOver(event) {
    this.setState(prevState => ({
      checked: prevState.checked,
      hovering: true,
      pressing: prevState.pressing,
    }))
  }

  handleMouseOut(event) {
    this.setState(prevState => ({
      checked: prevState.checked,
      hovering: false,
      pressing: false,
    }))
  }

  handleMouseDown(event) {
    this.setState(prevState => ({
      checked: prevState.checked,
      hovering: prevState.hovering,
      pressing: true,
    }))
  }

  render() {
    let styleInteract = styleCheckmark
    if (this.state.checked || (this.state.hovering && this.state.pressing)) {
      styleInteract = { ...styleCheckmark, ...styleCheckmarkChecked }
    } else if (this.state.hovering) {
      styleInteract = { ...styleCheckmark, ...styleCheckmarkHover }
    }

    return (
      <div
        style={styleCheckbox}
        onClick={this.handleChange}
        onMouseOver={this.handleMouseOver}
        onMouseOut={this.handleMouseOut}
        onMouseDown={this.handleMouseDown}
      >
        <input
          type="checkbox"
          name={this.props.name}
          value={this.props.value}
          checked={this.state.checked}
          onChange={() => {}}
          style={styleInput}
        />
        <label style={styleLabel} />
        <div style={styleInteract} />
      </div>
    )
  }
}

export default Checkbox
