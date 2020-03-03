import React from 'react'
import {Container, Row, Col} from 'react-grid-system'

const styleContainer = {
  margin: 0,
  padding: 0,
  width: '100%',
}

const styleRow = first => {
  return {
    margin: 0,
    padding: 0,
    borderTop: first ? 'none' : '1px solid gray',
  }
}

export default class DetailGrid extends React.Component {
  render() {
    const {grid, colWidths} = this.props

    const gridRows = grid.map((row, rowIndex) => {
      const gridColumns = row.map((col, colIndex) => {
        const cw = colIndex in (colWidths || []) ? colWidths[colIndex] : null
        return (
          <Col key={colIndex} {...cw}>
            {col}
          </Col>
        )
      })
      return (
        <Row
          key={rowIndex}
          nogutter
          justify="end"
          style={styleRow(rowIndex === 0)}
        >
          {gridColumns}
        </Row>
      )
    })

    return (
      <Container fluid={true} style={styleContainer}>
        {gridRows}
      </Container>
    )
  }
}
