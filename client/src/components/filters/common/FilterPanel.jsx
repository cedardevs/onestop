import React, {useState} from 'react'
import _ from 'lodash'

import {styleFilterPanel, styleFieldsetBorder} from './styleFilters'
import {consolidateStyles} from '../../../utils/styleUtils'

// const FilterPanel = ({style, children}) => {
//
//   return (
//     <div style={consolidateStyles(styleFilterPanel, style)}>
//       <fieldset
//         style={styleFieldsetBorder}
//       >
//         {children}
//       </fieldset>
//     </div>
//   )
// }
// export default FilterPanel
