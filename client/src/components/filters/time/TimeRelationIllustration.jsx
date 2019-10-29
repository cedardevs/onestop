import React, {useState} from 'react'

import _ from 'lodash'

import defaultStyles from '../../../style/defaultStyles'
import FlexColumn from '../../common/ui/FlexColumn'
import FlexRow from '../../common/ui/FlexRow'
import {consolidateStyles} from '../../../utils/styleUtils'

import {styleRelationIllustration} from '../common/styleFilters'

const QUERY = [ {start: 2, end: 6}, {start: 2}, {end: 6} ]

const RESULTS = [
  {
    start: 0,
    end: 1,
    description: [
      'result range ends before query begins, with no overlap',
      'result range ends before query begins, with no overlap',
      'result range ends before query ends',
    ],
    relation: [
      {contains: false, within: false, intersects: false, disjoint: true},
      {contains: false, within: false, intersects: false, disjoint: true},
      {contains: false, within: true, intersects: true, disjoint: false},
    ],
  },
  {
    start: 3,
    end: 5,
    description: [
      'result range is smaller than query, with complete overlap (result is a subset)',
      'result range is smaller than query, with complete overlap (result is a subset)',
      'result range ends before query ends',
    ],
    relation: [
      {contains: false, within: true, intersects: true, disjoint: false},
      {contains: false, within: true, intersects: true, disjoint: false},
      {contains: false, within: true, intersects: true, disjoint: false},
    ],
  },
  {
    start: 1,
    end: 7,
    description: [
      'result range is larger than query, with complete overlap (result is a superset)',
      'result range starts before query, with significant overlap',
      'result range ends before query, with significant overlap',
    ],
    relation: [
      {contains: true, within: false, intersects: true, disjoint: false},
      {contains: false, within: false, intersects: true, disjoint: false},
      {contains: false, within: false, intersects: true, disjoint: false},
    ],
  },
  {
    start: 4,
    description: [
      'result range starts in middle of query range, and continues into present',
      'result range starts in middle of query range, and continues into present',
      'result range starts in middle of query range, and continues into present',
    ],
    relation: [
      {contains: false, within: false, intersects: true, disjoint: false},
      {contains: false, within: true, intersects: true, disjoint: false},
      {contains: false, within: false, intersects: true, disjoint: false},
    ],
  },
  {
    start: 1,
    description: [
      'result range starts before query, and continues into present',
      'result range starts before query, and continues into present',
      'result range starts in middle of query range, and continues into present, past query end',
    ],
    relation: [
      {contains: true, within: false, intersects: true, disjoint: false},
      {contains: true, within: false, intersects: true, disjoint: false},
      {contains: false, within: false, intersects: true, disjoint: false},
    ],
  },
]

const Spacer = ({style, children}) => {
  const SPACER_HEIGHT = '0.309em'
  const styleSpacer = {height: SPACER_HEIGHT}

  return <div style={consolidateStyles(styleSpacer, style)}>{children}</div>
}

const TimeLineQuery = ({query, labels, outputs}) => {
  const timelineStartLabel = (
    <div
      key="inf"
      style={{
        paddingLeft: '0.309em',
      }}
      aria-label="negative infinity, start scale of timeline"
    >
      -∞
    </div>
  )
  const timelineEndLabel = (
    <div
      key="present"
      style={{
        paddingRight: '0.309em',
      }}
      aria-label="present, end scale of timeline"
    >
      present
    </div>
  )
  const timeline = (
    <div
      key="timeline"
      style={{
        width: '100%',
      }}
    >
      <div
        key="legend"
        style={{
          width: '100%',
          borderLeft: `2px solid ${styleRelationIllustration.query
            .borderColor}`,
          borderRight: `2px solid ${styleRelationIllustration.query
            .borderColor}`,
        }}
      >
        <FlexRow
          style={{justifyContent: 'space-between'}}
          items={[ timelineStartLabel, timelineEndLabel ]}
        />
      </div>
      <div
        key="timeline"
        style={{
          width: '100%',
          borderTop: `2px solid ${styleRelationIllustration.query.borderColor}`,
          borderLeft: `2px solid ${styleRelationIllustration.query
            .borderColor}`,
          borderRight: `2px solid ${styleRelationIllustration.query
            .borderColor}`,
        }}
        aria-hidden={true}
      >
        <span aria-hidden={true}>&nbsp;</span>
      </div>
    </div>
  )

  let labelColumn = [
    <div key="spacer1">
      <span aria-hidden={true}>&nbsp;</span>
    </div>,
    <div key="spacer2">
      <span aria-hidden={true}>&nbsp;</span>
    </div>,
    <Spacer key="spacer3" />,
  ]
  labels.forEach(label => {
    labelColumn.push(label)
  })

  let endLabelColumn = [ timelineEndLabel ]

  const leftColumn = (
    <FlexColumn
      key="left"
      style={{
        alignItems: 'flex-end',
        justifyContent: 'space-evenly',
      }}
      items={labelColumn}
    />
  )

  const queryBox = (
    <Spacer key="queryrange" style={{width: '100%'}}>
      <output
        style={{
          position: 'absolute',
          left: leftEdgeOfRange(query.start),
          width: width(query.start, query.end),
          height: '85%',
          bottom: 0,
          borderLeft: queryRangeBorder(query.start),
          borderRight: queryRangeBorder(query.end),
          backgroundColor: styleRelationIllustration.query.backgroundColor,
        }}
        title="user defined time filter"
      >
        <label
          style={{
            position: 'absolute',
            right: '0.5em',
            color: styleRelationIllustration.query.color,
          }}
        >
          filter
        </label>
      </output>
      <span aria-hidden={true}>&nbsp;</span>
    </Spacer>
  )

  let exampleColumn = [ timeline, queryBox ]
  outputs.forEach(output => {
    exampleColumn.push(output)
  })

  const middle = (
    <FlexColumn
      key="middle"
      style={{
        flexGrow: 1,
        position: 'relative',
        justifyContent: 'space-evenly',
      }}
      items={exampleColumn}
    />
  )

  return (
    <div>
      <div style={{textAlign: 'center'}}>timeline:</div> {middle}
    </div>
  )
}

