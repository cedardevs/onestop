import React from 'react'
import styles from './resultLayout.css'

class ResultLayout extends React.Component {
  constructor(props) {
    super(props)
  }

  render() {
    return <div id="layout" className={`pure-g ${styles.mainWindow}`}>
      <div className={`pure-u-1 ${styles.resultsContainer508}`}>
        {this.props.children}
      </div>
    </div>
  }
}

export default ResultLayout
