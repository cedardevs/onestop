import React, { PropTypes } from 'react'
import MapContainer from '../MapContainer'

class GranuleList extends React.Component {
  constructor(props) {
    super(props)
    this.results = props.results
    this.focusedIds = props.focusedIds
    this.onMouseOver = props.onMouseOver
  }

  render() {
    const rows = []
    const dataFormats = 'Data Formats TBD'
    const tags = 'Tags TBD'
    // FIXME Which granule fields are being displayed in table?
    this.results.forEach((value, key) => {
      rows.push(
        <tr key={key} onMouseOver={() => this.onMouseOver(key)}>
          <td>{value.get('description')}</td>
          <td>{value.get('modifiedDate')}</td>
          <td>{dataFormats}</td>
          <td>{tags}</td>
        </tr>
      )
    })

    return <div>
    <table className="pure-table">
        <thead>
          <tr>
            <th>Description</th>
            <th>Date Modified</th>
            <th>Data Formats</th>
            <th>Tags</th>
          </tr>
        </thead>
        <tbody>{rows}</tbody>
      </table>
      {/* Remove this line and uncomment map component below for map test */}
      {/*<MapContainer />*/}
    </div>
  }
}

export default GranuleList
