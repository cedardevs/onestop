import React, {Component} from 'react'
import ReactDOM from 'react-dom'
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
  height: '1.618em',
  overflow: 'hidden',
  whiteSpace: 'nowrap',
  textOverflow: 'ellipsis',
  fontWeight: 'normal',
  padding: '1.618em',
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
  height: '1.618em',
}

const styleMapContainer = {
  position: 'absolute',
  top: 0,
  zIndex: 0,
  width: '100%',
  maxWidth: '100%',
  height: '100%',
}

const styleTitle = {
  letterSpacing: '0.05em',
  fontSize: '1em',
  fontWeight: 600, // semi-bold
  textAlign: 'center',
  lineHeight: '1.618em', // use this value to count block height
  maxHeight: '4.854em', // maxHeight = lineHeight (1.618) * max lines (3)
  margin: 0,
  padding: 0,
  textOverflow: 'ellipsis',
  whiteSpace: 'normal',
  overflow: 'hidden',
}

export default class CollectionCard extends Component {
  constructor(props) {
    super(props)
    const {item} = this.props
    this.thumbnailUrl = processUrl(item.thumbnail)
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
    const {item} = this.props
    const geometry = item.spatialBounding
    if (!this.thumbnailUrl) {
      return (
        <div style={styleMapContainer}>
          <MapThumbnail geometry={geometry} interactive={false} />
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

  componentDidMount() {
    if (this.props.shouldFocus) {
      ReactDOM.findDOMNode(this.focusItem).focus()
    }
  }

  render() {
    const {item, onClick, shouldFocus} = this.props

    const title = item.title

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
      <div style={styleCard} onKeyPress={e => this.handleKeyPress(e, onClick)}>
        <div style={styleContentMerged}>
          <button
            style={styleOverlayMerged}
            onClick={onClick}
            onMouseOver={this.handleMouseOver}
            onMouseOut={this.handleMouseOut}
            onFocus={this.handleFocus}
            onBlur={this.handleBlur}
            ref={item => (this.focusItem = item)}
          >
            {this.renderThumbnailMap()}
            <div style={styleArchMerged}>
              <h2 style={styleTitle}>{title}</h2>
            </div>
          </button>
        </div>
      </div>
    )
  }
}

CollectionCard.propTypes = {
  item: PropTypes.object.isRequired,
  onClick: PropTypes.func.isRequired,
}
