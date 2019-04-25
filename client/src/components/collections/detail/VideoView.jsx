import React from 'react'
import Video from '../../common/Video'

const styleVideoView = {
  display: 'flex',
  flexDirection: 'column',
}

const styleVideoTabs = {
  display: 'flex',
}

const styleList = {
  listStyleType: 'none',
  width: '30em',
  minWidth: '15em',
  height: 'auto',
  backgroundColor: '#3a3a3a',
  padding: '0.618em',
  margin: 0,
  flex: 0,
}

const styleListElement = {
  fontSize: '1.15em',
  padding: '0.309em',
  margin: '0 0 0.618em 0',
  borderRadius: '0.1em 0.4em',
  border: '1px darkgray solid',
  textAlign: 'center',
  cursor: 'pointer',
  backgroundColor: '#222',
  color: '#f9f9f9',
}

const styleListElementSelected = {
  backgroundColor: '#b0d1ea',
  color: '#222',
}

export default class VideoView extends React.Component {
  constructor(props) {
    super(props)
    this.state = {
      current: 0,
    }
  }

  listElementSelected = i => {
    return i === this.state.current ? styleListElementSelected : ''
  }

  videoShown = i => {
    return i === this.state.current
  }

  onClick = i => {
    // this.windowResizing()
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
        const linkWithOptions =
          url.indexOf('?') > 0 ? `${url}&rel=0` : `${url}?rel=0`

        embeddedVideos.push(
          <Video
            key={index}
            link={linkWithOptions}
            protocol={link.linkProtocol}
            aspectRatio={0.5625}
          />
        )
      }

      // TODO link name needs to not be blank for this to work!!!
      titleList.push(
        <li
          key={index}
          onClick={() => this.onClick(index)}
          style={{
            ...styleListElement,
            ...this.listElementSelected(index),
          }}
        >
          {link.linkName}
        </li>
      )
    })

    const videoUrl = links ? links[0].linkUrl : null
    const videoUrlWithOptions =
      videoUrl.indexOf('?') > 0 ? `${videoUrl}&rel=0` : `${videoUrl}?rel=0`

    return (
      <div style={styleVideoView}>
        <div style={styleVideoTabs}>
          <ul style={styleList}>{titleList}</ul>
          {embeddedVideos}
        </div>
      </div>
    )
  }
}
