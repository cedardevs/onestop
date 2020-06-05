package org.cedar.onestop.indexer.util

import org.cedar.schemas.analyze.Analyzers
import org.cedar.schemas.analyze.Temporal
import org.cedar.schemas.avro.psi.Analysis
import org.cedar.schemas.avro.psi.TemporalBoundingAnalysis
import org.cedar.schemas.avro.psi.ValidDescriptor
import org.cedar.schemas.avro.psi.Checksum
import org.cedar.schemas.avro.psi.ChecksumAlgorithm
import org.cedar.schemas.avro.psi.Discovery
import org.cedar.schemas.avro.psi.FileInformation
import org.cedar.schemas.avro.psi.ParsedRecord
import org.cedar.schemas.avro.psi.RecordType
import org.cedar.schemas.avro.psi.Relationship
import org.cedar.schemas.avro.psi.RelationshipType
import org.cedar.schemas.avro.psi.TemporalBounding
import java.time.temporal.ChronoUnit
import spock.lang.Specification
import spock.lang.Unroll

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.cedar.schemas.avro.util.AvroUtils

import static org.cedar.schemas.avro.util.TemporalTestData.getSituations

import org.cedar.onestop.kafka.common.util.DataUtils;

@Unroll
class TransformationUtilsSpec extends Specification {

  static Set<String> collectionFields = TestUtils.esConfig.indexedProperties(TestUtils.esConfig.COLLECTION_SEARCH_INDEX_ALIAS).keySet()
  static Set<String> granuleFields = TestUtils.esConfig.indexedProperties(TestUtils.esConfig.GRANULE_SEARCH_INDEX_ALIAS).keySet()
  static Set<String> granuleAnalysisErrorFields = TestUtils.esConfig.indexedProperties(TestUtils.esConfig.GRANULE_ERROR_AND_ANALYSIS_INDEX_ALIAS).keySet()

  static expectedKeywords = [
      "SIO > Super Important Organization",
      "OSIO > Other Super Important Organization",
      "SSIO > Super SIO (Super Important Organization)",
      "EARTH SCIENCE > OCEANS > OCEAN TEMPERATURE > SEA SURFACE TEMPERATURE",
      "Atmosphere > Atmospheric Temperature > Surface Temperature > Dew Point Temperature",
      "Oceans > Salinity/Density > Salinity",
      "Volcanoes > This Keyword > Is Invalid",
      "Spectral/Engineering > Microwave > Brightness Temperature",
      "Spectral/Engineering > Microwave > Temperature Anomalies",
      "Geographic Region > Arctic",
      "Ocean > Atlantic Ocean > North Atlantic Ocean > Gulf Of Mexico",
      "Liquid Earth > This Keyword > Is Invalid",
      "Seasonal",
      "> 1 Km"
  ] as Set

  static expectedGcmdKeywords = [
      gcmdScienceServices     : null,
      gcmdScience             : [
          'Oceans',
          'Oceans > Ocean Temperature',
          'Oceans > Ocean Temperature > Sea Surface Temperature'
      ] as Set,
      gcmdLocations           : [
          'Geographic Region',
          'Geographic Region > Arctic',
          'Ocean',
          'Ocean > Atlantic Ocean',
          'Ocean > Atlantic Ocean > North Atlantic Ocean',
          'Ocean > Atlantic Ocean > North Atlantic Ocean > Gulf Of Mexico',
          'Liquid Earth',
          'Liquid Earth > This Keyword',
          'Liquid Earth > This Keyword > Is Invalid'
      ] as Set,
      gcmdInstruments         : null,
      gcmdPlatforms           : null,
      gcmdProjects            : null,
      gcmdDataCenters         : [
          'SIO > Super Important Organization',
          'OSIO > Other Super Important Organization',
          'SSIO > Super SIO (Super Important Organization)'
      ] as Set,
      gcmdHorizontalResolution: null,
      gcmdVerticalResolution  : ['> 1 Km'] as Set,
      gcmdTemporalResolution  : ['Seasonal'] as Set
  ]

  ///////////////////////////////
  // Generic Indexed Fields    //
  ///////////////////////////////
  // def "only mapped #type fields are indexed"() {
  //   when:
  //   def result = TransformationUtils.reformatMessage(record, fields)
  //
  //   then:
  //   result.keySet().each({ assert fields.keySet().contains(it) }) // TODO this is a shallow only check!
  //
  //   where:
  //   type          | fields            | record
  //   'collection'  | collectionFields  | TestUtils.inputCollectionRecord
  //   'granule'     | granuleFields     | TestUtils.inputGranuleRecord
  // }

  def "why is it complaining about checksums #label"() {
    when:

    ParsedRecord record = ParsedRecord.newBuilder(TestUtils.inputAvroRecord)
      .setFileInformation(FileInformation.newBuilder().setChecksums([Checksum.newBuilder().setAlgorithm(ChecksumAlgorithm.MD5).setValue('abc').build()]).build()).build()

    def indexedRecord = TransformationUtils.reformatMessage(record, fields)

    then:

    indexedRecord.keySet().contains("checksums") == shouldIncludeChecksums

    where:
    label | shouldIncludeChecksums | fields
    'collections' | false | TestUtils.esConfig.indexedProperties(TestUtils.esConfig.COLLECTION_SEARCH_INDEX_ALIAS).keySet()
    'granules' | true | TestUtils.esConfig.indexedProperties(TestUtils.esConfig.GRANULE_SEARCH_INDEX_ALIAS).keySet()

  }

  def "clean up nested map before indexing strictly mapped fields for search (granule)"() {
    when:
    // def parsed = [
    //   identification: null,
    //   titles: null,
    //   description: null,
    //   dataAccess: null,
    //   thumbnail: null,
    //   temporalBounding: [
    //     beginDescriptor: ValidDescriptor.VALID,
    //     beginPrecision: ChronoUnit.DAYS.toString(),
    //     beginIndexable: true,
    //     beginZoneSpecified: null,
    //     beginUtcDateTimeString: "2000-02-01",
    //     beginYear: 2000,
    //     beginDayOfYear: 32,
    //     beginDayOfMonth: 1,
    //     beginMonth: 2,
    //     endDescriptor: null,
    //     endPrecision: null,
    //     endIndexable: null,
    //     endZoneSpecified: null,
    //     endUtcDateTimeString: null,
    //     endYear: null,
    //     endDayOfYear: null,
    //     endDayOfMonth: null,
    //     endMonth: null,
    //     instantDescriptor: null,
    //     instantPrecision: null,
    //     instantIndexable: null,
    //     instantZoneSpecified: null,
    //     instantUtcDateTimeString: null,
    //     instantYear: null,
    //     instantDayOfYear: null,
    //     instantDayOfMonth: null,
    //     instantMonth: null,
    //     rangeDescriptor: null,
    //     fakeField: 123
    //   ],
    //   spatialBounding: null,
    //   internalParentIdentifier: null,
    //   errors: [
    //     [
    //       nonsense: "horrible",
    //       source: "valid field"
    //     ]
    //   ],
    //   garbage:"nuke meeee"
    // ]
    ParsedRecord record = ParsedRecord.newBuilder(TestUtils.inputAvroRecord)
        .setAnalysis(
          Analysis.newBuilder().setTemporalBounding(
          TemporalBoundingAnalysis.newBuilder()
              .setBeginDescriptor(ValidDescriptor.VALID)
              .setBeginIndexable(true)
              .setBeginPrecision(ChronoUnit.DAYS.toString())
              .setBeginZoneSpecified(null)
              .setBeginUtcDateTimeString("2000-02-01")
              .setBeginYear(2000)
              .setBeginMonth(2)
              .setBeginDayOfYear(32)
              .setBeginDayOfMonth(1)
              .build()
              ).build()).build()


    // def pruned = TransformationUtils.pruneKnownUnmappedFields(parsed, IndexingInput.getUnmappedAnalysisAndErrorsIndexFields())
    def indexedRecord = TransformationUtils.reformatMessage(record, TestUtils.esConfig.indexedProperties(TestUtils.esConfig.GRANULE_SEARCH_INDEX_ALIAS).keySet())
    // def indexedRecord = DataUtils.removeFromMap(pruned, minus)

    then:
    // minus == [
    //   temporalBounding: [
    //     fakeField: 123
    //   ],
    //   errors: [
    //     [
    //       nonsense: "horrible",
    //     ]
    //   ],
    //   garbage:"nuke meeee"
    // ]

    // def expectedKeyset = ["fileIdentifier", "parentIdentifier", "doi", "title", "description", "keywords", "topicCategories", "temporalBounding", "spatialBounding", "isGlobal", "acquisitionInstruments", "acquisitionOperations", "acquisitionPlatforms", "dataFormats", "links", "responsibleParties", "thumbnail", "citeAsStatements", "crossReferences", "largerWorks", "legalConstraints", "services", "gcmdVerticalResolution", "gcmdDataCenters", "gcmdTemporalResolution", "gcmdLocations", "gcmdScience", "beginDate", "endDate", "endDayOfYear", "beginYear", "endMonth", "endYear", "endDayOfMonth", "dataFormat", "linkProtocol", "serviceLinks", "serviceLinkProtocol", "internalParentIdentifier", "filename", "checksums"]


    indexedRecord.keySet().size() == granuleFields.size()
    indexedRecord.keySet().each({ assert granuleFields.contains(it) })

  }

