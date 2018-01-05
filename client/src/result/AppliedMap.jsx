import React, { Component } from 'react'

const styleDefault = {
  display: 'inline-flex',
  borderRadius: '0.1em 0.4em',
  padding: '.25em .1em .25em .5em',
  marginRight: '0.5em',
  marginBottom: '0.25em',
  backgroundColor: 'darkolivegreen',
  fontSize: '1.2em',
}

const styleHoverDefault = {
  filter: 'brightness(120%)',
}

const styleFocusDefault = {
  filter: 'brightness(120%)',
}

const styleClose = {
  color: 'lightgray',
  padding: '0 0.5em',
  cursor: 'pointer',
}

export default class AppliedMap extends Component {
  componentWillMount() {
    this.setState({
      hovering: false,
      focusing: false,
    })
  }

  handleMouseOver = event => {
    this.setState(prevState => {
      return {
        ...prevState,
        hovering: true,
      }
    })
  }

  handleMouseOut = event => {
    this.setState(prevState => {
      return {
        ...prevState,
        hovering: false,
      }
    })
  }

  handleFocus = event => {
    this.setState(prevState => {
      return {
        ...prevState,
        focusing: true,
      }
    })
  }

  handleBlur = event => {
    this.setState(prevState => {
      return {
        ...prevState,
        focusing: false,
      }
    })
  }

  render() {
    const {
      north,
      west,
      south,
      east,
      onUnselect,
      style,
      styleHover,
      styleFocus,
    } = this.props

    const stylesMerged = {
      ...styleDefault,
      ...style,
      ...(this.state.hovering ? {...styleHoverDefault, styleHover} : {}),
      ...(this.state.focusing ? {...styleFocusDefault, styleFocus} : {}),
    }

    return (
        <span
            style={stylesMerged}
            onMouseOver={this.handleMouseOver}
            onMouseOut={this.handleMouseOut}
            onFocus={this.handleFocus}
            onBlur={this.handleBlur}
        >
          North: {north}°, West:{west}°, South:{south}°, East:{east}°
          <span style={styleClose} onClick={onUnselect}>
					x
				</span>
			</span>
    )
  }
}
