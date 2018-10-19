import '../specHelper'
import {expect, assert} from 'chai'
import * as protocolUtils from '../../src/utils/resultUtils'
import _ from 'lodash'

describe('The resultUtils', function(){
  it('has distinct id and color combinations for each protocol', function(){
    const protocolIdCombos = _.map(protocolUtils.protocols, p => {
      return `${p.id}-${p.color}`
    })
    assert.deepEqual(
      protocolIdCombos,
      _.uniq(protocolIdCombos),
      `${protocolIdCombos} vs ${_.uniq(protocolIdCombos)}`
    )
  })

  it('can identify ogc:wcs', function(){
    const testCases = [
      {linkProtocol: 'ogc:wcs'},
      {linkProtocol: 'OGC:wcs'},
      {linkProtocol: 'ogc:WCS'},
      {linkProtocol: 'OGC:WCS'},
    ]

    _.each(testCases, p => {
      const protocol = protocolUtils.identifyProtocol(p)
      protocol.id.should.equal('C')
      protocol.label.should.equal('OGC Web Coverage Service')
      protocol.color.should.equal('#ab4e2c')
    })
  })

  it('can identify download links', function(){
    const testCases = [
      {linkProtocol: 'download'},
      {linkProtocol: 'Download'},
      {linkProtocol: 'DOWNLOAD'},
    ]

    _.each(testCases, p => {
      const protocol = protocolUtils.identifyProtocol(p)
      protocol.id.should.equal('D')
      protocol.label.should.equal('Download')
      protocol.color.should.equal('blue')
    })
  })

  it('can identify FTP links', function(){
    const testCases = [ {linkProtocol: 'FTP'}, {linkProtocol: 'ftp'} ]

    _.each(testCases, p => {
      const protocol = protocolUtils.identifyProtocol(p)
      protocol.id.should.equal('F')
      protocol.label.should.equal('FTP')
      protocol.color.should.equal('#c50000')
    })
  })

  it('can identify http links', function(){
    const testCases = [
      {linkProtocol: 'HTTP'},
      {linkProtocol: 'HTTPS'},
      {linkProtocol: 'http'},
      {linkProtocol: 'https'},
    ]

    _.each(testCases, p => {
      const protocol = protocolUtils.identifyProtocol(p)
      protocol.id.should.equal('H')
      protocol.label.should.equal('HTTP/HTTPS')
      protocol.color.should.equal('purple')
    })
  })

  it('can identify noaa:las', function(){
    const testCases = [ {linkProtocol: 'noaa:las'}, {linkProtocol: 'NOAA:LAS'} ]

    _.each(testCases, p => {
      const protocol = protocolUtils.identifyProtocol(p)
      protocol.id.should.equal('L')
      protocol.label.should.equal('NOAA Live Access Server')
      protocol.color.should.equal('#008484')
    })
  })

  it('can identify ogc:wms', function(){
    const testCases = [ {linkProtocol: 'ogc:wms'}, {linkProtocol: 'OGC:WMS'} ]

    _.each(testCases, p => {
      const protocol = protocolUtils.identifyProtocol(p)
      protocol.id.should.equal('M')
      protocol.label.should.equal('OGC Web Map Service')
      protocol.color.should.equal('#92631c')
    })
  })

  it('can identify opendap', function(){
    const testCases = [
      {linkProtocol: 'opendap'},
      {linkProtocol: 'OPENDAP'},
      {linkProtocol: 'OPeNDAP'},
      {linkProtocol: 'OPeNDAP:OPeNDAP'},
      {linkProtocol: 'OPeNDAP:Hyrax'},
    ]

    _.each(testCases, p => {
      const protocol = protocolUtils.identifyProtocol(p)
      protocol.id.should.equal('O')
      protocol.label.should.equal('OPeNDAP')
      protocol.color.should.equal('green')
    })
  })

  it('can identify thredds', function(){
    const testCases = [
      {linkProtocol: 'thredds'},
      {linkProtocol: 'THREDDS'},
      {linkProtocol: 'UNIDATA:THREDDS'},
    ]

    _.each(testCases, p => {
      const protocol = protocolUtils.identifyProtocol(p)
      protocol.id.should.equal('T')
      protocol.label.should.equal('THREDDS')
      protocol.color.should.equal('#616161')
    })
  })

  it('can identify empty protocols', function(){
    const testCases = [ {linkProtocol: ''} ]

    _.each(testCases, p => {
      const protocol = protocolUtils.identifyProtocol(p)
      protocol.id.should.equal('W')
      protocol.label.should.equal('Web')
      protocol.color.should.equal('#a26a03')
    })
  })

  it('cannot default unknown protocols', function(){
    const testCases = [
      {linkProtocol: 'nonsense'},
      {linkProtocol: ' '},
      {linkProtocol: 'download typo'},
      {linkProtocol: 'etc'},
    ]

    _.each(testCases, p => {
      const protocol = protocolUtils.identifyProtocol(p)
      protocol.id.should.equal('?')
      protocol.label.should.equal('Unknown')
      protocol.color.should.equal('black')
    })
  })
})
