import React from 'react'
import styles from './background.css'
import Modernizr from 'modernizr'

class BackgroundComponent extends React.Component {
  constructor(props) {
    super(props)
    this.state = {
      background: true
    }
  }

  componentDidMount(){
    this.setState({background: this.props.showImage})
  }

  componentWillReceiveProps(nextProps){
    this.setState({background: nextProps.showImage})
  }

  render() {
    return <div className={styles.backgroundColor}><div className={styles.background}></div></div>
  }
}

export default BackgroundComponent
