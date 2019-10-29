import React, {useState} from 'react'

import _ from 'lodash'

import defaultStyles from '../../../style/defaultStyles'
import FlexColumn from '../../common/ui/FlexColumn'
import FlexRow from '../../common/ui/FlexRow'
import {consolidateStyles} from '../../../utils/styleUtils'

const BOXES = [
  {
    // global
    label: 'global',
    top: 0,
    height: 9, // height: 100%,
    left: 0,
    width: 14, // width: 100%,
    description: [ 'global result', 'global result is always excluded' ],
    relation: [
      {contains: true, within: false, intersects: true, disjoint: false},
      {contains: false, within: false, intersects: false, disjoint: false}, // excludeGlobal
    ],
  },
  {
    // contains
    label: 'ex 1',
    top: 0,
    height: 9,
    left: 0,
    width: 10, // TODO or 11?
    description: [
      'result is larger than query, with complete overlap (result is a superset)',
    ],
    relation: [
      {contains: true, within: false, intersects: true, disjoint: false},
    ],
  },
  {
    // query
    label: 'query',
    query: true,
    top: 1,
    height: 7,
    left: 1,
    width: 5,
    description: [
      'user defined query', // TODO nonsense
    ],
    relation: [
      {contains: true, within: false, intersects: true, disjoint: false}, // TODO nonsense
    ],
  },
  {
    // within
    label: 'ex 2',
    top: 2,
    height: 2,
    left: 2,
    width: 2,
    description: [
      'result is smaller than query, with complete overlap (result is a subset)',
    ],
    relation: [
      {contains: false, within: true, intersects: true, disjoint: false},
    ],
  },
  {
    // disjoint
    label: 'ex 3',
    top: 1,
    height: 2,
    left: 7,
    width: 2,
    description: [ 'result is outside query, with no overlap' ],
    relation: [
      {contains: false, within: false, intersects: false, disjoint: true},
    ],
  },
  {
    // intersects
    label: 'ex 4',
    top: 4,
    height: 2,
    left: 4,
    width: 2,
    description: [ 'result partially overlaps query' ],
    relation: [
      {contains: false, within: false, intersects: true, disjoint: false},
    ],
  },
]

// const Spacer = ({style, children}) => {
//   const SPACER_HEIGHT = '0.309em'
//   const styleSpacer = {height: SPACER_HEIGHT}
//
//   return <div style={consolidateStyles(styleSpacer, style)}>{children}</div>
// }

