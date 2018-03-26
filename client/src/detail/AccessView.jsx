import React from 'react'
import A from '../common/link/Link'

const styleTable = {
  border: '1px solid #E0E0E0',
  width: '100%',
}

const styleTableRow = {
  borderBottom: '1px solid gray',
}

const styleTableHeading = {
  color: 'white',
  backgroundColor: '#222',
  borderRight: '1px solid gray',
  whiteSpace: 'nowrap',
  width: '1%',
  padding: '1.618em',
}

const styleTableCell = {
  padding: '1.618em',
}

const styleTableCellParagraph = {
  padding: 0,
  margin: '0 0 1.618em 0',
}

const styleTableCellParagraphLast = {
  padding: 0,
  margin: 0,
}

const styleTableCellList = {
  padding: 0,
  margin: '0 0 0 1.618em ',
}

const styleTableCellLink = {
  display: 'inline-block',
  color: 'rgb(85, 172, 228)',
  margin: '0 0 0.618em 0',
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
              style={styleTableCellLink}
            >
              {linkTitle}
            </A>
            <p
              style={
                index === lastIndex ? (
                  styleTableCellParagraphLast
                ) : (
                  styleTableCellParagraph
                )
              }
            >
              {linkDescription}
            </p>
          </div>
        )
      })

    if (information.length === 0) {
      information = 'No information links in metadata.'
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
              style={styleTableCellLink}
            >
              {linkTitle}
            </A>
            <p
              style={
                index === lastIndex ? (
                  styleTableCellParagraphLast
                ) : (
                  styleTableCellParagraph
                )
              }
            >
              {linkDescription}
            </p>
          </div>
        )
      })

    if (downloadData.length === 0) {
      downloadData = 'No download data links in metadata.'
    }

    const dataFormats = item.dataFormats ? item.dataFormats : []
    const distributionsFormats = dataFormats.map((format, index) => {
      return <li key={index}>{format.name}</li>
    })

    let distributionFormatsList = (
      <ul style={styleTableCellList}>{distributionsFormats}</ul>
    )
    if (dataFormats.length === 0) {
      distributionFormatsList = 'No formats in metadata.'
    }

    return (
      <div>
        <table style={styleTable}>
          <tbody>
            <tr style={styleTableRow}>
              <th style={styleTableHeading}>Information</th>
              <td style={styleTableCell}>{information}</td>
            </tr>
            <tr style={styleTableRow}>
              <th style={styleTableHeading}>Download Data</th>
              <td style={styleTableCell}>{downloadData}</td>
            </tr>
            <tr style={styleTableRow}>
              <th style={styleTableHeading}>Distribution Formats</th>
              <td style={styleTableCell}>{distributionFormatsList}</td>
            </tr>
            {/*<tr style={styleTableRow}>*/}
            {/*<th style={styleTableHeading}>Citations</th>*/}
            {/*<td style={styleTableCell}></td>*/}
            {/*</tr>*/}
          </tbody>
        </table>
      </div>
    )
  }
}