  // def "prune fields - spatial"() {
  //   when:
  //   def mapWithSpatial = [
  //     spatialBounding: [
  //     type: "MultiPolygon",
	// 	coordinates: [
	// 		[
	// 			[
	// 				[-180.0, -14.28],
	// 				[-61.821, -14.28],
	// 				[-61.821, 70.4],
	// 				[-180.0, 70.4],
	// 				[-180.0, -14.28]
	// 			]
	// 		],
	// 		[
	// 			[
	// 				[144.657, -14.28],
	// 				[180.0, -14.28],
	// 				[180.0, 70.4],
	// 				[144.657, 70.4],
	// 				[144.657, -14.28]
	// 			]
	// 		]
	// 	]
  //     ]
  //     ]
  //   def minus = TransformationUtils.identifyUnmappedFields(mapWithSpatial, TestUtils.esConfig.indexedProperties(TestUtils.esConfig.COLLECTION_SEARCH_INDEX_ALIAS))
  //
  //   then:
  //   minus == []
  // }

//   def "debug integration" () {
//     when:
//     def jsonrecord = (new JsonSlurper()).parseText("""{
// 			"type": "collection",
// 			"discovery": {
// 				"fileIdentifier": "gov.noaa.nodc:NDBC-COOPS",
// 				"parentIdentifier": null,
// 				"hierarchyLevelName": null,
// 				"doi": "doi:10.5072/FK2TEST",
// 				"purpose": "Basic research",
// 				"status": "completed",
// 				"credit": null,
// 				"title": "Coastal meteorological and water temperature data from National Water Level Observation Network (NWLON) and Physical Oceanographic Real-Time System (PORTS) stations of the NOAA Center for Operational Oceanographic Products and Services (CO-OPS)",
// 				"alternateTitle": null,
// 				"description": "The National Water Level Observation Network (NWLON) is a network of long-term water level stations operated and maintained by CO-OPS. NWLON stations are located on shore-based platforms, and primarily collect real-time water level measurements. As of January 2013, approximately 180 of 210 NWLON stations also collect real-time meteorological data. About 20 CO-OPS Physical Oceanographic Real-Time Systems (PORTS) comprise a group of water level stations, and 65 of these stations also collect real-time meteorological data. Data parameters include barometric pressure, wind direction, speed and gust, air temperature, and water temperature.",
// 				"keywords": [{
// 					"values": ["DOC/NOAA/NESDIS/NODC > National Oceanographic Data Center, NESDIS, NOAA, U.S. Department of Commerce", "DOC/NOAA/NESDIS/NCEI > National Centers for Environmental Information, NESDIS, NOAA, U.S. Department of Commerce"],
// 					"type": "dataCenter",
// 					"namespace": "GCMD Keywords - Data Centers"
// 				}, {
// 					"values": ["0107939", "0108059", "0109292", "0111163", "0112393", "0113250", "0113898", "0114473", "0115274", "0115910", "0116703", "0117348", "0117811", "0118682", "0120725", "0120726", "0122183", "0122220", "0123085", "0123363", "0124305", "0125493", "0126410", "0126781", "0127407", "0128443", "0129526", "0130004", "0131097", "0131931", "0137308", "0138303", "0139574", "0141136", "0144301", "0145770", "0148198", "0151779", "0154391", "0155989"],
// 					"type": null,
// 					"namespace": "NCEI ACCESSION NUMBER"
// 				}, {
// 					"values": ["AIR TEMPERATURE", "BAROMETRIC PRESSURE", "DEWPOINT", "RELATIVE HUMIDITY", "SEA SURFACE TEMPERATURE", "VISIBILITY", "WIND DIRECTION", "WIND GUST", "WIND SPEED"],
// 					"type": "theme",
// 					"namespace": "NODC DATA TYPES THESAURUS"
// 				}, {
// 					"values": ["anemometer", "barometers", "meteorological sensors", "thermistor"],
// 					"type": "instrument",
// 					"namespace": "NODC INSTRUMENT TYPES THESAURUS"
// 				}, {
// 					"values": ["meteorological", "physical"],
// 					"type": "theme",
// 					"namespace": "NODC OBSERVATION TYPES THESAURUS"
// 				}, {
// 					"values": ["FIXED PLATFORM"],
// 					"type": "platform",
// 					"namespace": "NODC PLATFORM NAMES THESAURUS"
// 				}, {
// 					"values": ["US DOC; NOAA; NOS; Center for Operational Oceanographic Products and Services"],
// 					"type": "dataCenter",
// 					"namespace": "NODC COLLECTING INSTITUTION NAMES THESAURUS"
// 				}, {
// 					"values": ["US DOC; NOAA; NWS; National Data Buoy Center"],
// 					"type": "dataCenter",
// 					"namespace": "NODC SUBMITTING INSTITUTION NAMES THESAURUS"
// 				}, {
// 					"values": ["National Water Level Observation Network (NWLON)", "Physical Oceanographic Real-Time System (PORTS)"],
// 					"type": "project",
// 					"namespace": "NODC PROJECT NAMES THESAURUS"
// 				}, {
// 					"values": ["Bay of Fundy", "Beaufort Sea", "Bering Sea", "Caribbean Sea", "Coastal waters of Alabama", "Coastal Waters of Florida", "Coastal Waters of Louisiana", "Coastal Waters of Mississippi", "Coastal Waters of Southeast Alaska and British Columbia", "Coastal Waters of Texas", "Florida Keys National Marine Sanctuary", "Great Lakes", "Gulf of Alaska", "Gulf of Mexico", "Kaneohe Bay", "Monterey Bay National Marine Sanctuary", "North Atlantic Ocean", "North Pacific Ocean", "Papahanaumokuakea Marine National Monument", "Philippine Sea", "San Diego Bay", "South Pacific Ocean", "Yaquina Bay"],
// 					"type": "place",
// 					"namespace": "NODC SEA AREA NAMES THESAURUS"
// 				}, {
// 					"values": ["oceanography"],
// 					"type": "theme",
// 					"namespace": "WMO_CategoryCode"
// 				}, {
// 					"values": ["GOVERNMENT AGENCIES-U.S. FEDERAL AGENCIES > DOC > NOAA > DOC/NOAA/NOS/CO-OPS > Center for Operational Oceanographic Products and Services, National Ocean Service, NOAA, U.S. Department of Commerce > http://tidesandcurrents.noaa.gov/", "GOVERNMENT AGENCIES-U.S. FEDERAL AGENCIES > DOC > NOAA > DOC/NOAA/NWS/NDBC > National Data Buoy Center, National Weather Service, NOAA, U.S. Department of Commerce > http://www.ndbc.noaa.gov/"],
// 					"type": "dataCenter",
// 					"namespace": "GCMD Keywords - Data Centers"
// 				}, {
// 					"values": ["EARTH SCIENCE > ATMOSPHERE > ATMOSPHERIC PRESSURE", "EARTH SCIENCE > ATMOSPHERE > ATMOSPHERIC TEMPERATURE", "EARTH SCIENCE > ATMOSPHERE > ATMOSPHERIC TEMPERATURE > SURFACE TEMPERATURE > DEW POINT TEMPERATURE", "EARTH SCIENCE > ATMOSPHERE > ATMOSPHERIC WATER VAPOR > HUMIDITY", "EARTH SCIENCE > ATMOSPHERE > ATMOSPHERIC WINDS > SURFACE WINDS > WIND SPEED/WIND DIRECTION", "EARTH SCIENCE > OCEANS > OCEAN OPTICS", "EARTH SCIENCE > OCEANS > OCEAN TEMPERATURE > SEA SURFACE TEMPERATURE"],
// 					"type": "theme",
// 					"namespace": "GCMD Keywords - Science Keywords"
// 				}, {
// 					"values": ["In Situ/Laboratory Instruments > Current/Wind Meters > ANEMOMETERS", "In Situ/Laboratory Instruments > Pressure/Height Meters > BAROMETERS", "In Situ/Laboratory Instruments > Temperature/Humidity Sensors > Thermistors > THERMISTORS"],
// 					"type": "instrument",
// 					"namespace": "GCMD Keywords - Instruments"
// 				}, {
// 					"values": ["air_pressure_at_sea_level", "air_temperature", "dew_point_temperature", "relative_humidity", "sea_surface_temperature", "time", "visibility_in_air", "wind_from_direction", "wind_speed", "wind_speed_of_gust"],
// 					"type": "theme",
// 					"namespace": "NetCDF Climate and Forecast (CF) Metadata Convention Standard Name Table"
// 				}, {
// 					"values": ["air_temperature_sensor", "anemometer", "barometer", "ct_sensor", "humidity_sensor", "ocean_temperature_sensor", "visibility_sensor"],
// 					"type": "instrument",
// 					"namespace": "NOS SENSOR THESAURUS"
// 				}, {
// 					"values": ["1611400 - NWWH1", "1612340 - OOUH1", "1612480 - MOKH1", "1615680 - KLIH1", "1617433 - KWHH1", "1617760 - ILOH1", "1619910 - SNDP5", "1630000 - APRP7", "1631428 - PGBP7", "1770000 - NSTP6", "1820000 - KWJP8", "1890000 - WAKP8", "2695540 - BEPB6", "8311030 - OBGN6", "8311062 - ALXN6", "8410140 - PSBM1", "8411060 - CFWM1", "8413320 - ATGM1", "8418150 - CASM1", "8419317 - WELM1", "8443970 - BHBM3", "8447386 - FRVM3", "8447387 - BLTM3", "8447412 - FRXM3", "8447930 - BZBM3", "8449130 - NTKM3", "8452660 - NWPR1", "8452944 - CPTR1", "8452951 - PTCR1", "8454000 - FOXR1", "8454049 - QPTR1", "8461490 - NLNC3", "8465705 - NWHC3", "8467150 - BRHC3", "8510560 - MTKN6", "8516945 - KPTN6", "8518750 - BATN6", "8519483 - BGNN4", "8519532 - MHRN6", "8530973 - ROBN4", "8531680 - SDHN4", "8534720 - ACYN4", "8536110 - CMAN4", "8537121 - SJSN4", "8538886 - TPBN4", "8539094 - BDRN4", "8540433 - MRCP1", "8545240 - PHBP1", "8548989 - NBLP1", "8551762 - DELD1", "8551910 - RDYD1", "8557380 - LWSD1", "8570283 - OCIM2", "8571421 - BISM2", "8571892 - CAMM2", "8573364 - TCBM2", "8573927 - CHCM2", "8574680 - BLTM2", "8574728 - FSKM2", "8575512 - APAM2", "8577018 - COVM2", "8577330 - SLIM2", "8578240 - PPTM2", "8594900 - WASD2", "8631044 - WAHV2", "8632200 - KPTV2", "8632837 - RPLV2", "8635027 - NCDV2", "8635750 - LWTV2", "8637611 - YKRV2", "8637689 - YKTV2", "8638511 - DOMV2", "8638595 - CRYV2", "8638610 - SWPV2", "8638614 - WDSV2", "8638863 - CBBV2", "8638999 - CHYV2", "8639348 - MNPV2", "8651370 - DUKN7", "8652587 - ORIN7", "8654467 - HCGN7", "8656483 - BFTN7", "8658120 - WLON7", "8658163 - JMPN7", "8661070 - MROS1", "8665530 - CHTS1", "8670870 - FPKG1", "8720030 - FRDF1", "8720215 - NFDF1", "8720218 - MYPF1", "8720219 - DMSF1", "8720228 - LTJF1", "8720233 - BLIF1", "8720245 - JXUF1", "8720357 - BKBF1", "8720503 - GCVF1", "8721604 - TRDF1", "8722670 - LKWF1", "8723214 - VAKF1", "8723970 - VCAF1", "8724580 - KYWF1", "8725110 - NPSF1", "8725520 - FMRF1", "8726384 - PMAF1", "8726412 - MTBF1", "8726520 - SAPF1", "8726607 - OPTF1", "8726667 - MCYF1", "8726669 - ERTF1", "8726673 - SBLF1", "8726679 - TSHF1", "8726694 - TPAF1", "8726724 - CWBF1", "8727520 - CKYF1", "8728690 - APCF1", "8729108 - PACF1", "8729210 - PCBF1", "8729840 - PCLF1", "8732828 - WBYA1", "8734673 - FMOA1", "8735180 - DILA1", "8736163 - MBPA1", "8736897 - MCGA1", "8737005 - PTOA1", "8737048 - OBLA1", "8741003 - PTBM6", "8741041 - ULAM6", "8741094 - RARM6", "8741501 - DKCM6", "8741533 - PNLM6", "8747437 - WYCM6", "8760721 - PILL1", "8760922 - PSTL1", "8761305 - SHBL1", "8761724 - GISL1", "8761927 - NWCL1", "8761955 - CARL1", "8762482 - BYGL1", "8762484 - FREL1", "8764044 - TESL1", "8764227 - AMRL1", "8764314 - EINL1", "8766072 - FRWL1", "8767816 - LCLL1", "8767961 - BKTL1", "8768094 - CAPL1", "8770570 - SBPT2", "8770613 - MGPT2", "8770822 - TXPT2", "8771013 - EPTT2", "8771341 - GNJT2", "8771450 - GTOT2", "8772447 - FCGT2", "8774770 - RCPT2", "8775870 - MQTT2", "8779770 - PTIT2", "9014070 - AGCM4", "9014090 - MBRM4", "9014098 - FTGM4", "9052030 - OSGN6", "9052058 - RCRN6", "9063012 - NIAN6", "9063020 - BUFN6", "9063028 - PSTN6", "9063038 - EREP1", "9063053 - FAIO1", "9063063 - CNDO1", "9063079 - MRHO1", "9063085 - THRO1", "9075014 - HRBM4", "9075065 - LPNM4", "9075080 - MACM4", "9075099 - DTLM4", "9076024 - RCKM4", "9076027 - WNEM4", "9076033 - LTRM4", "9076070 - SWPM4", "9087023 - LDTM4", "9087031 - HLNM4", "9087044 - CMTI2", "9087069 - KWNW3", "9087088 - MNMM4", "9087096 - PNLM4", "9099004 - PTIM4", "9099018 - MCGM4", "9099064 - DULM5", "9099090 - GDMM5", "9410170 - SDBC1", "9410172 - IIWC1", "9410230 - LJAC1", "9410660 - OHBC1", "9410665 - PRJC1", "9410670 - PFXC1", "9410840 - ICAC1", "9411340 - NTBC1", "9411406 - HRVC1", "9412110 - PSLC1", "9413450 - MTYC1", "9414290 - FTPC1", "9414296 - PXSC1", "9414311 - PXOC1", "9414523 - RTYC1", "9414750 - AAMC1", "9414763 - LNDC1", "9414769 - OMHC1", "9414776 - OKXC1", "9414797 - OBXC1", "9414847 - PPXC1", "9414863 - RCMC1", "9415020 - PRYC1", "9415102 - MZXC1", "9415115 - PSBC1", "9415118 - UPBC1", "9415141 - DPXC1", "9415144 - PCOC1", "9416841 - ANVC1", "9418767 - HBYC1", "9419750 - CECC1", "9431647 - PORO3", "9432780 - CHAO3", "9435380 - SBEO3", "9437540 - TLBO3", "9439011 - HMDO3", "9439040 - ASTO3", "9440422 - LOPW1", "9440910 - TOKW1", "9441102 - WPTW1", "9442396 - LAPW1", "9443090 - NEAW1", "9444090 - PTAW1", "9444900 - PTWW1", "9446482 - TCMW1", "9446484 - TCNW1", "9447130 - EBSW1", "9449424 - CHYW1", "9449880 - FRDW1", "9450460 - KECA2", "9451054 - PLXA2", "9451600 - ITKA2", "9452210 - JNEA2", "9452400 - SKTA2", "9452634 - ELFA2", "9453220 - YATA2", "9454050 - CRVA2", "9454240 - VDZA2", "9455090 - SWLA2", "9455500 - OVIA2", "9455760 - NKTA2", "9455920 - ANTA2", "9457292 - KDAA2", "9457804 - ALIA2", "9459450 - SNDA2", "9459881 - KGCA2", "9461380 - ADKA2", "9461710 - ATKA2", "9462450 - OLSA2", "9462620 - UNLA2", "9463502 - PMOA2", "9464212 - VCVA2", "9468756 - NMTA2", "9491094 - RDDA2", "9497645 - PRDA2", "9751364 - CHSV3", "9751381 - LAMV3", "9751401 - LTBV3", "9751639 - CHAV3", "9752695 - ESPP4", "9755371 - SJNP4", "9759110 - MGIP4", "9759394 - MGZP4", "9759412 - AUDP4", "9759938 - MISP4", "9761115 - BARA9"],
// 					"type": "platform",
// 					"namespace": "NOS - NWSLI PLATFORM THESAURUS"
// 				}, {
// 					"values": ["CONTINENT > NORTH AMERICA > CANADA > GREAT LAKES, CANADA", "CONTINENT > NORTH AMERICA > UNITED STATES OF AMERICA > GREAT LAKES", "OCEAN > ARCTIC OCEAN > BEAUFORT SEA", "OCEAN > ATLANTIC OCEAN > NORTH ATLANTIC OCEAN", "OCEAN > ATLANTIC OCEAN > NORTH ATLANTIC OCEAN > BAY OF FUNDY", "OCEAN > ATLANTIC OCEAN > NORTH ATLANTIC OCEAN > CARIBBEAN SEA", "OCEAN > ATLANTIC OCEAN > NORTH ATLANTIC OCEAN > GULF OF MEXICO", "OCEAN > PACIFIC OCEAN > CENTRAL PACIFIC OCEAN > HAWAIIAN ISLANDS", "OCEAN > PACIFIC OCEAN > NORTH PACIFIC OCEAN", "OCEAN > PACIFIC OCEAN > NORTH PACIFIC OCEAN > BERING SEA", "OCEAN > PACIFIC OCEAN > NORTH PACIFIC OCEAN > GULF OF ALASKA", "OCEAN > PACIFIC OCEAN > SOUTH PACIFIC OCEAN"],
// 					"type": "place",
// 					"namespace": "GCMD Keywords - Locations"
// 				}],
// 				"topicCategories": ["environment", "oceans", "climatologyMeteorologyAtmosphere"],
// 				"temporalBounding": {
// 					"beginDate": "2013-03-01",
// 					"beginIndeterminate": null,
// 					"endDate": null,
// 					"endIndeterminate": "now",
// 					"instant": null,
// 					"instantIndeterminate": null,
// 					"description": null
// 				},
// 				"spatialBounding": {
// 					"type": "MultiPolygon",
// 					"coordinates": [
// 						[
// 							[
// 								[-180.0, -14.28],
// 								[-61.821, -14.28],
// 								[-61.821, 70.4],
// 								[-180.0, 70.4],
// 								[-180.0, -14.28]
// 							]
// 						],
// 						[
// 							[
// 								[144.657, -14.28],
// 								[180.0, -14.28],
// 								[180.0, 70.4],
// 								[144.657, 70.4],
// 								[144.657, -14.28]
// 							]
// 						]
// 					]
// 				},
// 				"isGlobal": false,
// 				"acquisitionInstruments": [],
// 				"acquisitionOperations": [],
// 				"acquisitionPlatforms": [],
// 				"dataFormats": [{
// 					"name": "ORIGINATOR DATA FORMAT",
// 					"version": null
// 				}],
// 				"links": [{
// 					"linkName": "Descriptive Information",
// 					"linkProtocol": "HTTP",
// 					"linkUrl": "http://data.nodc.noaa.gov/cgi-bin/iso?id=gov.noaa.nodc:NDBC-COOPS",
// 					"linkDescription": "Navigate directly to the URL for a descriptive web page with download links.",
// 					"linkFunction": "information"
// 				}, {
// 					"linkName": "Granule Search",
// 					"linkProtocol": "HTTP",
// 					"linkUrl": "http://www.nodc.noaa.gov/search/granule/rest/find/document?searchText=fileIdentifier%3ACO-OPS*&start=1&max=100&expandResults=true&f=searchPage",
// 					"linkDescription": "Granule Search",
// 					"linkFunction": "search"
// 				}, {
// 					"linkName": "THREDDS",
// 					"linkProtocol": "THREDDS",
// 					"linkUrl": "http://data.nodc.noaa.gov/thredds/catalog/ndbc/co-ops/",
// 					"linkDescription": "These data are available through a variety of services via a THREDDS (Thematic Real-time Environmental Distributed Data Services) Data Server (TDS). Depending on the dataset, the TDS can provide WMS, WCS, DAP, HTTP, and other data access and metadata services as well. For more information on the TDS, see http://www.unidata.ucar.edu/software/thredds/current/tds/.",
// 					"linkFunction": "download"
// 				}, {
// 					"linkName": "OPeNDAP",
// 					"linkProtocol": "DAP",
// 					"linkUrl": "http://data.nodc.noaa.gov/opendap/ndbc/co-ops/",
// 					"linkDescription": "These data are available through the Data Access Protocol (DAP) via an OPeNDAP Hyrax server. For a listing of OPeNDAP clients which may be used to access OPeNDAP-enabled data sets, please see the OPeNDAP website at http://opendap.org/.",
// 					"linkFunction": "download"
// 				}, {
// 					"linkName": "HTTP",
// 					"linkProtocol": "HTTP",
// 					"linkUrl": "http://data.nodc.noaa.gov/ndbc/co-ops/",
// 					"linkDescription": "Navigate directly to the URL for data access and direct download.",
// 					"linkFunction": "download"
// 				}, {
// 					"linkName": "FTP",
// 					"linkProtocol": "FTP",
// 					"linkUrl": "ftp://ftp.nodc.noaa.gov/pub/data.nodc/ndbc/co-ops/",
// 					"linkDescription": "These data are available through the File Transfer Protocol (FTP). You may use any FTP client to download these data.",
// 					"linkFunction": "download"
// 				}],
// 				"responsibleParties": [{
// 					"individualName": null,
// 					"organizationName": "DOC/NOAA/NESDIS/NCEI > National Centers for Environmental Information, NESDIS, NOAA, U.S. Department of Commerce",
// 					"positionName": null,
// 					"role": "publisher",
// 					"email": "NODC.DataOfficer@noaa.gov",
// 					"phone": "301-713-3277"
// 				}, {
// 					"individualName": null,
// 					"organizationName": "DOC/NOAA/NESDIS/NODC > National Oceanographic Data Center, NESDIS, NOAA, U.S. Department of Commerce",
// 					"positionName": null,
// 					"role": "publisher",
// 					"email": "NODC.DataOfficer@noaa.gov",
// 					"phone": "301-713-3277"
// 				}, {
// 					"individualName": "Rex V Hervey",
// 					"organizationName": "US DOC; NOAA; NWS; National Data Buoy Center (NDBC)",
// 					"positionName": null,
// 					"role": "resourceProvider",
// 					"email": "rex.hervey@noaa.gov",
// 					"phone": "228-688-3007"
// 				}, {
// 					"individualName": null,
// 					"organizationName": "US DOC; NOAA; NWS; National Data Buoy Center (NDBC)",
// 					"positionName": null,
// 					"role": "resourceProvider",
// 					"email": null,
// 					"phone": null
// 				}, {
// 					"individualName": null,
// 					"organizationName": "DOC/NOAA/NESDIS/NCEI > National Centers for Environmental Information, NESDIS, NOAA, U.S. Department of Commerce",
// 					"positionName": null,
// 					"role": "pointOfContact",
// 					"email": "NCEI.Info@noaa.gov",
// 					"phone": "301-713-3277"
// 				}, {
// 					"individualName": null,
// 					"organizationName": "Global Change Data Center, Science and Exploration Directorate, Goddard Space Flight Center (GSFC) National Aeronautics and Space Administration (NASA)",
// 					"positionName": null,
// 					"role": "custodian",
// 					"email": null,
// 					"phone": null
// 				}],
// 				"thumbnail": "http://data.nodc.noaa.gov/cgi-bin/gfx?id=gov.noaa.nodc:NDBC-COOPS",
// 				"thumbnailDescription": "Preview graphic",
// 				"creationDate": null,
// 				"revisionDate": null,
// 				"publicationDate": "2013-06-05",
// 				"citeAsStatements": ["Cite as: Hervey, R. V. and US DOC; NOAA; NWS; National Data Buoy Center (2013). Coastal meteorological and water temperature data from National Water Level Observation Network (NWLON) and Physical Oceanographic Real-Time System (PORTS) stations of the NOAA Center for Operational Oceanographic Products and Services (CO-OPS). National Oceanographic Data Center, NOAA. Dataset. [access date]"],
// 				"crossReferences": [],
// 				"largerWorks": [],
// 				"useLimitation": "accessLevel: Public",
// 				"legalConstraints": ["Cite as: Hervey, R. V. and US DOC; NOAA; NWS; National Data Buoy Center (2013). Coastal meteorological and water temperature data from National Water Level Observation Network (NWLON) and Physical Oceanographic Real-Time System (PORTS) stations of the NOAA Center for Operational Oceanographic Products and Services (CO-OPS). National Oceanographic Data Center, NOAA. Dataset. [access date]", "NOAA and NCEI cannot provide any warranty as to the accuracy, reliability, or completeness of furnished data. Users assume responsibility to determine the usability of these data. The user is responsible for the results of any application of this data for other than its intended purpose."],
// 				"accessFeeStatement": null,
// 				"orderingInstructions": null,
// 				"edition": null,
// 				"dsmmAccessibility": 0,
// 				"dsmmDataIntegrity": 0,
// 				"dsmmDataQualityAssessment": 0,
// 				"dsmmDataQualityAssurance": 0,
// 				"dsmmDataQualityControlMonitoring": 0,
// 				"dsmmPreservability": 0,
// 				"dsmmProductionSustainability": 0,
// 				"dsmmTransparencyTraceability": 0,
// 				"dsmmUsability": 0,
// 				"dsmmAverage": 0.0,
// 				"updateFrequency": "asNeeded",
// 				"presentationForm": "tableDigital",
// 				"services": []
// 			},
// 			"analysis": {
// 				"identification": {
// 					"fileIdentifierExists": true,
// 					"fileIdentifierString": "gov.noaa.nodc:NDBC-COOPS",
// 					"doiExists": true,
// 					"doiString": "doi:10.5072/FK2TEST",
// 					"parentIdentifierExists": false,
// 					"parentIdentifierString": null,
// 					"hierarchyLevelNameExists": false,
// 					"isGranule": false
// 				},
// 				"titles": {
// 					"titleExists": true,
// 					"titleCharacters": 244,
// 					"alternateTitleExists": false,
// 					"alternateTitleCharacters": 0,
// 					"titleFleschReadingEaseScore": -15.662258064516124,
// 					"alternateTitleFleschReadingEaseScore": null,
// 					"titleFleschKincaidReadingGradeLevel": 23.14516129032258,
// 					"alternateTitleFleschKincaidReadingGradeLevel": null
// 				},
// 				"description": {
// 					"descriptionExists": true,
// 					"descriptionCharacters": 642,
// 					"descriptionFleschReadingEaseScore": 24.320808988764043,
// 					"descriptionFleschKincaidReadingGradeLevel": 14.289078651685397
// 				},
// 				"dataAccess": {
// 					"dataAccessExists": true
// 				},
// 				"thumbnail": {
// 					"thumbnailExists": true
// 				},
// 				"temporalBounding": {
// 					"beginDescriptor": "VALID",
// 					"beginPrecision": "Days",
// 					"beginIndexable": true,
// 					"beginZoneSpecified": null,
// 					"beginUtcDateTimeString": "2013-03-01T00:00:00Z",
// 					"beginYear": 2013,
// 					"beginDayOfYear": 60,
// 					"beginDayOfMonth": 1,
// 					"beginMonth": 3,
// 					"endDescriptor": "UNDEFINED",
// 					"endPrecision": null,
// 					"endIndexable": true,
// 					"endZoneSpecified": null,
// 					"endUtcDateTimeString": null,
// 					"endYear": null,
// 					"endDayOfYear": null,
// 					"endDayOfMonth": null,
// 					"endMonth": null,
// 					"instantDescriptor": "UNDEFINED",
// 					"instantPrecision": null,
// 					"instantIndexable": true,
// 					"instantZoneSpecified": null,
// 					"instantUtcDateTimeString": null,
// 					"instantYear": null,
// 					"instantDayOfYear": null,
// 					"instantDayOfMonth": null,
// 					"instantMonth": null,
// 					"rangeDescriptor": "ONGOING"
// 				},
// 				"spatialBounding": {
// 					"spatialBoundingExists": true,
// 					"isValid": true,
// 					"validationError": null
// 				}
// 			},
// 			"fileInformation": null,
// 			"fileLocations": {},
// 			"publishing": {
// 				"isPrivate": false,
// 				"until": null
// 			},
// 			"relationships": [],
// 			"errors": []
// 		}""")
//     def record = AvroUtils.mapToAvro((Map)jsonrecord, ParsedRecord)
//
//     // println("zeb "+JsonOutput.toJson(parsed))
//     println("ZEB")
//     println(record)
//     def discovery = record.getDiscovery();
//     def analysis = record.getAnalysis();
//     def discoveryMap = AvroUtils.avroToMap(discovery, true);
//
//     // prepare and apply fields that need to be reformatted for search
//     discoveryMap.putAll(TransformationUtils.prepareGcmdKeyword(discovery));
//     discoveryMap.putAll(TransformationUtils.prepareDates(discovery.getTemporalBounding(), analysis.getTemporalBounding()));
//     discoveryMap.put("dataFormat", TransformationUtils.prepareDataFormats(discovery));
//     discoveryMap.put("linkProtocol", TransformationUtils.prepareLinkProtocols(discovery));
//     discoveryMap.put("serviceLinks", TransformationUtils.prepareServiceLinks(discovery));
//     discoveryMap.put("serviceLinkProtocol", TransformationUtils.prepareServiceLinkProtocols(discovery));
//     discoveryMap.putAll(TransformationUtils.prepareResponsibleParties(record));
//     discoveryMap.put("internalParentIdentifier", TransformationUtils.prepareInternalParentIdentifier(record));
//     discoveryMap.put("filename", TransformationUtils.prepareFilename(record));
//     discoveryMap.put("checksums", TransformationUtils.prepareChecksums(record));
//
//     def pruned = TransformationUtils.pruneKnownUnmappedFields(discoveryMap, IndexingInput.getUnmappedAnalysisAndErrorsIndexFields())
//     def minus = TransformationUtils.identifyUnmappedFields(pruned, TestUtils.esConfig.indexedProperties(TestUtils.esConfig.COLLECTION_SEARCH_INDEX_ALIAS))
//     // def indexedRecord = DataUtils.removeFromMap(pruned, minus)
//
// println(JsonOutput.toJson(pruned))
// println(JsonOutput.toJson(minus))
//     then:
//     pruned == []
//     minus == [
//       internalParentIdentifier: null, // ok for granule, not collection
//       temporalBounding: [
//         fakeField: 123
//       ],
//       errors: [
//         [
//           nonsense: "horrible",
//         ]
//       ],
//       garbage:"nuke meeee"
//     ]
//     //
//     // def expectedKeyset = ["identification", "titles", "description", "dataAccess", "thumbnail", "temporalBounding", "spatialBounding", "errors" ]
//     // indexedRecord.keySet().size() == expectedKeyset.size()
//     // indexedRecord.keySet().each({ assert expectedKeyset.contains(it) })
//     //
//     // indexedRecord.temporalBounding == [
//     //     beginDescriptor: ValidDescriptor.VALID,
//     //     beginPrecision: ChronoUnit.DAYS.toString(),
//     //     beginIndexable: true,
//     //     beginZoneSpecified: null,
//     //     beginUtcDateTimeString: "2000-02-01",
//     //     endDescriptor: null,
//     //     endPrecision: null,
//     //     endIndexable: null,
//     //     endZoneSpecified: null,
//     //     endUtcDateTimeString: null,
//     //     instantDescriptor: null,
//     //     instantPrecision: null,
//     //     instantIndexable: null,
//     //     instantZoneSpecified: null,
//     //     instantUtcDateTimeString: null,
//     //     rangeDescriptor: null
//     //   ]
//     //
//     // indexedRecord.errors.size() == 1
//     // indexedRecord.errors[0] == [nonsense:"horrible", // FIXME this is not actually desired
//     //       source: "valid field"
//     //     ]
//   }

