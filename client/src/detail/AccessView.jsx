import React from 'react'
import DetailGrid from './DetailGrid'
import A from '../common/link/Link'

const styleParagraph = {
  padding: 0,
  margin: '0 0 1.618em 0',
}

const styleParagraphLast = {
  padding: 0,
  margin: 0,
}

const styleContent = {
  padding: '1.618em',
}

const styleContentList = {
  padding: '1.618em',
  marginLeft: '1.618em',
}

const styleLink = {
  display: 'inline-block',
  color: 'rgb(85, 172, 228)',
  margin: '0 0 0.618em 0',
}

const styleHeadingWrapper = {
  display: 'flex',
  backgroundColor: '#b0d1ea',
  justifyContent: 'center',
  alignItems: 'center',
  alignSelf: 'stretch',
  height: '100%',
  margin: 0,
}

const styleHeading = {
  margin: 0,
  padding: '0.618em',
  width: '100%'
}

export default class AccessView extends React.Component {
  render() {
    const {item} = this.props

    let information = item.links
      .filter(link => link.linkFunction === 'information')
      .map((link, index, arr) => {
        const lastIndex = arr.length - 1
        const {linkUrl, linkName, linkProtocol, linkDescription} = link
        const linkTitle = linkName ? linkName : linkProtocol
        return (
          <div key={index}>
            <A
              href={linkUrl}
              target="_blank"
              title={linkTitle}
              style={styleLink}
            >
              {linkTitle}
            </A>
            <p
              style={index === lastIndex ? styleParagraphLast : styleParagraph}
            >
              {linkDescription}
            </p>
          </div>
        )
      })

    let informationList = (
      <div style={styleContent}>No information links in metadata.</div>
    )
    if (information.length > 0) {
      informationList = <div style={styleContent}>{information}</div>
    }

    let downloadData = item.links
      .filter(link => link.linkFunction === 'download')
      .map((link, index, arr) => {
        const lastIndex = arr.length - 1
        const {linkUrl, linkName, linkProtocol, linkDescription} = link
        const linkTitle = linkName ? linkName : linkProtocol
        return (
          <div key={index}>
            <A
              href={linkUrl}
              target="_blank"
              title={linkTitle}
              style={styleLink}
            >
              {linkTitle}
            </A>
            <p
              style={index === lastIndex ? styleParagraphLast : styleParagraph}
            >
              {linkDescription}
            </p>
          </div>
        )
      })

    let downloadDataList = (
      <div style={styleContent}>No download data links in metadata.</div>
    )
    if (downloadData.length > 0) {
      downloadDataList = <div style={styleContent}>{downloadData}</div>
    }

    const dataFormats = item.dataFormats ? item.dataFormats : []
    const distributionsFormats = dataFormats.map((format, index) => {
      return <li key={index}>{format.name}</li>
    })

    let distributionFormatsList = (
      <ul style={styleContentList}>{distributionsFormats}</ul>
    )
    if (dataFormats.length === 0) {
      distributionFormatsList = 'No formats in metadata.'
    }

    const informationHeader = (
      <div style={styleHeadingWrapper}>
        <h3 style={styleHeading}>Information</h3>
      </div>
    )
    const downloadDataHeader = (
      <div style={styleHeadingWrapper}>
        <h3 style={styleHeading}>Download Data</h3>
      </div>
    )
    const distributionFormatsHeader = (
      <div style={styleHeadingWrapper}>
        <h3 style={styleHeading}>Distribution Formats</h3>
      </div>
    )

    const accessGrid = [
      [ informationHeader, informationList ],
      [ downloadDataHeader, downloadDataList ],
      [ distributionFormatsHeader, distributionFormatsList ],
    ]

    return <DetailGrid grid={accessGrid} colWidths={[ {sm: 3}, {sm: 9} ]} />
  }
}
