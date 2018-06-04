import React from 'react'
import _ from 'lodash'
import ReactDOM from 'react-dom'

import {Key} from '../../utils/keyboardUtils'
import Video from '../../common/Video'

const styleTrayContainer = (open, display, height, padding) => {
  return {
    boxSizing: 'border-box',
    width: '100%',
    transition: open // immediate transition
      ? 'padding 0.2s 0.0s, height 0.2s 0.0s'
      : 'height 0.2s 0.0s, padding 0.2s 0.0s',
    // properties set on a separate timer using state:
    height: height,
    // width: width,
    display: display,
    padding: padding,
  }
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
          // this.setState({
          //   height: 'initial',
          //   // width: '100%',
          //   opacity: '1',
          //   padding: '1.618em',
          // })
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
          // display: 'block', opacity: '0'
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
        setTimeout(
          () => {
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
          },
          //this.setState({display: 'none', opacity: '0'})
          500
        )

        const immediateTransition = {
          ...this.videoHeight(false),
          ...this.videoOpacity(false),
          ...this.videoPadding(false),
          // height: '0',
          // // width: '0',
          // opacity: '0',
          // padding: '0',
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

  componentWillReceiveProps(nextProps) {
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

    if (e.keyCode === Key.ESC) {
      this.closeTray()
    }
  }

  handleKeyDown = e => {
    // prevent the default behavior for tree control keys
    // these are the control keys used by the tree menu
    const controlKeys = [ Key.ESC ]
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
      // width,
      opacity,
      padding,
      videoReady,
    } = this.state
    const {url, protocol} = this.props

    return (
      <div
        style={styleTrayContainer(open, display, height, padding)}
        ref={container => {
          this.container = container
        }}
        onKeyUp={this.handleKeyPressed}
        onKeyDown={this.handleKeyDown}
      >
        {videoReady ? (
          <Video
            ref={videoRef => {
              this.videoRef = videoRef
            }}
            link={url}
            protocol={protocol}
            aspectRatio={0.5625}
            autofocus={videoReady}
            height={height}
          />
        ) : null}
      </div>
    )
  }
}

export default VideoTray