  def "clean up nested map before indexing strictly mapped fields for search (collection)"() {
    when:

    ParsedRecord record = ParsedRecord.newBuilder(TestUtils.inputAvroRecord)
        .setAnalysis(
          Analysis.newBuilder().setTemporalBounding(
          TemporalBoundingAnalysis.newBuilder()
              .setBeginDescriptor(ValidDescriptor.VALID)
              .setBeginIndexable(true)
              .setBeginPrecision(ChronoUnit.DAYS.toString())
              .setBeginZoneSpecified(null)
              .setBeginUtcDateTimeString("2000-02-01")
              .setBeginYear(2000)
              .setBeginMonth(2)
              .setBeginDayOfYear(32)
              .setBeginDayOfMonth(1)
              .build()
              ).build()).build()


    // def pruned = TransformationUtils.pruneKnownUnmappedFields(parsed, IndexingInput.getUnmappedAnalysisAndErrorsIndexFields())
    def indexedRecord = TransformationUtils.reformatMessage(record, TestUtils.esConfig.indexedProperties(TestUtils.esConfig.COLLECTION_SEARCH_INDEX_ALIAS).keySet())
    // def indexedRecord = DataUtils.removeFromMap(pruned, minus)

    then:
    //
    // def expectedKeyset = ["fileIdentifier", "parentIdentifier", "doi", "title", "description", "keywords", "topicCategories", "temporalBounding", "spatialBounding", "isGlobal", "acquisitionInstruments", "acquisitionOperations", "acquisitionPlatforms", "dataFormats", "links", "responsibleParties", "thumbnail", "citeAsStatements", "crossReferences", "largerWorks", "useLimitation", "legalConstraints", "accessFeeStatement", "orderingInstructions", "edition", "dsmmAverage", "services", "gcmdVerticalResolution", "gcmdDataCenters", "gcmdTemporalResolution", "gcmdLocations", "gcmdScience", "beginDate", "endDate", "endDayOfYear", "beginYear", "endMonth", "endYear", "endDayOfMonth", "dataFormat", "linkProtocol", "serviceLinks", "serviceLinkProtocol", "organizationNames",
    // "individualNames", "checksums"]


    indexedRecord.keySet().size() == collectionFields.size()
    collectionFields.each({ assert indexedRecord.keySet().contains(it) })
    indexedRecord.keySet().each({ assert collectionFields.contains(it) })

  }

