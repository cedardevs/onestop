import React from 'react'
import {SvgIcon} from '../components/common/SvgIcon'

export const renderBadgeIcon = protocol => {
  if (protocol.svgPath) {
    return (
      <SvgIcon
        wrapperStyle={{paddingBottom: '.23em'}}
        path={protocol.svgPath}
      />
    )
  }
  return <span>{protocol.id}</span>
}