const colorRelation = (isMatched, isBorder) => {
  if (isMatched) {
    return isBorder
      ? styleRelationIllustration.included.borderColor
      : styleRelationIllustration.included.backgroundColor
  }
  else {
    return isBorder
      ? styleRelationIllustration.excluded.borderColor
      : styleRelationIllustration.excluded.backgroundColor
  }
}

const queryRangeBorder = (offset, isMatched) => {
  let style = offset == null ? 'dashed' : 'solid'
  let color = styleRelationIllustration.query.borderColor
  if (isMatched != null) {
    color = isMatched
      ? styleRelationIllustration.included.borderColor
      : styleRelationIllustration.excluded.borderColor
  }
  return `1px ${style} ${color}`
}

const leftEdgeOfRange = offset => {
  return `${10 * ((offset == null ? -0.5 : offset) + 0.5)}%`
}
const rightEdgeOfRange = offset => {
  return `${10 * (9 - (offset == null ? 9 : offset))}%`
}
const width = (left, right) => {
  const leftOffset = 10 * ((left == null ? -0.5 : left) + 0.5)
  const rightOffset = 10 * (9 - (right == null ? 9 : right))
  return `${100 - rightOffset - leftOffset}%`
}

const TimeLineResult = ({id, label, result, relation, queryType}) => {
  let isOngoing = result.end == null
  let includedBasedOnRelationship = result.relation[queryType][relation]
  let description = `${includedBasedOnRelationship
    ? 'Included in search results:'
    : 'NOT included in search results:'} ${result.description[queryType]}`

  // const continuation = isOngoing ? ( // TODO try arrow to indicate ongoing
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
        marginLeft: leftEdgeOfRange(result.start),
        marginRight: rightEdgeOfRange(result.end),
        borderRadius: isOngoing ? '.2em 0 0 .2em' : '.2em',
        borderStyle: 'solid',
        borderWidth: '1px',
        borderColor: colorRelation(includedBasedOnRelationship, true),
        backgroundColor: colorRelation(includedBasedOnRelationship),
        borderLeft: queryRangeBorder(result.start, includedBasedOnRelationship),
        borderRight: queryRangeBorder(result.end, includedBasedOnRelationship),
        marginBottom: '0.309em',
        position: 'relative',
        overflow: 'visible',
        boxShadow: '2px 2px 5px 2px #2c2c2c59',
      }}
    >
      <label
        style={{
          width: '100%',
          color: includedBasedOnRelationship
            ? styleRelationIllustration.included.color
            : styleRelationIllustration.excluded.color, //'inherit' : '#FFF',

          textAlign: 'center',
          display: 'inline-block',
        }}
      >
        {label}
      </label>
      <div style={defaultStyles.hideOffscreen}>{description}</div>
    </output>
  )
}

const TimeRelationIllustration = ({relation, hasStart, hasEnd}) => {
  let currentQueryType = 0
  if (hasStart && !hasEnd) currentQueryType = 1
  if (!hasStart && hasEnd) currentQueryType = 2

  const labels = _.map(RESULTS, (result, index) => {
    return (
      <label
        key={`result${index + 1}`}
        htmlFor={`result${index + 1}`}
        style={{marginBottom: '0.309em', marginRight: '0.309em'}}
      >
        ex {index + 1} :
      </label>
    )
  })

  const outputs = _.map(RESULTS, (result, index) => {
    return (
      <TimeLineResult
        key={`result${index + 1}`}
        id={`result${index + 1}`}
        label={`ex ${index + 1}`}
        result={result}
        relation={relation}
        queryType={currentQueryType}
      />
    )
  })

  return (
    <TimeLineQuery
      query={QUERY[currentQueryType]}
      labels={labels}
      outputs={outputs}
    />
  )
}
export default TimeRelationIllustration
