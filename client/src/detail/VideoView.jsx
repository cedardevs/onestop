import React from 'react'
import Expandable from '../common/Expandable'

const styleMain = {
  display: 'flex',
  flexFlow: 'nowrap'
  // justifyContent: 'center'
}

const styleList = {
  listStyleType: 'none',
  width: '25%',
  height: 'auto',
  backgroundColor: '#242C36',
  padding: 0,
  borderRadius: '0.1em 0.4em',
  margin: '0.1em',
  flex: 0
}

const styleListElement = {
  fontSize: '1.15em',
  padding: '0.3em',
  margin: '1em 0.75em',
  borderRadius: '0.1em 0.4em',
  border: '1px darkgray solid',
  textAlign: 'center',
  cursor: 'pointer'
}

const styleListElementSelected = {
  backgroundColor: 'black'
}

const styleVideos = {
  flex: 1,
  width: '100%'
}

const styleVideoContainer = {
  // position: 'relative',
  // paddingBottom: '56.25%',
  // paddingTop: '30px',
  // height: 0,
  // overflow: 'hidden',
  opacity: 0,
  display: 'none'
}

const styleShownVideo = {
  opacity: 1,
  display: 'block'
}

const styleIFrame = {
  position: 'absolute',
  top: 0,
  left: 0,
  width: '100%',
  height: '100%',
  maxWidth: '800px',
  maxHeight: '450px'
}

export default class VideoView extends React.Component {

  constructor(props) {
    super(props)

    this.listElementSelected = this.listElementSelected.bind(this)
    this.videoShown = this.videoShown.bind(this)
    this.onClick = this.onClick.bind(this)

    this.state = {
      current: 0
    }
  }

  collectVideos = () => {
    // collect any video iframes in the component
    this.figures = this.sectionRef.querySelectorAll("figure[class='video']")
    this.iframes = this.sectionRef.querySelectorAll(
      "iframe[src^='//www.youtube.com']"
    )
    this.iframes.forEach(iframe => {
      // calculate and set aspect ratio
      let rect = iframe.getBoundingClientRect()
      let aspectRatio = rect.height / rect.width
      iframe.setAttribute('data-aspectratio', aspectRatio)
      // remove any explicit width and height attributes that may be set
      iframe.removeAttribute('height')
      iframe.removeAttribute('width')
    })
  }

  doneResizing = () => {
    // do work only if any figures exist to be resized
    if (this.figures.length > 0) {
      // get width of a figure (expecting all to be same 100% of article)
      const figureRect = this.figures[0].getBoundingClientRect()
      const newWidth = figureRect.width
      this.iframes.forEach(iframe => {
        // maintain aspectRatio when setting new dimensions
        const aspectRatio = iframe.getAttribute('data-aspectratio')
        iframe.style.width = newWidth + 'px'
        iframe.style.height = newWidth * aspectRatio + 'px'
      })
    }
  }

  windowResizing = () => {
    // prevent resize work unless threshold is reached
    // this helps ensure the new width doesn't stick on a transient value
    const resizeThreshold = 250 // ms
    clearTimeout(this.resizeId)
    this.resizeId = setTimeout(this.doneResizing, resizeThreshold)
  }

  componentDidMount() {
    this.collectVideos()
    this.windowResizing()
    window.addEventListener('resize', this.windowResizing)
  }

  componentDidUpdate() {
    this.collectVideos()
  }

  componentWillUnmount() {
    window.removeEventListener('resize', this.windowResizing)
  }

  listElementSelected(i) {
    return i === this.state.current ? styleListElementSelected : ''
  }

  videoShown(i) {
    return i === this.state.current ? styleShownVideo : ''
  }

  onClick(i) {
    this.setState({
      current: i
    })
  }

  render() {
    const { links } = this.props
    let embeddedVideos =[]
    let titleList = []

    links.forEach((link, index) => {
      embeddedVideos.push(
        <figure key={index} className="video" style={{
          ...styleVideoContainer,
          ...this.videoShown(index)
        }}>
          <iframe
            src={link.linkUrl}
            frameBorder="0"
            data-aspectratio="0.5625"
            style={{ width: '800px', height: '450px' }}
            // style={styleIFrame}
            allowFullScreen={true}
          />
        </figure>
      )

      titleList.push(
        <li key={index} onClick={() => this.onClick(index)} style={{
          ...styleListElement,
          ...this.listElementSelected(index)
        }}>{link.linkName}</li>
      )
    })

    return (
      <div style={styleMain}>
        <ul style={styleList}>
          {titleList}
        </ul>
        <div style={styleVideos} ref={sectionRef => {this.sectionRef = sectionRef}}>
          {embeddedVideos}
        </div>
      </div>
    )
  }
}



