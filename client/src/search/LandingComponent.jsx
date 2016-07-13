import React from 'react'
import IndexDropDown from './IndexDropDownComponent'
import PrimarySearchComponent from './PrimarySearchComponent'
import MapComponent from './map/MapComponent'
import styles from './landing.css'

const LandingComponent = ({indexName, submit, handleIndexChange}) => {
  console.log(submit)
  return <div><form className={`pure-form`}>
    <div className={styles.searchFields}>
      <span>
        <PrimarySearchComponent onEnterKeyDown={submit}/>
        <div className={styles.dropDown}>
          <IndexDropDown indexName={indexName} onChange={handleIndexChange}/>
        </div>
        </span>
    </div>
  </form>
    <div className={styles.mapContainer}>
      <MapComponent/>
    </div>
    </div>
}

export default LandingComponent
