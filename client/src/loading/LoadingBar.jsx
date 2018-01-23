import React from 'react'
import styles from './LoadingBar.css'

import defaultStyles from '../common/defaultStyles'

export class LoadingBar extends React.Component {
  constructor(props) {
    super(props)

    this.state = {
      loadingText: '',
    }
  }

  componentWillReceiveProps(nextProps) {
    this.setState(prevState => {
      return {
        ...prevState,
        loadingText: !_.isEqual(this.props.loadingText, nextProps.loadingText)
          ? nextProps.loadingText
          : '',
      }
    })
  }

  render() {
    const {loading, loadingText} = this.props

    return (
      <div>
        <div
          aria-live="polite"
          aria-atomic="false"
          style={defaultStyles.hideOffscreen}
        >
          <div id={this.props.loadingAlertId}>{this.state.loadingText}</div>
        </div>
        <div className={loading ? styles.loadingContainer : null} />
      </div>
    )
  }
}

export default LoadingBar
