import React from 'react'
import styles from './background.css'
import Modernizr from 'modernizr'

class Background extends React.Component {
  constructor(props) {
    super(props)
  }

  render() {
    var backgroundStyle = this.props.showImage
      ? styles.backgroundImage
      : styles.backgroundSolid
    var backgroundOverlay = this.props.showOverlay
      ? styles.backgroundOverlay
      : {}

    return (
      <div className={backgroundStyle}>
        <div className={backgroundOverlay} />
      </div>
    )
  }
}

export default Background
