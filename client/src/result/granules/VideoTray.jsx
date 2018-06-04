import React from 'react'
import _ from 'lodash'
import ReactDOM from 'react-dom'

import Video from '../../common/Video'

const styleTrayContainer = (open, display, height, padding) => {
  return {
    boxSizing: 'border-box',
    width: '100%',
    transition: open // immediate transition
      ? 'height 0.2s 0.0s, padding 0.2s 0.0s'
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
    // const {showVideo} = this.props
    // this.state = {
    //   open: showVideo,
    //   display: showVideo ? 'block' : 'none',
    //   height: showVideo ? '100%' : '0em',
    //   width: showVideo ? '100%' : '0%',
    //   opacity: showVideo ? '1' : '0',
    //   padding: showVideo ? '1.618em' : '0',
    // }
    // this.state = this.videoState(false)
    this.state = {
      ...{open: false},
      ...this.videoDisplay(false),
      ...this.videoHeight(false),
      ...this.videoOpacity(false),
      ...this.videoPadding(false),
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

    //componentWillReceiveProps(nextProps)
    // if (this.props.showVideo != nextProps.showVideo) {
    this.setState(prevState => {
      const isOpen = prevState.open
      const isDisplayed = prevState.display === 'block'
      const shouldClose = isOpen && isDisplayed
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
      }
      if (shouldClose) {
        setTimeout(
          () => {
            this.setState(prevState => {
              return {
                ...prevState,
                ...this.videoDisplay(false),
                ...this.videoOpacity(false),
              }
            })
          },
          //this.setState({display: 'none', opacity: '0'})
          500
        )
      }

      const immediateTransition = shouldOpen
        ? {
            // display: 'block', opacity: '0'
            ...this.videoDisplay(true),
            ...this.videoOpacity(false),
          }
        : shouldClose
          ? {
              ...this.videoHeight(false),
              ...this.videoOpacity(false),
              ...this.videoPadding(false),
              height: '0',
              // width: '0',
              opacity: '0',
              padding: '0',
            }
          : {}
      return {open: !isOpen, ...immediateTransition}
    })
    // }
  }

  componentWillUnmount() {
    this.container.removeEventListener(
      'transitionend',
      this.handleTransitionEnd
    )
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
