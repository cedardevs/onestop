import React from 'react'
import _ from 'lodash'
import ReactDOM from 'react-dom'
import {Key} from '../../../utils/keyboardUtils'
import FlexRow from '../../common/ui/FlexRow'
import Video from '../../common/Video'
import Button from '../../common/input/Button'
import {times_circle, SvgIcon} from '../../common/SvgIcon'
const styleTrayContainer = (open, display, height, padding) => {
  return {
    fill: 'black',
    boxSizing: 'border-box',
    width: '100%',
    overflowY: 'hidden',
    transition: open // immediate transition
      ? 'padding 0.2s 0.0s, height 0.2s 0.0s'
      : 'height 0.2s 0.0s, padding 0.2s 0.0s',
    // properties set on a separate timer using state:
    height: height,
    display: display,
    padding: padding,
  }
}
const styleCloseButton = {
  alignSelf: 'center',
  background: 'none',
  border: 'none',
  outline: 'none',
  padding: '0.309',
  marginLeft: '.618em',
}
const styleHoverCloseButton = {
  background: 'none',
  fill: 'blue',
}
class VideoTray extends React.Component {
  constructor(props) {
    super(props)
    const {showVideo, url} = this.props
    const show = showVideo && url
    this.state = {
      ...{open: show},
      ...this.videoDisplay(show),
      ...this.videoHeight(show),
      ...this.videoOpacity(show),
      ...this.videoPadding(show),
    }
  }
  videoDisplay = showVideo => {
    return {display: showVideo ? 'block' : 'none'}
  }
  videoHeight = showVideo => {
    return {height: showVideo ? 'initial' : '0em'}
  }
  videoOpacity = showVideo => {
    return {opacity: showVideo ? '1' : '0'}
  }
  videoPadding = showVideo => {
    return {padding: showVideo ? '1.618em' : '0'}
  }
  handleTransitionEnd = event => {
    this.setState(prevState => {
      return {
        ...prevState,
        videoReady: true,
      }
    })
    ReactDOM.findDOMNode(this.videoRef).scrollIntoView({behavior: 'smooth'})
  }
  componentDidMount() {
    this.container.addEventListener('transitionend', this.handleTransitionEnd)
    this.animateTransition(this.props.showVideo)
  }
  animateOpen = () => {
    this.setState(prevState => {
      const isOpen = prevState.open
      const isDisplayed = prevState.display === 'block'
      const shouldOpen = !isOpen && !isDisplayed
      // these transitions do occasionally have timing issues, but I've only seen them when rapidly toggling a single element on and off..
      if (shouldOpen) {
        setTimeout(() => {
          this.setState(prevState => {
            return {
              ...prevState,
              ...this.videoHeight(true),
              ...this.videoOpacity(true),
              ...this.videoPadding(true),
            }
          })
        }, 15)
        const immediateTransition = {
          ...this.videoDisplay(true),
          ...this.videoOpacity(false),
          open: true,
        }
        return {...prevState, ...immediateTransition}
      }
      return prevState
    })
  }
  animateClose = () => {
    this.setState(prevState => {
      const isOpen = prevState.open
      const isDisplayed = prevState.display === 'block'
      const shouldClose = isOpen && isDisplayed
      // these transitions do occasionally have timing issues, but I've only seen them when rapidly toggling a single element on and off..
      if (shouldClose) {
        setTimeout(() => {
          this.setState(prevState => {
            if (prevState.closingTray) {
              this.props.trayCloseComplete()
            }
            return {
              ...prevState,
              ...{closingTray: false},
              ...this.videoDisplay(false),
              ...this.videoOpacity(false),
            }
          })
        }, 500)
        const immediateTransition = {
          ...this.videoHeight(false),
          ...this.videoOpacity(false),
          ...this.videoPadding(false),
          open: false,
        }
        return {...prevState, ...immediateTransition}
      }
      return prevState
    })
  }
  animateTransition = show => {
    if (show) {
      this.animateOpen()
    }
    else {
      this.animateClose()
    }
  }
  UNSAFE_componentWillReceiveProps(nextProps) {
    this.animateTransition(nextProps.showVideo)
  }
  componentWillUnmount() {
    this.container.removeEventListener(
      'transitionend',
      this.handleTransitionEnd
    )
  }
  closeTray = () => {
    this.setState(prevState => {
      return {
        ...prevState,
        ...{closingTray: true},
      }
    })
    this.props.closeTray()
  }
  handleKeyPressed = e => {
    // do nothing if modifiers are pressed
    if (e.metaKey || e.shiftKey || e.ctrlKey || e.altKey) {
      return
    }
    e.stopPropagation()
    if (e.keyCode === Key.ESCAPE) {
      this.closeTray()
    }
  }
  handleKeyDown = e => {
    // prevent the default behavior for tree control keys
    // these are the control keys used by the tree menu
    const controlKeys = [ Key.ESCAPE ]
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
    const {
      open,
      display,
      height,
      opacity,
      padding,
      videoReady,
      focusingCloseButton,
    } = this.state
    const {url, protocol} = this.props
    const closeButton = (
      <Button
        key="video-close-button"
        onClick={this.closeTray}
        title="close video"
        styleHover={styleHoverCloseButton}
        style={styleCloseButton}
      >
        <SvgIcon size="1em" path={times_circle} />
      </Button>
    )
    const video = videoReady ? (
      <Video
        key={`${url}-video-tray`}
        ref={videoRef => {
          this.videoRef = videoRef
        }}
        link={url}
        protocol={protocol}
        aspectRatio={0.5625}
        autofocus={videoReady}
        height={height}
      />
    ) : null
    return (
      <div
        style={styleTrayContainer(open, display, height, padding)}
        ref={container => {
          this.container = container
        }}
        onKeyUp={this.handleKeyPressed}
        onKeyDown={this.handleKeyDown}
        role="dialog"
      >
        <FlexRow items={[ video, closeButton ]} />
      </div>
    )
  }
}
export default VideoTray
