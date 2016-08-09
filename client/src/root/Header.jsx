import React from 'react'
import { connect } from 'react-redux'
import styles from './root.css'

let Header = ({dispatch}) => {
  return (
  <div className={styles.header}>OneStop Sandbox</div>
  )
}

Header = connect()(Header)
export default Header
