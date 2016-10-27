import React from 'react'
import styles from './alphaBanner.css'
import config from '../config'

class AlphaBanner extends React.Component {

  constructor(props) {
    super(props)
    this.setState({message: null})
  }

  componentDidMount() {
    config.then((config => {
      if (config && config.disclaimer) {
        this.setState({message: 'Not an official US Government website - OneStop demonstration site to allow quick feedback from our stakeholders.'})
      }
    }))
  }

  render() {
    return this.message ? <div className={styles.banner}>{this.message}</div> : null
  }

}

export default AlphaBanner