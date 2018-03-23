import React, {Component} from 'react'
import PropTypes from 'prop-types'
import {processUrl} from '../../utils/urlUtils'
import MapThumbnail from '../../common/MapThumbnail'
import {boxShadow} from '../../common/defaultStyles'

const styleCard = {
  width: '25em',
  height: '15.5em',
  margin: '0 2em 2em 0',
  textAlign: 'center',
}

const styleContent = {
  boxSizing: 'border-box',
  width: '100%',
  height: '100%',
  color: 'white',
  overflow: 'hidden',
  position: 'relative',
  // border: '1px solid rgba(50, 50, 50, 0.75)',
  boxShadow: boxShadow,
}

const styleOverlay = {
  position: 'absolute',
  top: 0,
  left: 0,
  bottom: 0,
  right: 0,
  display: 'inline-flex',
  flexDirection: 'column',
  alignItems: 'flex-start',
  background: 'none',
  width: '100%',
  height: '100%',
  boxSizing: 'content-box',
  border: 0,
  color: 'inherit',
  font: 'inherit',
  lineHeight: 'normal',
  overflow: 'visible',
  borderRadius: 0,
  padding: 0,
  margin: 0,
}

const styleOverlayHover = {
  color: 'white',
}

const styleOverlayFocus = {
  color: 'white',
}

const styleOverlayBlur = {
  color: 'inherit',
}

const styleArch = {
  position: 'absolute',
  boxSizing: 'border-box',
  width: '100%',
  bottom: 0,
  left: 0,
  right: 0,
  height: '4.472em',
  overflow: 'hidden',
  whiteSpace: 'nowrap',
  textOverflow: 'ellipsis',
  fontWeight: 'normal',
  padding: '0.618em 1em 0.618em 1em',
  margin: 0,
  color: '#222',
  backgroundColor: '#FBFBFB',
  transition: 'background-color 0.3s ease, color 0.3s ease, height 0.3s ease',
  borderTop: '1px solid #AAA',
  borderRadius: '12.5em 12.5em 0em 0em / 2.236em',
  boxShadow: boxShadow,
}

const styleArchHover = {
  fontWeight: 'bold',
  backgroundColor: '#327CAC',
  color: '#FBFBFB',
  height: '7.708em',
}

const styleArchFocus = {
  fontWeight: 'bold',
  backgroundColor: '#327CAC',
  color: '#FBFBFB',
  height: '7.708em',
}

const styleArchBlur = {
  fontWeight: 'normal',
  backgroundColor: '#FBFBFB',
  color: '#222',
  height: '4.472em',
}

const styleMapContainer = {
  position: 'absolute',
  top: 0,
  zIndex: -1,
  width: '100%',
  maxWidth: '100%',
  height: '100%',
}

const styleSuperTitle = {
  marginTop: '0.309em',
  marginBottom: '0.309em',
  fontSize: '1em',
  lineHeight: '1.618em',
  fontWeight: 'normal',
}

const styleTitle = {
  fontSize: '1em',
  textAlign: 'left',
  lineHeight: '1.618em', // use this value to count block height
  maxHeight: '4.854em', // maxHeight = lineHeight (1.618) * max lines (3)
  marginTop: '0.309em',
  marginBottom: '0.309em',
  textOverflow: 'ellipsis',
  whiteSpace: 'normal',
  overflow: 'hidden',
}

export default class CollectionCard extends Component {
  constructor(props) {
    super(props)
    this.thumbnailUrl = processUrl(this.props.thumbnail)
  }

  componentWillMount() {
    this.setState(prevState => {
      return {
        hovering: false,
        focusing: false,
      }
    })
  }

  thumbnailStyle() {
    if (this.thumbnailUrl) {
      return {
        background: `url('${this.thumbnailUrl}')`,
        backgroundColor: 'black',
        backgroundRepeat: 'no-repeat',
        backgroundSize: 'cover',
        backgroundPosition: 'center center',
      }
    }
  }

  handleKeyPress(event, actionHandler) {
    if (event.key == 'Enter') {
      actionHandler()
    }
  }

  renderThumbnailMap() {
    if (!this.thumbnailUrl) {
      return (
        <div style={styleMapContainer}>
          <MapThumbnail geometry={this.props.geometry} interactive={false} />
        </div>
      )
    }
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
    const styleContentMerged = {
      ...styleContent,
      ...this.thumbnailStyle(),
    }

    const styleOverlayMerged = {
      ...styleOverlay,
      ...(this.state.focusing ? styleOverlayFocus : styleOverlayBlur),
      ...(this.state.hovering ? styleOverlayHover : {}),
    }

    const styleArchMerged = {
      ...styleArch,
      ...(this.state.focusing ? styleArchFocus : styleArchBlur),
      ...(this.state.hovering ? styleArchHover : {}),
    }

    return (
      <div
        style={styleCard}
        onKeyPress={e => this.handleKeyPress(e, this.props.onClick)}
      >
        <div style={styleContentMerged}>
          <button
            style={styleOverlayMerged}
            onClick={() => this.props.onClick()}
            onMouseOver={this.handleMouseOver}
            onMouseOut={this.handleMouseOut}
            onFocus={this.handleFocus}
            onBlur={this.handleBlur}
          >
            {this.renderThumbnailMap()}
            <div style={styleArchMerged}>
              <div style={styleSuperTitle} aria-hidden={true}>
                Collection Title:
              </div>
              <h2 style={styleTitle}>{this.props.title}</h2>
            </div>
          </button>
        </div>
      </div>
    )
  }
}

CollectionCard.propTypes = {
  onClick: PropTypes.func.isRequired,
  title: PropTypes.string.isRequired,
  thumbnail: PropTypes.string,
  geometry: PropTypes.object,
}
