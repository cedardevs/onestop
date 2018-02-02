import '../specHelper'
import { expect } from 'chai'
import * as protocolUtils from '../../src/utils/protocolUtils'
import _ from 'lodash'

describe('The protocolUtils', function () {

  it('can identify ogc:wcs', function () {
    const testCases = [
      {linkProtocol: 'ogc:wcs'},
      {linkProtocol: 'OGC:wcs'},
      {linkProtocol: 'ogc:WCS'},
      {linkProtocol: 'OGC:WCS'},
    ]

    _.each(testCases, (p) => {
      const protocol = protocolUtils.identifyProtocol(p)
      protocol.id.should.equal('C')
      protocol.label.should.equal('OGC Web Coverage Service')
      // TODO worth confirming color?
    })
  })

  it('can identify download links', function () {
    const testCases = [
      {linkProtocol: 'download'},
      {linkProtocol: 'Download'},
      {linkProtocol: 'DOWNLOAD'},
    ]

    _.each(testCases, (p) => {
      const protocol = protocolUtils.identifyProtocol(p)
      protocol.id.should.equal('D')
      protocol.label.should.equal('Download')
      // TODO worth confirming color?
    })
  })

  it('can identify FTP links', function () {
    const testCases = [
      {linkProtocol: 'FTP'},
      {linkProtocol: 'ftp'},
    ]

    _.each(testCases, (p) => {
      const protocol = protocolUtils.identifyProtocol(p)
      protocol.id.should.equal('F')
      protocol.label.should.equal('FTP')
      // TODO worth confirming color?
    })
  })

  it('can identify http links', function () {
    const testCases = [
      {linkProtocol: 'HTTP'},
      {linkProtocol: 'HTTPS'},
      {linkProtocol: 'http'},
      {linkProtocol: 'https'},
    ]

    _.each(testCases, (p) => {
      const protocol = protocolUtils.identifyProtocol(p)
      protocol.id.should.equal('H')
      protocol.label.should.equal('HTTP/HTTPS')
      // TODO worth confirming color?
    })
  })

  it('can identify noaa:las', function () {
    const testCases = [
      {linkProtocol: 'noaa:las'},
      {linkProtocol: 'NOAA:LAS'},
    ]

    _.each(testCases, (p) => {
      const protocol = protocolUtils.identifyProtocol(p)
      protocol.id.should.equal('L')
      protocol.label.should.equal('NOAA Live Access Server')
      // TODO worth confirming color?
    })
  })

  it('can identify ogc:wms', function () {
    const testCases = [
      {linkProtocol: 'ogc:wms'},
      {linkProtocol: 'OGC:WMS'},
    ]

    _.each(testCases, (p) => {
      const protocol = protocolUtils.identifyProtocol(p)
      protocol.id.should.equal('M')
      protocol.label.should.equal('OGC Web Map Service')
      // TODO worth confirming color?
    })
  })

  it('can identify opendap', function () {
    const testCases = [
      {linkProtocol: 'opendap'},
      {linkProtocol: 'OPENDAP'},
      {linkProtocol: 'OPeNDAP'},
    ]

    _.each(testCases, (p) => {
      const protocol = protocolUtils.identifyProtocol(p)
      protocol.id.should.equal('O')
      protocol.label.should.equal('OPeNDAP')
      // TODO worth confirming color?
    })
  })

  it('can identify thredds', function () {
    const testCases = [
      {linkProtocol: 'thredds'},
      {linkProtocol: 'THREDDS'},
    ]

    _.each(testCases, (p) => {
      const protocol = protocolUtils.identifyProtocol(p)
      protocol.id.should.equal('T')
      protocol.label.should.equal('THREDDS')
      // TODO worth confirming color?
    })
  })

  it('can identify empty protocols', function () {
    const testCases = [
      {linkProtocol: ''},
    ]

    _.each(testCases, (p) => {
      const protocol = protocolUtils.identifyProtocol(p)
      protocol.id.should.equal('W')
      protocol.label.should.equal('Web')
      // TODO worth confirming color?
    })
  })

  it('cannot default unknown protocols', function () {
    const testCases = [
      {linkProtocol: 'nonsense'},
      {linkProtocol: ' '},
      {linkProtocol: 'download typo'},
      {linkProtocol: 'etc'},
    ]

    _.each(testCases, (p) => {
      const protocol = protocolUtils.identifyProtocol(p)
      expect(protocol).to.be.undefined
    })
  })

})