const TimeLineQuery = ({query, labels, outputs}) => {
  // const timelineStartLabel = (
  //   <div
  //     key="inf"
  //     style={{
  //       paddingLeft: '0.309em',
  //       // borderLeft: '2px solid blue'
  //     }}
  //     aria-label="negative infinity, start scale of timeline"
  //   >
  //     -∞
  //   </div>
  // )
  // const timelineEndLabel = (
  //   <div
  //     key="present"
  //     style={{
  //       paddingRight: '0.309em',
  //       // borderRight: '2px solid blue',
  //       // position: 'relative',
  //     }}
  //     aria-label="present, end scale of timeline"
  //   >
  //     present
  //   </div>
  // )
  // const timeline = (
  //   <div
  //     key="timeline"
  //     style={{
  //       width: '100%',
  //     }}
  //   >
  //     // <div
  //     //   key="legend"
  //     //   style={{
  //     //     width: '100%',
  //     //     borderLeft: `2px solid ${COLORS.query.borderColor}`, //'2px solid blue',
  //     //     borderRight: `2px solid ${COLORS.query.borderColor}`, //'2px solid blue',
  //     //   }}
  //     // >
  //     //   <FlexRow
  //     //     style={{justifyContent: 'space-between'}}
  //     //     items={[ timelineStartLabel, timelineEndLabel ]}
  //     //   />
  //     // </div>
  //     <div
  //       key="timeline"
  //       style={{
  //         width: '100%',
  //         // marginTop: 'auto',
  //         // marginBottom: 'auto',
  //         // height: '0px', // fix for IE
  //         // borderStyle: 'solid',
  //         // borderColor: 'blue',
  //         borderTop: `2px solid ${COLORS.query.borderColor}`, //'2px solid blue',
  //         borderLeft: `2px solid ${COLORS.query.borderColor}`, //'2px solid blue',
  //         borderRight: `2px solid ${COLORS.query.borderColor}`, //'2px solid blue',
  //       }}
  //       aria-hidden={true}
  //     >
  //       <span aria-hidden={true}>&nbsp;</span>
  //     </div>
  //   </div>
  // )

  // let labelColumn = [ <Spacer key="spacer1" />, <Spacer key="spacer2" /> ]
  // let labelColumn = [
  //   <div key="spacer1">
  //     <span aria-hidden={true}>&nbsp;</span>
  //   </div>,
  //   <div key="spacer2">
  //     <span aria-hidden={true}>&nbsp;</span>
  //   </div>,
  //   <Spacer key="spacer3" />,
  // ]
  // labels.forEach(label => {
  //   labelColumn.push(label)
  // })
  //
  // let endLabelColumn = [ timelineEndLabel ]

  // const leftColumn = (
  //   <FlexColumn
  //     key="left"
  //     style={{
  //       alignItems: 'flex-end',
  //       // position: 'relative',
  //       justifyContent: 'space-evenly',
  //     }}
  //     items={labelColumn}
  //   />
  // )

  const queryBox = (
    <div
      key="queryrange"
      style={{
        width: '100%',
        height: '7em', // TODO picked an arbitrary total height for now...
        position: 'relative',
      }}
    >
      <output
        style={{
          position: 'absolute',
          left: leftEdgeOfRange(query.left), //TODO rename to leftEdge
          width: width(query.width),
          height: height(query.height),
          top: topEdge(query.top),
          border: queryRangeBorder(), // TODO what param to distinguish query from results?
          backgroundColor: COLORS.query.backgroundColor, // TODO what param to distinguish query from results?
          // TODO (below) title from query (also rename query arg to box arg or something?)
          // TODO (below) dynamic label from box too
        }}
        title="user defined time filter"
      >
        <label
          style={{position: 'absolute', right: '0.25em', bottom: '0.25em'}}
        >
          ex #
        </label>
      </output>
    </div>
  )

  // let exampleColumn = [ timeline, queryBox ]
  // outputs.forEach(output => {
  //   exampleColumn.push(output)
  // })
  // exampleColumn.push(<Spacer key="spacer1" />)
  // exampleColumn.push(<Spacer key="spacer2" />)

  // const middle = (
  //   <FlexColumn
  //     key="middle"
  //     style={{
  //       flexGrow: 1,
  //       position: 'relative',
  //       justifyContent: 'space-evenly',
  //     }}
  //     items={exampleColumn}
  //   />
  // )

  // const rightColumn = <FlexColumn key="right" items={endLabelColumn} />

  // return (
  //   <div>
  //     <div>Timeline</div>
  //     <FlexRow items={[ leftColumn, middle ]} />
  //   </div>
  // )
  return queryBox
}

const COLORS = {
  included: {backgroundColor: '#86D29A', borderColor: '#56B770'}, // 359E51, 1D8739, 096B23
  excluded: {backgroundColor: '#4E5F53', borderColor: '#414642'}, // 363C38, 2B312D, 1F2420
  query: {backgroundColor: '#277cb2', borderColor: '#28323E'},
}

const colorRelation = (isMatched, isBorder) => {
  // if (isBorder) return isMatched ? '#4b966e' : '#52665b'
  // return isMatched ? '#78b494' : '#71867a'

  if (isMatched) {
    return isBorder
      ? COLORS.included.borderColor
      : COLORS.included.backgroundColor
  }
  else {
    return isBorder
      ? COLORS.excluded.borderColor
      : COLORS.excluded.backgroundColor
  }
}

