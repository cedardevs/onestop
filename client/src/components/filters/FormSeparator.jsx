import React from 'react'

import FlexRow from '../common/ui/FlexColumn'

import {FilterColors} from '../../style/defaultStyles'

const styleSeparator = {
  display: 'flex',
  flexDirection: 'row',
  margin: '0.618em 0',
}

const styleLineSeparator = {
  flexGrow: 1,
  marginTop: 'auto',
  marginBottom: 'auto',
  height: '0px', // fix for IE
  borderStyle: 'dashed',
  borderColor: FilterColors.LIGHT_SHADOW,
}

const styleText = {margin: '0 0.613em'}

const FormSeparator = ({text}) => {
  return (
    <FlexRow
      style={styleSeparator}
      items={[
        <hr
          aria-hidden={true}
          key="SEPARATOR::BEFORE"
          style={styleLineSeparator}
        />,
        <div key="SEPARATOR::TEXT" style={styleText}>
          {text}
        </div>,
        <hr
          aria-hidden={true}
          key="SEPARATOR::AFTER"
          style={styleLineSeparator}
        />,
      ]}
    />
  )
}
export default FormSeparator
