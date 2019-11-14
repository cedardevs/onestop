import React from 'react'
import styles from '../../style/defaultStyles'

export default class HiddenAccessibilityHeading extends React.Component {
  render() {
    const {content} = this.props
    return <div style={styles.hideOffscreen}>{content}</div>
  }
}
