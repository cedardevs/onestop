import React from 'react'
import styles from './alphaBanner.css'
import config from '../config'

class AlphaBanner extends React.Component {

  constructor(props) {
    super(props)
    this.state = {message: null}
  }

  componentDidMount() {
    config.then((config => {
      if (config && config.disclaimer) {
        this.state.message = 'Not an official US Government website - OneStop demonstration site to allow quick feedback from our stakeholders.'
      }
    }))
  }

  render() {
    return this.state.message ? <div className={styles.banner}>{this.state.message}</div> : null
  }

}

export default AlphaBanner