import React from 'react'
import FlexColumn from '../../common/FlexColumn'
import FlexRow from '../../common/FlexRow'
import TextInput from '../../common/input/TextInput'

const DirectionID = {
  WEST: 'west',
  SOUTH: 'south',
  EAST: 'east',
  NORTH: 'north',
}

const inputSize = 8

const styleMin = {
  justifyContent: 'center',
  alignSelf: 'stretch',
  marginBottom: '0.309em',
}

const styleMax = {
  justifyContent: 'center',
  alignSelf: 'stretch',
  marginBottom: '0.618em',
}

const validateBounds = (bounds) => {
  return bounds
}

export default class MapFilterBoundingBoxInput extends React.Component {
  componentWillMount() {
    // initial/default state
    this.setState({
      west: -180,
      south: -90,
      east: 180,
      north: 90,
    })
  }

  componentWillReceiveProps(nextProps) {
    // if(nextProps.bounds) {
    //   this.setState(prevProps => {
    //     return {
    //       ...prevProps,
    //       west: nextProps.bounds.west,
    //       south: nextProps.bounds.south,
    //       east: nextProps.bounds.east,
    //       north: nextProps.bounds.north,
    //     }
    //   })
    // }
  }

  validate = (value, id) => {
    const { boundsSource, updateBounds } = this.props
    const { west, south, east, north } = this.state

    switch (id) {
      case DirectionID.WEST:
        let validWest = value >= -180 && value <= 180
        if(validWest && boundsSource !== "filter") {
          updateBounds({ west: value, south: south, east: east, north: north}, "filter")
        }
        return validWest
        break
      case DirectionID.SOUTH:
        let validSouth = value >= -90 && value <= 90
        if(validSouth && boundsSource !== "filter") {
          updateBounds({ west: west, south: value, east: east, north: north}, "filter")
        }
        return validSouth
        break
      case DirectionID.EAST:
        let validEast = value >= -180 && value <= 180
        if(validEast && boundsSource !== "filter") {
          updateBounds({ west: west, south: south, east: value, north: north}, "filter")
        }
        return validEast
        break
      case DirectionID.NORTH:
        let validNorth = value >= -90 && value <= 90
        if(validNorth && boundsSource !== "filter") {
          updateBounds({ west: west, south: south, east: east, north: value}, "filter")
        }
        return validNorth
        break
      default:
        if(boundsSource !== "filter") {
          updateBounds(undefined, "filter")
        }
        return false
    }
  }

  render() {
    const { west, south, east, north } = this.state

    return (
      <div>
        <FlexRow
          style={styleMin}
          items={[
            <FlexColumn
                key="north-col"
                items={[
                  <TextInput
                      key={DirectionID.NORTH}
                      id={DirectionID.NORTH}
                      name="North"
                      value={north.toFixed(2)}
                      size={inputSize}
                      validate={this.validate}
                      label="North"
                  />,
                ]}
            />,
            <FlexColumn
                key="east-col"
                items={[
                  <TextInput
                      key={DirectionID.EAST}
                      id={DirectionID.EAST}
                      name="East"
                      value={east.toFixed(2)}
                      size={inputSize}
                      validate={this.validate}
                      label="East"
                  />,
                ]}
            />,
          ]}
        />
        <FlexRow
          style={styleMax}
          items={[
            <FlexColumn
                key="west-col"
                items={[
                  <TextInput
                      key={DirectionID.WEST}
                      id={DirectionID.WEST}
                      name="West"
                      value={west.toFixed(2)}
                      size={inputSize}
                      validate={this.validate}
                      label="West"
                  />,
                ]}
            />,
            <FlexColumn
                key="south-col"
                items={[
                  <TextInput
                      key={DirectionID.SOUTH}
                      id={DirectionID.SOUTH}
                      name="South"
                      value={south.toFixed(2)}
                      size={inputSize}
                      validate={this.validate}
                      label="South"
                  />,
                ]}
            />
          ]}
        />
      </div>
    )
  }
}
