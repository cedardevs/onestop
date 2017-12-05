import React from 'react'

const styleMain = {
  display: 'flex',
  flexFlow: 'nowrap',
  justifyContent: 'space-between',
}

const styleList = {
  listStyleType: 'none',
  width: '30em',
  minWidth: '15em',
  height: 'auto',
  backgroundColor: '#242C36',
  padding: '0.1em',
  borderRadius: '0.1em 0.4em',
  margin: 0,
  flex: 0,
}

const styleListElement = {
  fontSize: '1.15em',
  padding: '0.3em',
  margin: '1em 0.75em',
  borderRadius: '0.1em 0.4em',
  border: '1px darkgray solid',
  textAlign: 'center',
  cursor: 'pointer',
}

const styleListElementSelected = {
  backgroundColor: 'black',
}

const styleVideos = {
  flex: 1,
  width: '100%',
  overflow: 'hidden',
}

const styleVideoContainer = {
  margin: 0,
  padding: 0,
}

const styleShownVideo = {
  opacity: 1,
  display: 'block',
}

export default class VideoView extends React.Component {

  constructor(props) {
    super(props)

    this.listElementSelected = this.listElementSelected.bind(this)
    this.videoShown = this.videoShown.bind(this)
    this.onClick = this.onClick.bind(this)

    this.state = {
      current: 0,
    }
  }

  collectVideos = () => {
    // collect any video iframes in the component
    this.figures = this.sectionRef.querySelectorAll('figure[class=\'video\']')
    this.iframes = this.sectionRef.querySelectorAll(
        'iframe[src*=\'//www.youtube.com\']',
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
    return i === this.state.current
  }

  onClick(i) {
    this.windowResizing()
    this.setState({
      current: i,
    })
  }

  render() {
    const {links} = this.props
    let embeddedVideos = []
    let titleList = []

    links.forEach((link, index) => {
      if (this.videoShown(index)) {
        const url = link.linkUrl
        const linkWithOptions = url.indexOf('?') > 0 ? `${url}&rel=0` : `${url}?rel=0`
        embeddedVideos.push(
            <figure key={index} className="video" style={styleVideoContainer}>
              <iframe
                  src={linkWithOptions}
                  frameBorder="0"
                  data-aspectratio="0.5625"
                  style={{width: '800px', height: '450px'}}
                  allowFullScreen={true}
              />
            </figure>,
        )
      }

      titleList.push(
          <li key={index} onClick={() => this.onClick(index)} style={{
            ...styleListElement,
            ...this.listElementSelected(index)
          }}>{link.linkName}</li>,
      )
    })

    return (
        <div style={styleMain}>
          <ul style={styleList}>
            {titleList}
          </ul>
          <div style={styleVideos} ref={sectionRef => {
            this.sectionRef = sectionRef
          }}>
            {embeddedVideos}
          </div>
        </div>
    )
  }
}



