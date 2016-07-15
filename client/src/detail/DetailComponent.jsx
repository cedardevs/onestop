import React, {PropTypes} from 'react'
import styles from './detail.css'

class Detail extends React.Component {
  constructor(props) {
    super(props)

    this.close = this.close.bind(this)
    this.handleKeyDown = this.handleKeyDown.bind(this)
  }

  render() {
    if (!this.props.id) {
      return <div style={{display: 'none'}}></div>
    }

    return <div className={styles.modal}>
      <div className={styles.modalContent}>
        <span className={styles.close} onClick={this.close}>x</span>
        <p>{this.props.item.description}</p>
      </div>
    </div>
  }

  close() {
    this.props.dismiss()
  }

  handleKeyDown(event) {
    if (event.keyCode === 27) { // esc
      this.close()
    }
  }

  componentWillUpdate(nextProps, nextState) {
    if (nextProps.id) {
      document.addEventListener("keydown", this.handleKeyDown, false);
    }
    else {
      document.removeEventListener("keydown", this.handleKeyDown, false);
    }
  }

}

Detail.propTypes = {
  id: PropTypes.string,
  item: PropTypes.object,
}

export default Detail
