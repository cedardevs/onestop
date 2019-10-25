import React from 'react'

// import FlexRow from '../../common/ui/FlexRow'
import Button from '../../common/input/Button'

const styleButtonRow = {
  // TODO replace with flexrow
  display: 'flex',
  flexDirection: 'row',
  alignItems: 'center',
  justifyContent: 'center',
}

const styleButton = {
  width: '30.9%',
  padding: '0.309em',
  margin: '0 0.309em',
  fontSize: '1.05em',
}

const ApplyClearRow = ({ariaActionDescription, applyAction, clearAction}) => {
  return (
    <div style={styleButtonRow}>
      <Button
        key="filter::apply"
        text="Apply"
        title={`Apply ${ariaActionDescription}`}
        onClick={applyAction}
        style={styleButton}
      />
      <Button
        key="filter::clear"
        text="Clear"
        title={`Clear ${ariaActionDescription}`}
        onClick={clearAction}
        style={styleButton}
      />
    </div>
  )
}
export default ApplyClearRow