  // def "clean up nested map before indexing strictly mapped fields for analysis and errors (granule)"() { // TODO change to use reformatMessageFor method
  //   when:
  //   def parsed = [
  //     identification: null,
  //     internalParentIdentifier: null,
  //     titles: null,
  //     description: null,
  //     dataAccess: null,
  //     thumbnail: null,
  //     temporalBounding: [
  //       beginDescriptor: ValidDescriptor.VALID,
  //       beginPrecision: ChronoUnit.DAYS.toString(),
  //       beginIndexable: true,
  //       beginZoneSpecified: null,
  //       beginUtcDateTimeString: "2000-02-01",
  //       beginYear: 2000,
  //       beginDayOfYear: 32,
  //       beginDayOfMonth: 1,
  //       beginMonth: 2,
  //       endDescriptor: null,
  //       endPrecision: null,
  //       endIndexable: null,
  //       endZoneSpecified: null,
  //       endUtcDateTimeString: null,
  //       endYear: null,
  //       endDayOfYear: null,
  //       endDayOfMonth: null,
  //       endMonth: null,
  //       instantDescriptor: null,
  //       instantPrecision: null,
  //       instantIndexable: null,
  //       instantZoneSpecified: null,
  //       instantUtcDateTimeString: null,
  //       instantYear: null,
  //       instantDayOfYear: null,
  //       instantDayOfMonth: null,
  //       instantMonth: null,
  //       rangeDescriptor: null,
  //       fakeField: 123
  //     ],
  //     spatialBounding: null,
  //     errors: [
  //       [
  //         nonsense: "horrible",
  //         source: "valid field"
  //       ]
  //     ],
  //     garbage:"nuke meeee"
  //   ]
  //
  //   // ParsedRecord record = ParsedRecord.newBuilder(TestUtils.inputAvroRecord)
  //   //     .setAnalysis(
  //   //       Analysis.newBuilder().setTemporalBounding(
  //   //       TemporalBoundingAnalysis.newBuilder()
  //   //           .setBeginDescriptor(ValidDescriptor.VALID)
  //   //           .setBeginIndexable(true)
  //   //           .setBeginPrecision(ChronoUnit.DAYS.toString())
  //   //           .setBeginZoneSpecified(null)
  //   //           .setBeginUtcDateTimeString("2000-02-01")
  //   //           .setBeginYear(2000)
  //   //           .setBeginMonth(2)
  //   //           .setBeginDayOfYear(32)
  //   //           .setBeginDayOfMonth(1)
  //   //           .build()
  //   //           ).build()).build()
  //
  //           // def parsed = TransformationUtils.unfilteredAEMessage(record)
  //
  //   def pruned = TransformationUtils.pruneKnownUnmappedFields(parsed, IndexingInput.getUnmappedAnalysisAndErrorsIndexFields())
  //   def minus = TransformationUtils.identifyUnmappedFields(pruned, TestUtils.esConfig.indexedProperties(TestUtils.esConfig.GRANULE_ERROR_AND_ANALYSIS_INDEX_ALIAS))
  //   def indexedRecord = DataUtils.removeFromMap(pruned, minus)
  //
  //   then:
  //   minus == [
  //     temporalBounding: [
  //       fakeField: 123
  //     ],
  //     errors: [
  //       [
  //         nonsense: "horrible",
  //       ]
  //     ],
  //     garbage:"nuke meeee"
  //   ]
  //
  //   def expectedKeyset = ["identification", "titles", "description", "dataAccess", "thumbnail", "temporalBounding", "spatialBounding", "internalParentIdentifier", "errors" ]
  //   indexedRecord.keySet().size() == expectedKeyset.size()
  //   indexedRecord.keySet().each({ assert expectedKeyset.contains(it) })
  //
  //   indexedRecord.temporalBounding == [
  //       beginDescriptor: ValidDescriptor.VALID,
  //       beginPrecision: ChronoUnit.DAYS.toString(),
  //       beginIndexable: true,
  //       beginZoneSpecified: null,
  //       beginUtcDateTimeString: "2000-02-01",
  //       endDescriptor: null,
  //       endPrecision: null,
  //       endIndexable: null,
  //       endZoneSpecified: null,
  //       endUtcDateTimeString: null,
  //       instantDescriptor: null,
  //       instantPrecision: null,
  //       instantIndexable: null,
  //       instantZoneSpecified: null,
  //       instantUtcDateTimeString: null,
  //       rangeDescriptor: null
  //     ]
  //
  //   indexedRecord.errors.size() == 1
  //   indexedRecord.errors[0] == [nonsense:"horrible", // FIXME this is not actually desired
  //         source: "valid field"
  //       ]
  //
  // }

