import React from 'react'
import styles from './background.css'

class BackgroundComponent extends React.Component {
  constructor(props) {
    super(props)
  }

  render() {
    const bgStyle = this.props.showImage ? styles.backgroundImage : styles.backgroundColor
    return <div className={bgStyle}>
      {this.props.pageData}
      </div>
  }
}

export default BackgroundComponent
