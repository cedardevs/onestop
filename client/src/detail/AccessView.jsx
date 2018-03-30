import React from 'react'
import DetailGrid from './DetailGrid'
import A from '../common/link/Link'

const styleParagraph = {
  padding: 0,
  margin: '0 0 1.618em 0',
}

const styleContent = {
  padding: '1.618em',
}

const styleContentList = {
  padding: '0 1.618em 1.618em 1.618em',
  margin: '0 0 0 1.618em',
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

  renderAccessHeading = heading => {
    return (
        <div style={styleHeadingWrapper}>
          <h3 style={styleHeading}>{heading}</h3>
        </div>
    )
  }

  renderAccessLinkList = (links, notAvailable) => {
    let listItems = links.map((link, index) => {
      const {linkUrl, linkName, linkProtocol, linkDescription} = link
      const linkTitle = linkName ? linkName : linkProtocol
      return (
          <li key={index}>
            <A
                href={linkUrl}
                target="_blank"
                title={linkTitle}
                style={styleLink}
            >
              {linkTitle}
            </A>
            <p
                style={styleParagraph}
            >
              {linkDescription}
            </p>
          </li>
      )
    })
    let list = (
        <div style={styleContent}>{notAvailable}</div>
    )
    if (listItems.length > 0) {
      list = (
          <div style={styleContent}>
            <ul style={styleContentList}>{listItems}</ul>
          </div>
      )
    }
    return list
  }

  renderAccessList = (items, notAvailable) => {
    const list = items ? items : []
    if (list.length === 0) {
      return <div style={styleContent}>{notAvailable}</div>
    }
    const distributionsFormats = list.map((format, index) => {
      return <li key={index}>{format.name}</li>
    })
    return <div style={styleContent}><ul style={styleContentList}>{distributionsFormats}</ul></div>
  }

  render() {
    const {item} = this.props

    const informationHeading = this.renderAccessHeading("Information")
    const informationLinks = item.links.filter(link => link.linkFunction === 'information')
    const informationList = this.renderAccessLinkList(informationLinks, "No information links in metadata.")

    const downloadDataHeading = this.renderAccessHeading("Download Data")
    const downloadDataLinks = item.links.filter(link => link.linkFunction === 'download')
    const downloadDataList = this.renderAccessLinkList(downloadDataLinks, "No download links in metadata.")

    const distributionFormatsHeading = this.renderAccessHeading("Distribution Formats")
    const distributionFormatsList = this.renderAccessList(item.dataFormats, "No formats in metadata.")

    const accessGrid = [
      [ informationHeading, informationList ],
      [ downloadDataHeading, downloadDataList ],
      [ distributionFormatsHeading, distributionFormatsList ],
    ]

    return <DetailGrid grid={accessGrid} colWidths={[ {sm: 3}, {sm: 9} ]} />
  }
}