    // def "clean up nested map before indexing strictly mapped fields for analysis and errors (collection)"() { // TODO change to use reformatMessageFor method
    //   when:
    //   def parsed = [
    //     identification: null,
    //     internalParentIdentifier: null,
    //     titles: null,
    //     description: null,
    //     dataAccess: null,
    //     thumbnail: null,
    //     temporalBounding: [
    //       beginDescriptor: ValidDescriptor.VALID,
    //       beginPrecision: ChronoUnit.DAYS.toString(),
    //       beginIndexable: true,
    //       beginZoneSpecified: null,
    //       beginUtcDateTimeString: "2000-02-01",
    //       beginYear: 2000,
    //       beginDayOfYear: 32,
    //       beginDayOfMonth: 1,
    //       beginMonth: 2,
    //       endDescriptor: null,
    //       endPrecision: null,
    //       endIndexable: null,
    //       endZoneSpecified: null,
    //       endUtcDateTimeString: null,
    //       endYear: null,
    //       endDayOfYear: null,
    //       endDayOfMonth: null,
    //       endMonth: null,
    //       instantDescriptor: null,
    //       instantPrecision: null,
    //       instantIndexable: null,
    //       instantZoneSpecified: null,
    //       instantUtcDateTimeString: null,
    //       instantYear: null,
    //       instantDayOfYear: null,
    //       instantDayOfMonth: null,
    //       instantMonth: null,
    //       rangeDescriptor: null,
    //       fakeField: 123
    //     ],
    //     spatialBounding: null,
    //     errors: [
    //       [
    //         nonsense: "horrible",
    //         source: "valid field"
    //       ]
    //     ],
    //     garbage:"nuke meeee"
    //   ]
    //
    //   // ParsedRecord record = ParsedRecord.newBuilder(TestUtils.inputAvroRecord)
    //   //     .setAnalysis(
    //   //       Analysis.newBuilder().setTemporalBounding(
    //   //       TemporalBoundingAnalysis.newBuilder()
    //   //           .setBeginDescriptor(ValidDescriptor.VALID)
    //   //           .setBeginIndexable(true)
    //   //           .setBeginPrecision(ChronoUnit.DAYS.toString())
    //   //           .setBeginZoneSpecified(null)
    //   //           .setBeginUtcDateTimeString("2000-02-01")
    //   //           .setBeginYear(2000)
    //   //           .setBeginMonth(2)
    //   //           .setBeginDayOfYear(32)
    //   //           .setBeginDayOfMonth(1)
    //   //           .build()
    //   //           ).build()).build()
    //
    //           // def parsed = TransformationUtils.unfilteredAEMessage(record)
    //
    //   def pruned = TransformationUtils.pruneKnownUnmappedFields(parsed, IndexingInput.getUnmappedAnalysisAndErrorsIndexFields())
    //   def minus = TransformationUtils.identifyUnmappedFields(pruned, TestUtils.esConfig.indexedProperties(TestUtils.esConfig.COLLECTION_ERROR_AND_ANALYSIS_INDEX_ALIAS))
    //   def indexedRecord = DataUtils.removeFromMap(pruned, minus)
    //
    //   then:
    //   minus == [
    //     internalParentIdentifier: null, // ok for granule, not collection
    //     temporalBounding: [
    //       fakeField: 123
    //     ],
    //     errors: [
    //       [
    //         nonsense: "horrible",
    //       ]
    //     ],
    //     garbage:"nuke meeee"
    //   ]
    //
    //   def expectedKeyset = ["identification", "titles", "description", "dataAccess", "thumbnail", "temporalBounding", "spatialBounding", "errors" ]
    //   indexedRecord.keySet().size() == expectedKeyset.size()
    //   indexedRecord.keySet().each({ assert expectedKeyset.contains(it) })
    //
    //   indexedRecord.temporalBounding == [
    //       beginDescriptor: ValidDescriptor.VALID,
    //       beginPrecision: ChronoUnit.DAYS.toString(),
    //       beginIndexable: true,
    //       beginZoneSpecified: null,
    //       beginUtcDateTimeString: "2000-02-01",
    //       endDescriptor: null,
    //       endPrecision: null,
    //       endIndexable: null,
    //       endZoneSpecified: null,
    //       endUtcDateTimeString: null,
    //       instantDescriptor: null,
    //       instantPrecision: null,
    //       instantIndexable: null,
    //       instantZoneSpecified: null,
    //       instantUtcDateTimeString: null,
    //       rangeDescriptor: null
    //     ]
    //
    //   indexedRecord.errors.size() == 1
    //   indexedRecord.errors[0] == [nonsense:"horrible", // FIXME this is not actually desired
    //         source: "valid field"
    //       ]
    //
    // }