const queryRangeBorder = (offset, isMatched) => {
  // if(isMatched != null) {
  //   return isMatched ? offset == null ? '1px dashed #0f5a32': '1px solid #0f5a32': offset == null ? '1px dashed #35443c': '1px solid #35443c'
  // }
  // return offset == null ? '1px dashed blue' : '1px solid blue'

  let style = offset == null ? 'dashed' : 'solid'
  let color = COLORS.query.borderColor
  if (isMatched != null) {
    color = isMatched
      ? COLORS.included.borderColor
      : COLORS.excluded.borderColor
  }
  return `1px ${style} ${color}`
}
const leftEdgeOfRange = offset => {
  return `${10 * ((offset == null ? 0 : offset) + 0)}%`
}
// const rightEdgeOfRange = offset => {
//   return `${10 * (9 - (offset == null ? 9 : offset))}%`
// }
const width = width => {
  return `${100 * (width / 14)}%`
}
const topEdge = offset => {
  return `${10 * ((offset == null ? 0 : offset) + 0)}%`
}
const height = height => {
  return `${100 * (height / 9)}%`
}

const TimeLineResult = ({id, label, result, relation, queryType}) => {
  let includedBasedOnRelationship = result.relation[queryType][relation]
  let description = `${includedBasedOnRelationship
    ? 'Included in search results:'
    : 'NOT included in search results:'} ${result.description[queryType]}`
  // let title = `${result.label} ${description}.`

  // const continuation = isOngoing ? (
  //   <div
  //     style={{
  //       position: 'absolute',
  //       height: '100%',
  //       borderRadius: '0px 1em 1em 0px',
  //       borderStyle: 'solid',
  //       borderWidth: '1px',
  //       borderColor: colorRelation(includedBasedOnRelationship, true),
  //       backgroundColor: colorRelation(includedBasedOnRelationship),
  //       borderLeft: '0',
  //       top: '-1px', // due to border width
  //       left: '100%',
  //       paddingRight: '.4em',
  //     }}
  //   >
  //     ...
  //   </div>
  // ) : null // TODO background-image: linear-gradient(to right, green , yellow) instead of bg color?

  return (
    <output
      id={id}
      title={description}
      style={{
        cursor: 'pointer',
        display: 'block',
        position: 'absolute',
        left: leftEdgeOfRange(result.left), //TODO rename to leftEdge
        width: width(result.width),
        height: height(result.height),
        top: topEdge(result.top),
        borderRadius: '.2em',
        borderStyle: 'solid',
        borderWidth: '1px',
        borderColor: result.query
          ? COLORS.query.borderColor
          : colorRelation(includedBasedOnRelationship, true),
        backgroundColor: result.query
          ? COLORS.query.backgroundColor
          : colorRelation(includedBasedOnRelationship),
        // border: queryRangeBorder(result.left, includedBasedOnRelationship), // TODO not really variable, except color
        overflow: 'visible',
        boxShadow: '2px 2px 5px 2px #2c2c2c59',
      }}
    >
      <label
        style={{
          color:
            includedBasedOnRelationship && !result.query ? 'inherit' : '#FFF',
          position: 'absolute',
          right: '0.25em',
          bottom: 0,
        }}
      >
        {label}
      </label>
      <div style={defaultStyles.hideOffscreen}>{description}</div>
    </output>
  )
}

const GeoRelationIllustration = ({relation, excludeGlobal}) => {
  let currentQueryType = 0
  // if (hasStart && !hasEnd) currentQueryType = 1
  // if (!hasStart && hasEnd) currentQueryType = 2

  // const labels = _.map(RESULTS, (result, index) => {
  //   return (
  //     <label
  //       key={`result${index + 1}`}
  //       htmlFor={`result${index + 1}`}
  //       style={{marginBottom: '0.309em', marginRight: '0.309em'}}
  //     >
  //       ex {index + 1} :
  //     </label>
  //   )
  // })
  const outputs = _.map(BOXES, (result, index) => {
    return (
      <TimeLineResult
        key={`result${index + 1}`}
        id={`result${index + 1}`}
        label={result.label}
        result={result}
        relation={relation}
        queryType={currentQueryType}
      />
    )
  })

  // <TimeLineQuery
  //   query={BOXES[0]}
  // />
  return (
    <div
      key="queryrange"
      style={{
        width: '100%',
        height: '7em', // TODO picked an arbitrary total height for now...
        position: 'relative',
        marginTop: '.609em',
      }}
    >
      {outputs}
    </div>
  )
}
export default GeoRelationIllustration
