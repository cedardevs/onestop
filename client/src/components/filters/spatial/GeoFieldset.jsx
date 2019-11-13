import React, {useState} from 'react'
import {styleForm} from '../common/styleFilters'
import FilterFieldset from '../FilterFieldset'
import InputRow from './InputRow' // TODO rename input row

const GeoFieldset = ({bounds, handleKeyDown}) => {
  return (
    <div>
      <form
        style={styleForm}
        onKeyDown={handleKeyDown}
        aria-describedby="mapFilterInstructions"
      >
        <FilterFieldset legendText="Bounding Box Coordinates:">
          <InputRow
            direction="west"
            placeholder="-180.0 to 180.0"
            value={bounds.west}
            onChange={e => bounds.setWest(e.target.value)}
          />
          <InputRow
            direction="south"
            placeholder=" -90.0 to  90.0"
            value={bounds.south}
            onChange={e => bounds.setSouth(e.target.value)}
          />
          <InputRow
            direction="east"
            placeholder="-180.0 to 180.0"
            value={bounds.east}
            onChange={e => bounds.setEast(e.target.value)}
          />
          <InputRow
            direction="north"
            placeholder=" -90.0 to  90.0"
            value={bounds.north}
            onChange={e => bounds.setNorth(e.target.value)}
          />
        </FilterFieldset>
      </form>
    </div>
  )
}
export default GeoFieldset