  ////////////////////////////////
  // Identifiers, "Names"       //
  ////////////////////////////////
  def "produces internalParentIdentifier for collection record correctly"() {
    expect:
    TransformationUtils.prepareInternalParentIdentifier(TestUtils.inputAvroRecord) == null
  }

  def "produces internalParentIdentifier for granule record correctly"() {
    def testId = "ABC"
    def record = ParsedRecord.newBuilder(TestUtils.inputAvroRecord)
        .setType(RecordType.granule)
        .setRelationships([
            Relationship.newBuilder().setType(RelationshipType.COLLECTION).setId(testId).build()
        ])
        .build()

    expect:
    TransformationUtils.prepareInternalParentIdentifier(record) == testId
  }

  def "produces filename for collection record correctly"() {
    expect:
    TransformationUtils.prepareFilename(TestUtils.inputAvroRecord) == null
  }

  def "produces filename for granule record correctly when record has data"() {
    def filename = "ABC"
    def record = ParsedRecord.newBuilder(TestUtils.inputAvroRecord)
        .setType(RecordType.granule)
        .setFileInformation(FileInformation.newBuilder().setName(filename).build())
        .build()

    expect:
    TransformationUtils.prepareFilename(record) == filename
  }

  def "produces filename for granule record correctly when record does not have data"() {
    def record = ParsedRecord.newBuilder(TestUtils.inputAvroRecord)
        .setType(RecordType.granule)
        .build()

    expect:
    TransformationUtils.prepareFilename(record) == null
  }

