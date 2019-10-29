import React, {useState} from 'react'

import _ from 'lodash'

import defaultStyles from '../../../style/defaultStyles'
import FlexColumn from '../../common/ui/FlexColumn'
import FlexRow from '../../common/ui/FlexRow'
import {consolidateStyles} from '../../../utils/styleUtils'

import {styleRelationIllustration} from '../common/styleFilters'
import {arrow_right, SvgIcon} from '../../common/SvgIcon'

const QUERY = [ {start: 2, end: 6}, {start: 2}, {end: 6} ]

const resultDescription = (include, description) => {
  return `${include
    ? 'Included in search results:'
    : 'NOT included in search results:'} ${description}`
}

const resultStyles = {
  color: include =>
    include
      ? styleRelationIllustration.included.color
      : styleRelationIllustration.excluded.color,
  backgroundColor: include =>
    include
      ? styleRelationIllustration.included.backgroundColor
      : styleRelationIllustration.excluded.backgroundColor,
  borderColor: include =>
    include
      ? styleRelationIllustration.included.borderColor
      : styleRelationIllustration.excluded.borderColor,
}

const RESULTS = [
  {
    start: 0,
    end: 1,
    description: (include, queryIndex) => {
      let description = [
        'result range ends before query begins, with no overlap',
        'result range ends before query begins, with no overlap',
        'result range ends before query ends',
      ]
      return resultDescription(include, description[queryIndex])
    },
    styles: resultStyles,
    relation: [
      {contains: false, within: false, intersects: false, disjoint: true},
      {contains: false, within: false, intersects: false, disjoint: true},
      {contains: false, within: true, intersects: true, disjoint: false},
    ],
  },
  {
    start: 3,
    end: 5,
    description: (include, queryIndex) => {
      let description = [
        'result range is smaller than query, with complete overlap (result is a subset)',
        'result range is smaller than query, with complete overlap (result is a subset)',
        'result range ends before query ends',
      ]
      return resultDescription(include, description[queryIndex])
    },
    styles: resultStyles,
    relation: [
      {contains: false, within: true, intersects: true, disjoint: false},
      {contains: false, within: true, intersects: true, disjoint: false},
      {contains: false, within: true, intersects: true, disjoint: false},
    ],
  },
  {
    start: 1,
    end: 7,
    description: (include, queryIndex) => {
      let description = [
        'result range is larger than query, with complete overlap (result is a superset)',
        'result range starts before query, with significant overlap',
        'result range ends before query, with significant overlap',
      ]
      return resultDescription(include, description[queryIndex])
    },
    styles: resultStyles,
    relation: [
      {contains: true, within: false, intersects: true, disjoint: false},
      {contains: false, within: false, intersects: true, disjoint: false},
      {contains: false, within: false, intersects: true, disjoint: false},
    ],
  },
  {
    start: 4,
    description: (include, queryIndex) => {
      let description = [
        'result range starts in middle of query range, and continues into present',
        'result range starts in middle of query range, and continues into present',
        'result range starts in middle of query range, and continues into present',
      ]
      return resultDescription(include, description[queryIndex])
    },
    styles: resultStyles,
    relation: [
      {contains: false, within: false, intersects: true, disjoint: false},
      {contains: false, within: true, intersects: true, disjoint: false},
      {contains: false, within: false, intersects: true, disjoint: false},
    ],
  },
  {
    start: 1,
    description: (include, queryIndex) => {
      let description = [
        'result range starts before query, and continues into present',
        'result range starts before query, and continues into present',
        'result range starts in middle of query range, and continues into present, past query end',
      ]
      return resultDescription(include, description[queryIndex])
    },
    styles: resultStyles,
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

const TimeLineQuery = ({query, outputs}) => {
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

  let endLabelColumn = [ timelineEndLabel ]

  // const leftColumn = (
  //   <FlexColumn
  //     key="left"
  //     style={{
  //       alignItems: 'flex-end',
  //       justifyContent: 'space-evenly',
  //     }}
  //     items={labelColumn}
  //   />
  // )

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
    <div style={{marginTop: '.609em'}}>
      <div style={{textAlign: 'center'}}>timeline:</div> {middle}
    </div>
  )
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

const styleResult = {
  cursor: 'pointer',
  display: 'block',
  // position: 'relative',
  borderRadius: '.2em',
  borderStyle: 'solid',
  borderWidth: '1px',
  marginBottom: '0.309em',
  overflow: 'visible',
  boxShadow: '2px 2px 5px 2px #2c2c2c59', // TODO check in other browers and maybe move to styleRelationIllustration?
}

const stylePosition = ({start, end}) => {
  return {
    marginLeft: leftEdgeOfRange(start),
    marginRight: rightEdgeOfRange(end),
  }
}

const TimeLineResult = ({id, label, result, relation, queryType}) => {
  let isOngoing = result.end == null
  let includedBasedOnRelationship = result.relation[queryType][relation]

  let description = result.description(includedBasedOnRelationship, queryType)

  const styleColors = {
    // borderColor: result.styles.borderColor(includedBasedOnRelationship),
    backgroundColor: result.styles.backgroundColor(includedBasedOnRelationship),
  }

  const continuation = isOngoing ? ( // TODO retest accessibility!
    <div
      style={consolidateStyles(styleColors, {
        backgroundColor: 'none',
        fill: result.styles.color(includedBasedOnRelationship),
        backgroundImage: `linear-gradient(to right, ${result.styles.backgroundColor(
          includedBasedOnRelationship
        )} , ${includedBasedOnRelationship? '#cbeed5': '#6b8c73'})`,
        // borderRadius: '0 .2em .2em 0',
        // borderStyle: 'solid',
        // borderWidth: '1px',
        // borderLeft: 0,
        // borderRight: queryRangeBorder(result.end, includedBasedOnRelationship),
      })}
    ><SvgIcon key="hackFlexRow" wrapperStyle={{display: 'inline'}} path={arrow_right} size=".5em" /></div>
) : null // TODO svg hovering doesn't show text now...

  const styleBorders = {
    // borderRadius: isOngoing ? '.2em 0 0 .2em' : '.2em',
    borderLeft: queryRangeBorder(result.start, includedBasedOnRelationship),
    borderRight: queryRangeBorder(result.end, includedBasedOnRelationship), //0
    borderColor: result.styles.borderColor(includedBasedOnRelationship),
  }

  const styleLabel = {
    color: result.styles.color(includedBasedOnRelationship),
    width: '100%',
    textAlign: 'center',
    display: 'inline-block',
  }

  // previous layout... (no arrow)
  // return (
  //   <output
  //     id={id}
  //     title={description}
  //     style={consolidateStyles(
  //       styleResult,
  //       stylePosition(result),
  //       styleBorders,
  //       styleColors
  //     )}
  //   >
  //     <FlexRow items={[<label key="label" style={styleLabel}>{label}</label>, continuation]} />
  //     <div style={defaultStyles.hideOffscreen}>{description}</div>
  //   </output>
  // )

  return (
    <div
      style={consolidateStyles(
        {
          cursor: 'pointer', // TODO redefine styles
          display: 'block',
          position: 'relative',
          marginBottom: '0.309em',
          overflow: 'visible',
          borderRadius: '.2em',
          borderStyle: 'solid',
          borderWidth: '1px',
          boxShadow: '2px 2px 5px 2px #2c2c2c59',
        },
        stylePosition(result),
        styleBorders
      )}
      title={description}
    >
      <FlexRow
        items={[
          <output
            id={id}
            key="output"
            title={description}
            style={consolidateStyles(
              // stylePosition(result),
              {flexGrow: 1},
              // styleBorders,
              styleColors
            )}
          >
            <label key="label" style={styleLabel}>
              {label}
            </label>
            <div style={defaultStyles.hideOffscreen}>{description}</div>
          </output>,
          continuation,
        ]}
      />
    </div>
  )
}

const TimeRelationIllustration = ({relation, hasStart, hasEnd}) => {
  let currentQueryType = 0
  if (hasStart && !hasEnd) currentQueryType = 1
  if (!hasStart && hasEnd) currentQueryType = 2

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

  return <TimeLineQuery query={QUERY[currentQueryType]} outputs={outputs} />
}
export default TimeRelationIllustration
