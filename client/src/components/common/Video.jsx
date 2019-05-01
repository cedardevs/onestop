import React from 'react'
import ReactDOM from 'react-dom'

import {govExternalYouTubeMsg, isGovExternal} from '../../utils/urlUtils'

const styleDisclaimer = {
  color: '#f9f9f9',
  backgroundColor: '#1a1a1a',
  margin: 0,
  fontStyle: 'italic',
  padding: '.5em',
}

export default class Video extends React.Component {
  componentDidMount() {
    // need to set dimensions intially before resize events
    this.debounceResize()
    // subsequent resize event will retrigger our calculation
    window.addEventListener('resize', this.debounceResize)
  }

  componentWillUnmount() {
    // remember to remove custom listeners before unmounting
    window.removeEventListener('resize', this.debounceResize)
  }

  debounceResize = () => {
    // prevent resize work unless threshold is reached
    // this helps ensure the new width doesn't stick on a transient value
    const resizeThreshold = 250 // ms
    clearTimeout(this.resizeId)
    this.resizeId = setTimeout(this.resize, resizeThreshold)
  }

  resize = () => {
    // do work only if iframe exists
    if (this.iframeRef) {
      // recall our aspect ratio from props
      const {aspectRatio, autofocus} = this.props
      // get new width dynamically
      const iframeRect = this.iframeRef.getBoundingClientRect()
      const newWidth = iframeRect.width
      // maintain aspect ratio when setting height
      this.iframeRef.style.height = newWidth * aspectRatio + 'px'
      if (autofocus) {
        ReactDOM.findDOMNode(this.iframeRef).focus()
      }
    }
  }

  componentDidUpdate(prevProps, prevState) {
    if (this.props.link != prevProps.link && this.videoRef) {
      // TODO figure out if two youtube links in the granules also need this kind of trigger?
      this.videoRef.load()
    }
  }

  render() {
    const {link, protocol} = this.props
    const youtubeVideo = (
      <div>
        <iframe
          ref={iframeRef => {
            this.iframeRef = iframeRef
          }}
          src={link}
          frameBorder={0}
          allowFullScreen={true}
          style={{width: '100%'}}
        />
        <div style={styleDisclaimer}>Disclaimer: {govExternalYouTubeMsg}</div>
      </div>
    )
    const mp4Video = (
      <video
        ref={videoRef => {
          this.iframeRef = videoRef
          this.videoRef = videoRef
        }}
        controls
        style={{width: '100%'}}
      >
        <source type="video/mp4" src={link} />
      </video>
    )
    const other = <div>Could Not Play Video</div>
    if (protocol === 'video:youtube') {
      return youtubeVideo
    }
    else if (link.includes('.mp4') && !isGovExternal(link)) {
      return mp4Video
    }
    else {
      return other
    }
  }
}
