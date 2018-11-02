import * as protocolUtils from '../../src/utils/resultUtils'
import _ from 'lodash'

describe('The resultUtils', function () {

  it('has distinct id and color combinations for each protocol', function() {
    const protocolIdCombos = _.map(protocolUtils.protocols, p => {
      return `${p.id}-${p.color}`
    })
    expect(protocolIdCombos).toEqual(_.uniq(protocolIdCombos))
  })

  describe('can identify ogc:wcs', function () {
    const testCases = [
      {linkProtocol: 'ogc:wcs'},
      {linkProtocol: 'OGC:wcs'},
      {linkProtocol: 'ogc:WCS'},
      {linkProtocol: 'OGC:WCS'},
    ]

    _.each(testCases, (p) => {
      it(`for input ${p.linkProtocol}`, function() {
        const protocol = protocolUtils.identifyProtocol(p)
        expect(protocol.id).toBe('C')
        expect(protocol.label).toBe('OGC Web Coverage Service')
        expect(protocol.color).toBe('#ab4e2c')
      })
    })
  })

  describe('can identify download links', function () {
    const testCases = [
      {linkProtocol: 'download'},
      {linkProtocol: 'Download'},
      {linkProtocol: 'DOWNLOAD'},
    ]

    _.each(testCases, (p) => {
      it(`for input ${p.linkProtocol}`, function() {
        const protocol = protocolUtils.identifyProtocol(p)
        expect(protocol.id).toBe('D')
        expect(protocol.label).toBe('Download')
        expect(protocol.color).toBe('blue')
      })
    })
  })

  describe('can identify FTP links', function () {
    const testCases = [
      {linkProtocol: 'FTP'},
      {linkProtocol: 'ftp'},
    ]

    _.each(testCases, (p) => {
      it(`for input ${p.linkProtocol}`, function() {
        const protocol = protocolUtils.identifyProtocol(p)
        expect(protocol.id).toBe('F')
        expect(protocol.label).toBe('FTP')
        expect(protocol.color).toBe('#c50000')
      })
    })
  })

  describe('can identify http links', function () {
    const testCases = [
      {linkProtocol: 'HTTP'},
      {linkProtocol: 'HTTPS'},
      {linkProtocol: 'http'},
      {linkProtocol: 'https'},
    ]

    _.each(testCases, (p) => {
      it(`for input ${p.linkProtocol}`, function() {
        const protocol = protocolUtils.identifyProtocol(p)
        expect(protocol.id).toBe('H')
        expect(protocol.label).toBe('HTTP/HTTPS')
        expect(protocol.color).toBe('purple')
      })
    })
  })

  describe('can identify noaa:las', function () {
    const testCases = [
      {linkProtocol: 'noaa:las'},
      {linkProtocol: 'NOAA:LAS'},
    ]

    _.each(testCases, (p) => {
      it(`for input ${p.linkProtocol}`, function() {
        const protocol = protocolUtils.identifyProtocol(p)
        expect(protocol.id).toBe('L')
        expect(protocol.label).toBe('NOAA Live Access Server')
        expect(protocol.color).toBe('#008484')
      })
    })
  })

  describe('can identify ogc:wms', function () {
    const testCases = [
      {linkProtocol: 'ogc:wms'},
      {linkProtocol: 'OGC:WMS'},
    ]

    _.each(testCases, (p) => {
      it(`for input ${p.linkProtocol}`, function() {
        const protocol = protocolUtils.identifyProtocol(p)
        expect(protocol.id).toBe('M')
        expect(protocol.label).toBe('OGC Web Map Service')
        expect(protocol.color).toBe('#92631c')
      })
    })
  })

  describe('can identify opendap', function () {
    const testCases = [
      {linkProtocol: 'opendap'},
      {linkProtocol: 'OPENDAP'},
      {linkProtocol: 'OPeNDAP'},
      {linkProtocol: 'OPeNDAP:OPeNDAP'},
      {linkProtocol: 'OPeNDAP:Hyrax'},
    ]

    _.each(testCases, (p) => {
      it(`for input ${p.linkProtocol}`, function() {
        const protocol = protocolUtils.identifyProtocol(p)
        expect(protocol.id).toBe('O')
        expect(protocol.label).toBe('OPeNDAP')
        expect(protocol.color).toBe('green')
      })
    })
  })

  describe('can identify thredds', function () {
    const testCases = [
      {linkProtocol: 'thredds'},
      {linkProtocol: 'THREDDS'},
      {linkProtocol: 'UNIDATA:THREDDS'},
    ]

    _.each(testCases, (p) => {
      it(`for input ${p.linkProtocol}`, function() {
        const protocol = protocolUtils.identifyProtocol(p)
        expect(protocol.id).toBe('T')
        expect(protocol.label).toBe('THREDDS')
        expect(protocol.color).toBe('#616161')
      })
    })
  })

  describe('can identify empty protocols', function () {
    const testCases = [
      {linkProtocol: ''},
    ]

    _.each(testCases, (p) => {
      it(`for input ${p.linkProtocol}`, function() {
        const protocol = protocolUtils.identifyProtocol(p)
        expect(protocol.id).toBe('W')
        expect(protocol.label).toBe('Web')
        expect(protocol.color).toBe('#a26a03')
      })
    })
  })

  describe('cannot default unknown protocols', function () {
    const testCases = [
      {linkProtocol: 'nonsense'},
      {linkProtocol: ' '},
      {linkProtocol: 'download typo'},
      {linkProtocol: 'etc'},
    ]

    _.each(testCases, (p) => {
      it(`for input ${p.linkProtocol}`, function() {
        const protocol = protocolUtils.identifyProtocol(p)
        expect(protocol.id).toBe('?')
        expect(protocol.label).toBe('Unknown')
        expect(protocol.color).toBe('black')
      })
    })
  })



})
