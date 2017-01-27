import React, { PropTypes } from 'react'
import { Link, hashHistory } from 'react-router'
import _ from 'lodash'
import MapContainer from '../MapContainer'
import styles from './list.css'

class GranuleList extends React.Component {
  constructor(props) {
    super(props)
  }

  render() {
    const rows = []
    const dataFormats = 'Data Formats TBD'
    const tags = 'Tags TBD'
    // FIXME Which granule fields are being displayed in table?

    _.forEach(this.props.results, (value, key) => {
      rows.push(
        <tr key={key} onMouseOver={() => this.props.onMouseOver(key)} onMouseLeave={() => this.props.onMouseOver(key)}>
          <td>{value.title}</td>
          {/*<td>{value.modifiedDate}</td>*/}
          {/*<td>{dataFormats}</td>*/}
          {/*<td>{tags}</td>*/}
        </tr>
      )
    })

    return (
      <div><Link to="collections" className={styles.navLink}>Return To Collection Results</Link>
    <div className={`pure-g ${styles.mainWindow}`}>
          <div className={`pure-u-1-2 ${styles.map}`}>
            <MapContainer style={styles.mapContainer}/>
          </div>
          <div className={`pure-u-1-2`}>
            <div className={`${styles.granuleInfo}`}>
              <div className={`${styles.title}`}>
                {this.props.selectedCollection.title}
              </div>
              <div className={`${styles.description}`}>
                {this.props.selectedCollection.description}
              </div>
              <table className={`pure-table ${styles.table}`}>
                <thead>
                <tr>
                  <th>Title</th>
                  {/*<th>Date Modified</th>*/}
                  {/*<th>Data Formats</th>*/}
                  {/*<th>Tags</th>*/}
                </tr>
                </thead>
                <tbody>{rows}</tbody>
              </table>
            </div>
          </div>
        </div>
      </div>
    )
  }
}

GranuleList.propTypes = {
  results: PropTypes.object,
  focusedIds: PropTypes.array,
  selectedCollection: PropTypes.object,
  onMouseOver: PropTypes.func,
}

export default GranuleList
