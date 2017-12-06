import React from 'react'
import FlexColumn from '../common/FlexColumn'
import FlexRow from '../common/FlexRow'
import TextInput from '../common/input/TextInput'

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

export default class MapFilterBoundingBoxInput extends React.Component {
  validate = (value) => {
    console.log('validate::value:', value)
    if (value < 0) {
      return true
    } else {
      return false
    }
  }

  render() {
    const minCoordinatePlaceholder = {
      x: -180.0,
      y: -90.0,
    }

    const maxCoordinatePlaceholder = {
      x: 180.0,
      y: 90.0,
    }

    const boundsPlaceholder = {
      min: minCoordinatePlaceholder,
      max: maxCoordinatePlaceholder,
    }

    return (
      <div>
        <FlexRow
          style={styleMin}
          items={[
            <FlexColumn
              key="west-col"
              items={[
                <TextInput
                  key="west"
                  id="west"
                  name="West"
                  placeholder={boundsPlaceholder.min.x.toString()}
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
                  key="south"
                  id="south"
                  name="South"
                  placeholder={boundsPlaceholder.min.y.toString()}
                  size={inputSize}
                  label="South"
                />,
              ]}
            />,
          ]}
        />
        <FlexRow
          style={styleMax}
          items={[
            <FlexColumn
              key="east-col"
              items={[
                <TextInput
                  key="east"
                  id="east"
                  name="East"
                  placeholder={boundsPlaceholder.max.x.toString()}
                  size={inputSize}
                  label="East"
                />,
              ]}
            />,
            <FlexColumn
              key="north-col"
              items={[
                <TextInput
                  key="north"
                  id="north"
                  name="North"
                  placeholder={boundsPlaceholder.max.y.toString()}
                  size={inputSize}
                  label="North"
                />,
              ]}
            />,
          ]}
        />
      </div>
    )
  }
}
