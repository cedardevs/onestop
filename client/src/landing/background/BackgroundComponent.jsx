import React from 'react'
import styles from './background.css'

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
    const bgStyle = this.state.background ? styles.backgroundImage : styles.backgroundColor
    return <div className={bgStyle}></div>
  }
}

export default BackgroundComponent
