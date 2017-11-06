import React from 'react'
import styles from './banner.css'

class Banner extends React.Component {

  constructor(props) {
    super(props)
  }

  render() {
    if (!this.props.message) {
      return null
    }

    const configStyle = {
      color: this.props.colors && this.props.colors.text || 'white',
      background: this.props.colors && this.props.colors.background || 'red'
    }

    return <div style={configStyle} className={styles.banner}>
      {this.props.message}
    </div>
  }

}

export default Banner