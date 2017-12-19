import React from 'react'
import styles from './LoadingBar.css'

export class LoadingBar extends React.Component {
  render() {
    const {loading} = this.props

    return <div className={loading ? styles.loadingContainer : null} />
  }
}

export default LoadingBar