  ////////////////////////////////
  // Services, Links, Protocols //
  ////////////////////////////////
  def "prepares service links"() {
    when:
    def discovery = TestUtils.inputGranuleRecord.discovery
    def result = TransformationUtils.prepareServiceLinks(discovery)

    then:
    result.size() == 1
    result[0].title == "Multibeam Bathymetric Surveys ArcGIS Map Service"
    result[0].alternateTitle == "Alternate Title for Testing"
    result[0].description == "NOAA's National Centers for Environmental Information (NCEI) is the U.S. national archive for multibeam bathymetric data and presently holds over 2400 surveys received from sources worldwide, including the U.S. academic fleet via the Rolling Deck to Repository (R2R) program. In addition to deep-water data, the multibeam database also includes hydrographic multibeam survey data from the National Ocean Service (NOS). This map service shows navigation for multibeam bathymetric surveys in NCEI's archive. Older surveys are colored orange, and more recent recent surveys are green."
    result[0].links as Set == [
        [
            linkProtocol   : 'http',
            linkUrl        : 'https://maps.ngdc.noaa.gov/arcgis/services/web_mercator/multibeam_dynamic/MapServer/WMSServer?request=GetCapabilities&service=WMS',
            linkName       : 'Multibeam Bathymetric Surveys Web Map Service (WMS)',
            linkDescription: 'The Multibeam Bathymetric Surveys ArcGIS cached map service provides rapid display of ship tracks from global scales down to zoom level 9 (approx. 1:1,200,000 scale).',
            linkFunction   : 'search'
        ],
        [
            linkProtocol   : 'http',
            linkUrl        : 'https://maps.ngdc.noaa.gov/arcgis/rest/services/web_mercator/multibeam/MapServer',
            linkName       : 'Multibeam Bathymetric Surveys ArcGIS Cached Map Service',
            linkDescription: 'Capabilities document for Open Geospatial Consortium Web Map Service for Multibeam Bathymetric Surveys',
            linkFunction   : 'search'
        ]
    ] as Set
  }

  def "prepares service link protocols"() {
    Set protocols = ['HTTP']
    def discovery = TestUtils.inputGranuleRecord.discovery

    expect:
    TransformationUtils.prepareServiceLinkProtocols(discovery) == protocols
  }

  def "prepares link protocols"() {
    Set protocols = ['HTTP']
    def discovery = TestUtils.inputGranuleRecord.discovery

    expect:
    TransformationUtils.prepareLinkProtocols(discovery) == protocols
  }

  ////////////////////////////
  // Data Formats           //
  ////////////////////////////
  def "prepares data formats"() {
    def discovery = TestUtils.inputGranuleRecord.discovery

    expect:
    TransformationUtils.prepareDataFormats(discovery) == [
        "ASCII",
        "CSV",
        "NETCDF",
        "NETCDF > 4",
        "NETCDF > CLASSIC",
    ] as Set
  }

  ////////////////////////////
  // Responsible Parties    //
  ////////////////////////////
  def "prepares responsible party names"() {
    when:
    def record = TestUtils.inputCollectionRecord
    def result = TransformationUtils.prepareResponsibleParties(record)

    then:
    result.individualNames == [
        'John Smith',
        'Jane Doe',
        'Jarianna Whackositz',
        'Dr. Quinn McClojure Man',
        'Zebulon Pike',
        'Little Rhinoceros',
        'Skeletor McSkittles',
    ] as Set
    result.organizationNames == [
        'University of Awesome',
        'Secret Underground Society',
        'Soap Boxes Inc.',
        'Pikes Peak Inc.',
        'Alien Infested Spider Monkey Rescue',
        'The Underworld',
        'Super Important Organization',
    ] as Set
  }

