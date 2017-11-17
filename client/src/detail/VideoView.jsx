import React from 'react'
import Expandable from '../common/Expandable'

const styleVideoHeading = {
    border: "1px solid white",
    backgroundColor: "green"
}

const styleVideoContent = {
    border: "1px solid white",
    backgroundColor: "magenta"
}

export default class VideoView extends React.Component {
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

  headingElement = (title) => {
      return (
          <div>
              {title}
          </div>
      )
  }

  render() {
    const { links } = this.props;

    const embeddedVideos = links.map((link, index) => {
      const videoFigure = (
        <figure key={index} className="video">
          <iframe
            src={link}
            frameBorder="0"
            data-aspectratio="0.5625"
            style={{ width: '800px', height: '450px' }}
            allowFullScreen={true}
          />
        </figure>
      )
      return <Expandable open={false} heading={"This is the star wars trailer for accessibility purposes."} content={videoFigure} />
    })

    return (
      <section
        ref={sectionRef => {
          this.sectionRef = sectionRef
        }}
      >
        {embeddedVideos}
      </section>
    )
  }
}
