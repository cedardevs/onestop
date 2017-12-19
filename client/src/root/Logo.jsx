import React, {Component} from 'react'
import PropTypes from 'prop-types'
import stopCircle from 'fa/stop-circle-o.svg'
import defaultStyles from '../common/defaultStyles'

const noaaLogo = require('../../img/noaa_logo_circle_72x72.svg')

//-- Styles

const stylesLogoWrapper = {
  padding: '0 1.618em 0 0',
  display: 'flex',
  flex: '0 0 max-content',
  alignItems: 'center',
  flexWrap: 'nowrap',
}

const stylesNoaaLogoWrapper = {
  padding: '0 0.618em 0 0',
}

const stylesNoaaLogo = {
  height: '4.5em',
  width: '4.5em',
  minHeight: '4.5em',
  minWidth: '4.5em',
}

const stylesTextWrapper = {
  display: 'inline-flex',
  whiteSpace: 'nowrap',
  flex: '0 0 max-content',
}

const stylesOneStopLink = {
  color: 'white',
  textDecoration: 'none',
  display: 'inline',
  fontSize: '2em',
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

//-- Component

export default class Logo extends Component {
  constructor(props) {
    super(props)
  }

  handleClick = () => {
    if (typeof this.props.onClick === 'function') {
      this.props.onClick()
    }
  }

  render() {
    return (
      <div style={stylesLogoWrapper}>
        <div style={stylesNoaaLogoWrapper}>
          <a
            href="#"
            title="One Stop Home"
            aria-hidden={true}
            onClick={() => this.props.onClick()}
          >
            <img
              style={stylesNoaaLogo}
              id="logo"
              alt="NOAA Logo"
              src={noaaLogo}
            />
          </a>
        </div>
        <div style={stylesTextWrapper}>
          <a
            href="#"
            title="One Stop Home"
            style={stylesOneStopLink}
            onClick={() => this.props.onClick()}
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
            <span style={stylesNceiText}>Atmospheric Administration</span>
          </a>
        </div>
      </div>
    )
  }
}

Logo.propTypes = {
  onClick: PropTypes.func,
}