  def "does not prepare responsible party names for granules"() {
    when:
    def record = TestUtils.inputGranuleRecord
    def result = TransformationUtils.prepareResponsibleParties(record)

    then:
    result.individualNames == [] as Set
    result.organizationNames == [] as Set
  }

  def "party names are not included in granule search info"() {
    when:
    def record = TestUtils.inputGranuleRecord // <-- granule!
    def result = TransformationUtils.reformatMessage(record, collectionFields) // <-- top level reformat method!

    then:
    result.individualNames == [] as Set
    result.organizationNames == [] as Set
  }

  ////////////////////////////
  // Dates                  //
  ////////////////////////////
  def "When #situation.description, expected temporal bounding generated"() {
    when:
    def newTimeMetadata = TransformationUtils.prepareDates(situation.bounding, situation.analysis)

    then:
    newTimeMetadata.sort() == expectedResult

    where:
    situation               | expectedResult
    situations.instantDay   | [beginDate: '1999-12-31T00:00:00Z', beginYear: 1999, beginDayOfYear: 365, beginDayOfMonth: 31, beginMonth: 12, endDate: '1999-12-31T23:59:59Z', endYear: 1999, endDayOfYear:365, endDayOfMonth:31, endMonth:12].sort()
    situations.instantYear  | [beginDate: '1999-01-01T00:00:00Z', beginYear: 1999, beginDayOfYear: 1, beginDayOfMonth:1, beginMonth: 1, endDate: '1999-12-31T23:59:59Z', endYear: 1999, endDayOfMonth:31, endDayOfYear:365, endMonth:12].sort()
    situations.instantPaleo | [beginDate: null, endDate: null, beginYear: -1000000000, endYear: -1000000000, beginDayOfYear: null, beginDayOfMonth:null, beginMonth: null, endDayOfYear: null, endDayOfMonth:null, endMonth:null].sort()
    situations.instantNano  | [beginDate: '2008-04-01T00:00:00Z', beginYear: 2008, beginDayOfYear: 92, beginDayOfMonth:1, beginMonth: 4, endDate: '2008-04-01T00:00:00Z', endYear: 2008,  endDayOfYear: 92, endDayOfMonth:1, endMonth:4].sort()
    situations.bounded      | [beginDate: '1900-01-01T00:00:00Z',  beginYear: 1900, beginDayOfYear: 1, beginDayOfMonth:1, beginMonth: 1, endDate: '2009-12-31T23:59:59Z', endYear: 2009, endDayOfYear:365, endDayOfMonth:31, endMonth:12].sort()
    situations.paleoBounded | [beginDate: null, endDate: null, beginYear: -2000000000, endYear: -1000000000, beginDayOfYear: null, beginDayOfMonth:null, beginMonth: null, endDayOfYear: null, endDayOfMonth:null, endMonth:null].sort()
    situations.ongoing      | [beginDate: "1975-06-15T12:30:00Z", beginDayOfMonth:15, beginDayOfYear:166, beginMonth:6, beginYear:1975, endDate:null, endYear:null, endDayOfYear: null, endDayOfMonth: null, endMonth: null].sort()
    situations.empty        | [beginDate: null, endDate: null, beginYear: null, endYear: null, beginDayOfYear: null, beginDayOfMonth:null, beginMonth: null, endDayOfYear: null, endDayOfMonth:null, endMonth:null].sort()
  }

  def "temporal bounding with #testCase dates is prepared correctly"() {
    given:
    def bounding = TemporalBounding.newBuilder().setBeginDate(begin).setEndDate(end).build()
    def analysis = Temporal.analyzeBounding(Discovery.newBuilder().setTemporalBounding(bounding).build())

    when:
    def result = TransformationUtils.prepareDates(bounding, analysis)

    then:
    expected.forEach({ k, v ->
      assert result.get(k) == v
    })

    where:
    testCase      | begin                  | end                     | expected
    'typical'     | '2005-05-09T00:00:00Z' | '2010-10-01'            | [beginDate: '2005-05-09T00:00:00Z', endDate: '2010-10-01T23:59:59.999Z', beginYear: 2005, endYear: 2010]
    'no timezone' | '2005-05-09T00:00:00'  | '2010-10-01T00:00:00'   | [beginDate: '2005-05-09T00:00:00Z', endDate: '2010-10-01T00:00:00Z', beginYear: 2005, endYear: 2010]
    'paleo'       | '-100000001'           | '-1601050'              | [beginDate: null, endDate: '-1601050-12-31T23:59:59.999Z', beginYear: -100000001, endYear: -1601050]
    'invalid'     | '1984-04-31'           | '1985-505-09T00:00:00Z' | [beginDate: null, endDate: null, beginYear: null, endYear: null]
  }

  ////////////////////////////
  // Keywords               //
  ////////////////////////////
  def "Create GCMD keyword lists"() {
    when:
    Map parsedKeywords = TransformationUtils.prepareGcmdKeyword(TestUtils.inputAvroRecord.discovery)

    then:
    parsedKeywords.gcmdScienceServices == expectedGcmdKeywords.gcmdScienceServices
    parsedKeywords.gcmdScience == expectedGcmdKeywords.gcmdScience
    parsedKeywords.gcmdLocations == expectedGcmdKeywords.gcmdLocations
    parsedKeywords.gcmdInstruments == expectedGcmdKeywords.gcmdInstruments
    parsedKeywords.gcmdPlatforms == expectedGcmdKeywords.gcmdPlatforms
    parsedKeywords.gcmdProjects == expectedGcmdKeywords.gcmdProjects
    parsedKeywords.gcmdDataCenters == expectedGcmdKeywords.gcmdDataCenters
    parsedKeywords.gcmdHorizontalResolution == expectedGcmdKeywords.gcmdHorizontalResolution
    parsedKeywords.gcmdVerticalResolution == expectedGcmdKeywords.gcmdVerticalResolution
    parsedKeywords.gcmdTemporalResolution == expectedGcmdKeywords.gcmdTemporalResolution

    and: "should recreate keywords without accession values"
    parsedKeywords.keywords.size() == expectedKeywords.size()
  }

  def "science keywords are parsed as expected from iso"() {
    def expectedKeywordsFromIso = [
        science       : [
            'Atmosphere > Atmospheric Pressure',
            'Atmosphere',
            'Atmosphere > Atmospheric Temperature',
            'Atmosphere > Atmospheric Water Vapor > Water Vapor Indicators > Humidity > Relative Humidity',
            'Atmosphere > Atmospheric Water Vapor > Water Vapor Indicators > Humidity',
            'Atmosphere > Atmospheric Water Vapor > Water Vapor Indicators',
            'Atmosphere > Atmospheric Water Vapor',
            'Atmosphere > Atmospheric Winds > Surface Winds > Wind Direction',
            'Atmosphere > Atmospheric Winds > Surface Winds',
            'Atmosphere > Atmospheric Winds',
            'Atmosphere > Atmospheric Winds > Surface Winds > Wind Speed',
            'Oceans > Bathymetry/Seafloor Topography > Seafloor Topography',
            'Oceans > Bathymetry/Seafloor Topography',
            'Oceans',
            'Oceans > Bathymetry/Seafloor Topography > Bathymetry',
            'Oceans > Bathymetry/Seafloor Topography > Water Depth',
            'Land Surface > Topography > Terrain Elevation',
            'Land Surface > Topography',
            'Land Surface',
            'Land Surface > Topography > Topographical Relief Maps',
            'Oceans > Coastal Processes > Coastal Elevation',
            'Oceans > Coastal Processes'
        ] as Set,
        scienceService: [
            'Data Analysis And Visualization > Calibration/Validation > Calibration',
            'Data Analysis And Visualization > Calibration/Validation',
            'Data Analysis And Visualization'
        ] as Set
    ]

    when:
    def discovery = TestUtils.inputCollectionRecord.discovery
    def parsedKeywords = TransformationUtils.prepareGcmdKeyword(discovery)

    then:
    parsedKeywords.gcmdScience == expectedKeywordsFromIso.science
    parsedKeywords.gcmdScienceServices == expectedKeywordsFromIso.scienceService
  }

  def "accession values are not included"() {
    when:
    def result = TransformationUtils.reformatMessage(TestUtils.inputAvroRecord, collectionFields)

    then:
    result.accessionValues == null
  }
}
