import React, {useState} from 'react'
import {styleForm} from '../common/styleFilters'
import FilterFieldset from '../FilterFieldset'
import CoordinateTextbox from './CoordinateTextbox'

const GeoFieldset = ({bounds, handleKeyDown}) => {
  return (
    <div>
      <form
        style={styleForm}
        onKeyDown={handleKeyDown}
        aria-describedby="mapFilterInstructions"
      >
        <FilterFieldset legendText="Bounding Box Coordinates:">
          <CoordinateTextbox
            name="west"
            placeholder="-180.0 to 180.0"
            value={bounds.west.get}
            valid={bounds.west.valid}
            onChange={e => bounds.west.set(e.target.value)}
          />
          <CoordinateTextbox
            name="south"
            placeholder=" -90.0 to  90.0"
            value={bounds.south.get}
            valid={bounds.south.valid}
            onChange={e => bounds.south.set(e.target.value)}
          />
          <CoordinateTextbox
            name="east"
            placeholder="-180.0 to 180.0"
            value={bounds.east.get}
            valid={bounds.east.valid}
            onChange={e => bounds.east.set(e.target.value)}
          />
          <CoordinateTextbox
            name="north"
            placeholder=" -90.0 to  90.0"
            value={bounds.north.get}
            valid={bounds.north.valid}
            onChange={e => bounds.north.set(e.target.value)}
          />
        </FilterFieldset>
      </form>
    </div>
  )
}
export default GeoFieldset
