import React, {Component} from 'react'
import PropTypes from 'prop-types'
import stopCircle from 'fa/stop-circle-o.svg'
import defaultStyles from '../common/defaultStyles'
import {fontFamilySerif} from '../utils/styleUtils'
const noaaLogo = require('../../img/noaa_logo_circle_72x72.svg')

//-- Styles
const styleLogoWrapper = {
  padding: '0 1.618em 0 0',
  display: 'flex',
  flex: '0 0 max-content',
  alignItems: 'center',
  flexWrap: 'nowrap',
  fontFamily: fontFamilySerif(),
  letterSpacing: '0.105em',
}

const styleNoaaLogoWrapper = {
  padding: 0,
}

const styleNoaaLogo = {
  height: '4.5em',
  width: '4.5em',
  minHeight: '4.5em',
  minWidth: '4.5em',
  boxSizing: 'border-box',
}

const stylesNoaaLogoFocused = {
  border: '2px dashed white',
  borderRadius: '50%',
}

const styleTextWrapper = {
  display: 'inline-flex',
  whiteSpace: 'nowrap',
  flex: '0 0 max-content',
  padding: '0 0.309em',
}

const styleTextWrapperFocused = {
  outline: '2px dashed white',
}

const stylesOneStopLink = {
  color: 'white',
  textDecoration: 'none',
  display: 'inline',
  fontSize: '2em',
  outline: 'none',
}

const stylesStopCircle = {
  position: 'relative',
  top: '.15em',
  left: '.07em',
  maxWidth: '1.1em',
  maxHeight: '1.1em',
}

const stylesOneStopText = {
  fontSize: '1em',
  display: 'block',
}

const stylesNceiText = {
  fontSize: '0.4em',
  display: 'block',
  textTransform: 'uppercase',
  padding: '0 0 0 0.5em',
}

const stylesNceiTextBottom = {
  fontSize: '0.4em',
  display: 'block',
  textTransform: 'uppercase',
  padding: '0 0 0.309em 0.5em',
}

//-- Component

export default class Logo extends Component {
  componentWillMount() {
    this.setState({
      focusingImage: false,
      focusingText: false,
    })
  }

  handleImageFocus = event => {
    this.setState(prevState => {
      return {
        ...prevState,
        focusingImage: true,
      }
    })
  }

  handleImageBlur = event => {
    this.setState(prevState => {
      return {
        ...prevState,
        focusingImage: false,
      }
    })
  }

  handleTextFocus = event => {
    this.setState(prevState => {
      return {
        ...prevState,
        focusingText: true,
      }
    })
  }

  handleTextBlur = event => {
    this.setState(prevState => {
      return {
        ...prevState,
        focusingText: false,
      }
    })
  }

  render() {
    const styleNoaaLogoMerged = {
      ...styleNoaaLogo,
      ...(this.state.focusingImage ? stylesNoaaLogoFocused : {}),
    }

    const styleTextWrapperMerged = {
      ...styleTextWrapper,
      ...(this.state.focusingText ? styleTextWrapperFocused : {}),
    }

    return (
      <div style={styleLogoWrapper}>
        <div style={styleNoaaLogoWrapper}>
          <a
            href="#"
            title="One Stop Home"
            aria-hidden={true}
            onClick={() => this.props.onClick()}
            onFocus={this.handleImageFocus}
            onBlur={this.handleImageBlur}
            style={{outline: 'none'}}
          >
            <img
              style={styleNoaaLogoMerged}
              id="logo"
              alt="NOAA Logo"
              src={noaaLogo}
            />
          </a>
        </div>
        <div style={styleTextWrapperMerged}>
          <a
            href="#"
            title="One Stop Home"
            style={stylesOneStopLink}
            onClick={() => this.props.onClick()}
            onFocus={this.handleTextFocus}
            onBlur={this.handleTextBlur}
          >
            <span style={defaultStyles.hideOffscreen}>OneStop</span>
            <span aria-hidden="true" style={stylesOneStopText}>
              <img
                src={stopCircle}
                style={stylesStopCircle}
                alt={'OneStop'}
              />neStop
            </span>
            <span style={stylesNceiText}>National Oceanic and</span>
            <span style={stylesNceiTextBottom}>Atmospheric Administration</span>
          </a>
        </div>
      </div>
    )
  }
}

Logo.propTypes = {
  onClick: PropTypes.func,
}
