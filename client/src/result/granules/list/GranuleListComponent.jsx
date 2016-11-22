import React, { PropTypes } from 'react'
import MapContainer from '../MapContainer'
import styles from './list.css'

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
        <tr key={key} onMouseOver={() => this.onMouseOver(key)} onMouseLeave={() => this.onMouseOver(key)}>
          <td>{value.get('title')}</td>
          <td>{value.get('modifiedDate')}</td>
          <td>{dataFormats}</td>
          <td>{tags}</td>
        </tr>
      )
    })

    return <div>
    <div className={styles.granuleHeader}>
      <div className={styles.leftDescription}>
        <div className={styles.leftTitle}>
          {this.props.selectedCollection.title}
        </div>
        <div>
          {this.props.selectedCollection.description}
        </div>
      </div>
      <div className={styles.rightMap}>
        <MapContainer />
      </div>
    </div>
    <table className={`pure-table ${styles.table}`}>
        <thead>
          <tr>
            <th>Title</th>
            <th>Date Modified</th>
            <th>Data Formats</th>
            <th>Tags</th>
          </tr>
        </thead>
        <tbody>{rows}</tbody>
      </table>
    </div>
  }
}

export default GranuleList
