import React from 'react'
import IndexDropDown from './IndexDropDownComponent'
import PrimarySearchComponent from './PrimarySearchComponent'
import MapContainer from './map/MapContainer'
import styles from './landing.css'

const LandingComponent = ({indexName, submit, handleIndexChange}) => {
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
      <MapContainer />
    </div>
    </div>
}

export default LandingComponent
