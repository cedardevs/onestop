import _ from 'lodash';

export const exampleFacets = {
	science: {
		'Atmosphere > Aerosols > Aerosol Optical Depth/Thickness': {
			count: 2,
		},
		'Atmosphere > Atmospheric Winds > Surface Winds > Wind Speed': {
			count: 3,
		},
		'Atmosphere > Precipitation > Precipitation Rate': {
			count: 1,
		},
		'Land Surface > Topography > Terrain Elevation > Topographical Relief Maps': {
			count: 1,
		},
		'Oceans > Bathymetry/Seafloor Topography > Bathymetry': {
			count: 1,
		},
		'Oceans > Bathymetry/Seafloor Topography > Water Depth': {
			count: 1,
		},
		'Oceans > Ocean Temperature > Sea Surface Temperature': {
			count: 6,
		},
		'Oceans > Ocean Temperature > Sea Surface Temperature > Blended Sea Surface Temperature': {
			count: 1,
		},
		'Oceans > Ocean Temperature > Sea Surface Temperature > Bulk Sea Surface Temperature': {
			count: 2,
		},
		'Oceans > Ocean Temperature > Sea Surface Temperature > Skin Sea Surface Temperature': {
			count: 2,
		},
		'Oceans > Ocean Temperature > Sea Surface Temperature > Sub-Skin Sea Surface Temperature': {
			count: 1,
		},
	},
	instruments: {
		'AATSR > Advanced Along-Track Scanning Radiometer': {
			count: 2,
		},
		'AMSR-E > Advanced Microwave Scanning Radiometer-EOS': {
			count: 1,
		},
		'AMSR2 > Advanced Microwave Scanning Radiometer 2': {
			count: 1,
		},
		'AVHRR-3 > Advanced Very High Resolution Radiometer-3': {
			count: 3,
		},
	},
	platforms: {
		'AQUA > Earth Observing System, AQUA': {
			count: 1,
		},
		'ENVISAT > Environmental Satellite': {
			count: 2,
		},
		'GCOM-W1 > Global Change Observation Mission 1st-Water': {
			count: 1,
		},
		'METOP-A > Meteorological Operational Satellite - A': {
			count: 2,
		},
		'METOP-B > Meteorological Operational Satellite - B': {
			count: 1,
		},
		'NOAA-16 > National Oceanic & Atmospheric Administration-16': {
			count: 1,
		},
		'NOAA-17 > National Oceanic & Atmospheric Administration-17': {
			count: 1,
		},
		'NOAA-18 > National Oceanic & Atmospheric Administration-18': {
			count: 1,
		},
		'NOAA-19 > National Oceanic & Atmospheric Administration-19': {
			count: 1,
		},
	},
	projects: {
		'GHRSST > Group for High Resolution Sea Surface Temperature': {
			count: 6,
		},
		'ICSU-WDS > International Council for Science - World Data System': {
			count: 1,
		},
		'NOAA OneStop Project': {
			count: 6,
		},
	},
	dataCenters: {
		'DOC/NOAA/NESDIS/NCDC > National Climatic Data Center, NESDIS, NOAA, U.S. Department of Commerce': {
			count: 1,
		},
		'DOC/NOAA/NESDIS/NCEI > National Centers for Environmental Information, NESDIS, NOAA, U.S. Department of Commerce': {
			count: 7,
		},
		'DOC/NOAA/NESDIS/NGDC > National Geophysical Data Center, NESDIS, NOAA, U.S. Department of Commerce': {
			count: 1,
		},
		'DOC/NOAA/NESDIS/NODC > National Oceanographic Data Center, NESDIS, NOAA, U.S. Department of Commerce': {
			count: 6,
		},
		'NASA/JPL/PODAAC > Physical Oceanography Distributed Active Archive Center, Jet Propulsion Laboratory, NASA': {
			count: 6,
		},
	},
	dataResolution: {},
};

export const exampleFacets2 = {
	science: {
		Agriculture: {
			count: 2,
			children: {
				'Agricultural Aquatic Sciences': {
					count: 1,
					children: {
						Fisheries: {
							count: 1,
							children: {},
							parent: 'Agricultural Aquatic Sciences',
							term: 'Agriculture > Agricultural Aquatic Sciences > Fisheries',
						},
					},
					parent: 'Agriculture',
					term: 'Agriculture > Agricultural Aquatic Sciences',
				},
				'Animal Science': {
					count: 2,
					children: {
						'Animal Ecology And Behavior': {
							count: 2,
							children: {},
							parent: 'Animal Science',
							term:
								'Agriculture > Animal Science > Animal Ecology And Behavior',
						},
					},
					parent: 'Agriculture',
					term: 'Agriculture > Animal Science',
				},
			},
			parent: null,
			term: 'Agriculture',
		},
		Atmosphere: {
			count: 62,
			children: {
				Aerosols: {
					count: 13,
					children: {
						'Aerosol Optical Depth/Thickness': {
							count: 13,
							children: {},
							parent: 'Aerosols',
							term: 'Atmosphere > Aerosols > Aerosol Optical Depth/Thickness',
						},
					},
					parent: 'Atmosphere',
					term: 'Atmosphere > Aerosols',
				},
				'Atmospheric Chemistry': {
					count: 2,
					children: {
						'Carbon And Hydrocarbon Compounds': {
							count: 2,
							children: {
								'Carbon Dioxide': {
									count: 2,
									children: {},
									parent: 'Carbon And Hydrocarbon Compounds',
									term:
										'Atmosphere > Atmospheric Chemistry > Carbon And Hydrocarbon Compounds > Carbon Dioxide',
								},
							},
							parent: 'Atmospheric Chemistry',
							term:
								'Atmosphere > Atmospheric Chemistry > Carbon And Hydrocarbon Compounds',
						},
					},
					parent: 'Atmosphere',
					term: 'Atmosphere > Atmospheric Chemistry',
				},
				'Atmospheric Pressure': {
					count: 42,
					children: {},
					parent: 'Atmosphere',
					term: 'Atmosphere > Atmospheric Pressure',
				},
				'Atmospheric Radiation': {
					count: 7,
					children: {
						'Incoming Solar Radiation': {
							count: 7,
							children: {},
							parent: 'Atmospheric Radiation',
							term:
								'Atmosphere > Atmospheric Radiation > Incoming Solar Radiation',
						},
					},
					parent: 'Atmosphere',
					term: 'Atmosphere > Atmospheric Radiation',
				},
				'Atmospheric Temperature': {
					count: 46,
					children: {
						'Surface Temperature': {
							count: 36,
							children: {
								'Air Temperature': {
									count: 34,
									children: {},
									parent: 'Surface Temperature',
									term:
										'Atmosphere > Atmospheric Temperature > Surface Temperature > Air Temperature',
								},
								'Dew Point Temperature': {
									count: 3,
									children: {},
									parent: 'Surface Temperature',
									term:
										'Atmosphere > Atmospheric Temperature > Surface Temperature > Dew Point Temperature',
								},
							},
							parent: 'Atmospheric Temperature',
							term:
								'Atmosphere > Atmospheric Temperature > Surface Temperature',
						},
					},
					parent: 'Atmosphere',
					term: 'Atmosphere > Atmospheric Temperature',
				},
				'Atmospheric Water Vapor': {
					count: 6,
					children: {
						Humidity: {
							count: 5,
							children: {},
							parent: 'Atmospheric Water Vapor',
							term: 'Atmosphere > Atmospheric Water Vapor > Humidity',
						},
						'Water Vapor Indicators': {
							count: 2,
							children: {
								Humidity: {
									count: 2,
									children: {
										'Relative Humidity': {
											count: 2,
											children: {},
											parent: 'Humidity',
											term:
												'Atmosphere > Atmospheric Water Vapor > Water Vapor Indicators > Humidity > Relative Humidity',
										},
									},
									parent: 'Water Vapor Indicators',
									term:
										'Atmosphere > Atmospheric Water Vapor > Water Vapor Indicators > Humidity',
								},
							},
							parent: 'Atmospheric Water Vapor',
							term:
								'Atmosphere > Atmospheric Water Vapor > Water Vapor Indicators',
						},
					},
					parent: 'Atmosphere',
					term: 'Atmosphere > Atmospheric Water Vapor',
				},
				'Atmospheric Winds': {
					count: 61,
					children: {
						'Surface Winds': {
							count: 61,
							children: {
								'U/V Wind Components': {
									count: 1,
									children: {},
									parent: 'Surface Winds',
									term:
										'Atmosphere > Atmospheric Winds > Surface Winds > U/V Wind Components',
								},
								'Wind Direction': {
									count: 9,
									children: {},
									parent: 'Surface Winds',
									term:
										'Atmosphere > Atmospheric Winds > Surface Winds > Wind Direction',
								},
								'Wind Speed': {
									count: 24,
									children: {},
									parent: 'Surface Winds',
									term:
										'Atmosphere > Atmospheric Winds > Surface Winds > Wind Speed',
								},
								'Wind Speed/Wind Direction': {
									count: 38,
									children: {},
									parent: 'Surface Winds',
									term:
										'Atmosphere > Atmospheric Winds > Surface Winds > Wind Speed/Wind Direction',
								},
							},
							parent: 'Atmospheric Winds',
							term: 'Atmosphere > Atmospheric Winds > Surface Winds',
						},
					},
					parent: 'Atmosphere',
					term: 'Atmosphere > Atmospheric Winds',
				},
				Clouds: {
					count: 42,
					children: {
						'Cloud Properties': {
							count: 42,
							children: {
								'Cloud Frequency': {
									count: 42,
									children: {},
									parent: 'Cloud Properties',
									term:
										'Atmosphere > Clouds > Cloud Properties > Cloud Frequency',
								},
							},
							parent: 'Clouds',
							term: 'Atmosphere > Clouds > Cloud Properties',
						},
						'Cloud Types': {
							count: 36,
							children: {},
							parent: 'Clouds',
							term: 'Atmosphere > Clouds > Cloud Types',
						},
					},
					parent: 'Atmosphere',
					term: 'Atmosphere > Clouds',
				},
				Precipitation: {
					count: 3,
					children: {},
					parent: 'Atmosphere',
					term: 'Atmosphere > Precipitation',
				},
			},
			parent: null,
			term: 'Atmosphere',
		},
		'Biological Classification': {
			count: 6,
			children: {
				'Animals/Invertebrates': {
					count: 1,
					children: {},
					parent: 'Biological Classification',
					term: 'Biological Classification > Animals/Invertebrates',
				},
				'Animals/Vertebrates': {
					count: 5,
					children: {
						Birds: {
							count: 2,
							children: {},
							parent: 'Animals/Vertebrates',
							term: 'Biological Classification > Animals/Vertebrates > Birds',
						},
						Fish: {
							count: 3,
							children: {},
							parent: 'Animals/Vertebrates',
							term: 'Biological Classification > Animals/Vertebrates > Fish',
						},
						Mammals: {
							count: 2,
							children: {
								Cetaceans: {
									count: 1,
									children: {},
									parent: 'Mammals',
									term:
										'Biological Classification > Animals/Vertebrates > Mammals > Cetaceans',
								},
							},
							parent: 'Animals/Vertebrates',
							term: 'Biological Classification > Animals/Vertebrates > Mammals',
						},
						Reptiles: {
							count: 1,
							children: {},
							parent: 'Animals/Vertebrates',
							term:
								'Biological Classification > Animals/Vertebrates > Reptiles',
						},
					},
					parent: 'Biological Classification',
					term: 'Biological Classification > Animals/Vertebrates',
				},
				'Bacteria/Archaea': {
					count: 1,
					children: {},
					parent: 'Biological Classification',
					term: 'Biological Classification > Bacteria/Archaea',
				},
				Plants: {
					count: 1,
					children: {
						Microalgae: {
							count: 1,
							children: {
								Diatoms: {
									count: 1,
									children: {},
									parent: 'Microalgae',
									term:
										'Biological Classification > Plants > Microalgae > Diatoms',
								},
							},
							parent: 'Plants',
							term: 'Biological Classification > Plants > Microalgae',
						},
					},
					parent: 'Biological Classification',
					term: 'Biological Classification > Plants',
				},
				Protists: {
					count: 3,
					children: {
						Plankton: {
							count: 3,
							children: {
								Phytoplankton: {
									count: 3,
									children: {},
									parent: 'Plankton',
									term:
										'Biological Classification > Protists > Plankton > Phytoplankton',
								},
							},
							parent: 'Protists',
							term: 'Biological Classification > Protists > Plankton',
						},
					},
					parent: 'Biological Classification',
					term: 'Biological Classification > Protists',
				},
			},
			parent: null,
			term: 'Biological Classification',
		},
		Biosphere: {
			count: 5,
			children: {
				'Ecological Dynamics': {
					count: 2,
					children: {
						'Ecosystem Functions': {
							count: 2,
							children: {
								'Primary Production': {
									count: 2,
									children: {},
									parent: 'Ecosystem Functions',
									term:
										'Biosphere > Ecological Dynamics > Ecosystem Functions > Primary Production',
								},
							},
							parent: 'Ecological Dynamics',
							term: 'Biosphere > Ecological Dynamics > Ecosystem Functions',
						},
					},
					parent: 'Biosphere',
					term: 'Biosphere > Ecological Dynamics',
				},
				Ecosystems: {
					count: 5,
					children: {
						'Aquatic Ecosystems': {
							count: 3,
							children: {
								Plankton: {
									count: 3,
									children: {
										Zooplankton: {
											count: 3,
											children: {},
											parent: 'Plankton',
											term:
												'Biosphere > Ecosystems > Aquatic Ecosystems > Plankton > Zooplankton',
										},
									},
									parent: 'Aquatic Ecosystems',
									term:
										'Biosphere > Ecosystems > Aquatic Ecosystems > Plankton',
								},
							},
							parent: 'Ecosystems',
							term: 'Biosphere > Ecosystems > Aquatic Ecosystems',
						},
						'Marine Ecosystems': {
							count: 3,
							children: {
								Benthic: {
									count: 3,
									children: {},
									parent: 'Marine Ecosystems',
									term: 'Biosphere > Ecosystems > Marine Ecosystems > Benthic',
								},
							},
							parent: 'Ecosystems',
							term: 'Biosphere > Ecosystems > Marine Ecosystems',
						},
					},
					parent: 'Biosphere',
					term: 'Biosphere > Ecosystems',
				},
				Vegetation: {
					count: 1,
					children: {
						Biomass: {
							count: 1,
							children: {},
							parent: 'Vegetation',
							term: 'Biosphere > Vegetation > Biomass',
						},
					},
					parent: 'Biosphere',
					term: 'Biosphere > Vegetation',
				},
			},
			parent: null,
			term: 'Biosphere',
		},
		Cryosphere: {
			count: 1,
			children: {
				'Sea Ice': {
					count: 1,
					children: {
						'Ice Depth/Thickness': {
							count: 1,
							children: {},
							parent: 'Sea Ice',
							term: 'Cryosphere > Sea Ice > Ice Depth/Thickness',
						},
					},
					parent: 'Cryosphere',
					term: 'Cryosphere > Sea Ice',
				},
				'Snow/Ice': {
					count: 1,
					children: {
						Albedo: {
							count: 1,
							children: {},
							parent: 'Snow/Ice',
							term: 'Cryosphere > Snow/Ice > Albedo',
						},
						'Snow Depth': {
							count: 1,
							children: {},
							parent: 'Snow/Ice',
							term: 'Cryosphere > Snow/Ice > Snow Depth',
						},
						'Snow Water Equivalent': {
							count: 1,
							children: {},
							parent: 'Snow/Ice',
							term: 'Cryosphere > Snow/Ice > Snow Water Equivalent',
						},
						'Snow/Ice Temperature': {
							count: 1,
							children: {},
							parent: 'Snow/Ice',
							term: 'Cryosphere > Snow/Ice > Snow/Ice Temperature',
						},
					},
					parent: 'Cryosphere',
					term: 'Cryosphere > Snow/Ice',
				},
			},
			parent: null,
			term: 'Cryosphere',
		},
		'Human Dimensions': {
			count: 1,
			children: {},
			parent: null,
			term: 'Human Dimensions',
		},
		'Land Surface': {
			count: 1,
			children: {
				'Geomorphic Landforms/Processes': {
					count: 1,
					children: {},
					parent: 'Land Surface',
					term: 'Land Surface > Geomorphic Landforms/Processes',
				},
			},
			parent: null,
			term: 'Land Surface',
		},
		Oceans: {
			count: 108,
			children: {
				'Bathymetry/Seafloor Topography': {
					count: 13,
					children: {
						'Water Depth': {
							count: 13,
							children: {},
							parent: 'Bathymetry/Seafloor Topography',
							term: 'Oceans > Bathymetry/Seafloor Topography > Water Depth',
						},
					},
					parent: 'Oceans',
					term: 'Oceans > Bathymetry/Seafloor Topography',
				},
				'Coastal Processes': {
					count: 2,
					children: {
						'Coral Reefs': {
							count: 2,
							children: {
								'Coral Bleaching': {
									count: 2,
									children: {},
									parent: 'Coral Reefs',
									term:
										'Oceans > Coastal Processes > Coral Reefs > Coral Bleaching',
								},
							},
							parent: 'Coastal Processes',
							term: 'Oceans > Coastal Processes > Coral Reefs',
						},
					},
					parent: 'Oceans',
					term: 'Oceans > Coastal Processes',
				},
				'Marine Sediments': {
					count: 3,
					children: {
						'Sediment Chemistry': {
							count: 1,
							children: {},
							parent: 'Marine Sediments',
							term: 'Oceans > Marine Sediments > Sediment Chemistry',
						},
					},
					parent: 'Oceans',
					term: 'Oceans > Marine Sediments',
				},
				'Ocean Acoustics': {
					count: 23,
					children: {
						'Acoustic Velocity': {
							count: 23,
							children: {},
							parent: 'Ocean Acoustics',
							term: 'Oceans > Ocean Acoustics > Acoustic Velocity',
						},
					},
					parent: 'Oceans',
					term: 'Oceans > Ocean Acoustics',
				},
				'Ocean Chemistry': {
					count: 46,
					children: {
						Alkalinity: {
							count: 11,
							children: {},
							parent: 'Ocean Chemistry',
							term: 'Oceans > Ocean Chemistry > Alkalinity',
						},
						Ammonia: {
							count: 8,
							children: {},
							parent: 'Ocean Chemistry',
							term: 'Oceans > Ocean Chemistry > Ammonia',
						},
						Carbon: {
							count: 7,
							children: {},
							parent: 'Ocean Chemistry',
							term: 'Oceans > Ocean Chemistry > Carbon',
						},
						Chlorophyll: {
							count: 14,
							children: {},
							parent: 'Ocean Chemistry',
							term: 'Oceans > Ocean Chemistry > Chlorophyll',
						},
						Hydrocarbons: {
							count: 6,
							children: {},
							parent: 'Ocean Chemistry',
							term: 'Oceans > Ocean Chemistry > Hydrocarbons',
						},
						'Inorganic Carbon': {
							count: 1,
							children: {},
							parent: 'Ocean Chemistry',
							term: 'Oceans > Ocean Chemistry > Inorganic Carbon',
						},
						Nitrate: {
							count: 27,
							children: {},
							parent: 'Ocean Chemistry',
							term: 'Oceans > Ocean Chemistry > Nitrate',
						},
						Nitrite: {
							count: 15,
							children: {},
							parent: 'Ocean Chemistry',
							term: 'Oceans > Ocean Chemistry > Nitrite',
						},
						Nitrogen: {
							count: 3,
							children: {},
							parent: 'Ocean Chemistry',
							term: 'Oceans > Ocean Chemistry > Nitrogen',
						},
						Nutrients: {
							count: 3,
							children: {},
							parent: 'Ocean Chemistry',
							term: 'Oceans > Ocean Chemistry > Nutrients',
						},
						'Organic Carbon': {
							count: 6,
							children: {},
							parent: 'Ocean Chemistry',
							term: 'Oceans > Ocean Chemistry > Organic Carbon',
						},
						'Organic Matter': {
							count: 3,
							children: {},
							parent: 'Ocean Chemistry',
							term: 'Oceans > Ocean Chemistry > Organic Matter',
						},
						Oxygen: {
							count: 44,
							children: {},
							parent: 'Ocean Chemistry',
							term: 'Oceans > Ocean Chemistry > Oxygen',
						},
						Ph: {
							count: 27,
							children: {},
							parent: 'Ocean Chemistry',
							term: 'Oceans > Ocean Chemistry > Ph',
						},
						Phosphate: {
							count: 29,
							children: {},
							parent: 'Ocean Chemistry',
							term: 'Oceans > Ocean Chemistry > Phosphate',
						},
						Pigments: {
							count: 10,
							children: {
								Chlorophyll: {
									count: 9,
									children: {},
									parent: 'Pigments',
									term: 'Oceans > Ocean Chemistry > Pigments > Chlorophyll',
								},
							},
							parent: 'Ocean Chemistry',
							term: 'Oceans > Ocean Chemistry > Pigments',
						},
						Silicate: {
							count: 27,
							children: {},
							parent: 'Ocean Chemistry',
							term: 'Oceans > Ocean Chemistry > Silicate',
						},
						'Suspended Solids': {
							count: 7,
							children: {},
							parent: 'Ocean Chemistry',
							term: 'Oceans > Ocean Chemistry > Suspended Solids',
						},
					},
					parent: 'Oceans',
					term: 'Oceans > Ocean Chemistry',
				},
				'Ocean Circulation': {
					count: 7,
					children: {
						'Ocean Currents': {
							count: 7,
							children: {},
							parent: 'Ocean Circulation',
							term: 'Oceans > Ocean Circulation > Ocean Currents',
						},
					},
					parent: 'Oceans',
					term: 'Oceans > Ocean Circulation',
				},
				'Ocean Heat Budget': {
					count: 1,
					children: {
						'Longwave Radiation': {
							count: 1,
							children: {},
							parent: 'Ocean Heat Budget',
							term: 'Oceans > Ocean Heat Budget > Longwave Radiation',
						},
						'Shortwave Radiation': {
							count: 1,
							children: {},
							parent: 'Ocean Heat Budget',
							term: 'Oceans > Ocean Heat Budget > Shortwave Radiation',
						},
					},
					parent: 'Oceans',
					term: 'Oceans > Ocean Heat Budget',
				},
				'Ocean Optics': {
					count: 43,
					children: {
						'Attenuation/Transmission': {
							count: 15,
							children: {},
							parent: 'Ocean Optics',
							term: 'Oceans > Ocean Optics > Attenuation/Transmission',
						},
						Fluorescence: {
							count: 4,
							children: {},
							parent: 'Ocean Optics',
							term: 'Oceans > Ocean Optics > Fluorescence',
						},
						Irradiance: {
							count: 1,
							children: {},
							parent: 'Ocean Optics',
							term: 'Oceans > Ocean Optics > Irradiance',
						},
						'Ocean Color': {
							count: 19,
							children: {},
							parent: 'Ocean Optics',
							term: 'Oceans > Ocean Optics > Ocean Color',
						},
						'Photosynthetically Active Radiation': {
							count: 1,
							children: {},
							parent: 'Ocean Optics',
							term:
								'Oceans > Ocean Optics > Photosynthetically Active Radiation',
						},
						Reflectance: {
							count: 1,
							children: {},
							parent: 'Ocean Optics',
							term: 'Oceans > Ocean Optics > Reflectance',
						},
						'Secchi Depth': {
							count: 19,
							children: {},
							parent: 'Ocean Optics',
							term: 'Oceans > Ocean Optics > Secchi Depth',
						},
						Turbidity: {
							count: 8,
							children: {},
							parent: 'Ocean Optics',
							term: 'Oceans > Ocean Optics > Turbidity',
						},
					},
					parent: 'Oceans',
					term: 'Oceans > Ocean Optics',
				},
				'Ocean Pressure': {
					count: 21,
					children: {
						'Water Pressure': {
							count: 21,
							children: {},
							parent: 'Ocean Pressure',
							term: 'Oceans > Ocean Pressure > Water Pressure',
						},
					},
					parent: 'Oceans',
					term: 'Oceans > Ocean Pressure',
				},
				'Ocean Temperature': {
					count: 101,
					children: {
						'Potential Temperature': {
							count: 1,
							children: {},
							parent: 'Ocean Temperature',
							term: 'Oceans > Ocean Temperature > Potential Temperature',
						},
						'Sea Surface Temperature': {
							count: 57,
							children: {
								'Blended Sea Surface Temperature': {
									count: 2,
									children: {},
									parent: 'Sea Surface Temperature',
									term:
										'Oceans > Ocean Temperature > Sea Surface Temperature > Blended Sea Surface Temperature',
								},
								'Bulk Sea Surface Temperature': {
									count: 15,
									children: {},
									parent: 'Sea Surface Temperature',
									term:
										'Oceans > Ocean Temperature > Sea Surface Temperature > Bulk Sea Surface Temperature',
								},
								'Foundation Sea Surface Temperature': {
									count: 12,
									children: {},
									parent: 'Sea Surface Temperature',
									term:
										'Oceans > Ocean Temperature > Sea Surface Temperature > Foundation Sea Surface Temperature',
								},
								'Foundation Sea Surface Temperature (Sstfnd)': {
									count: 4,
									children: {},
									parent: 'Sea Surface Temperature',
									term:
										'Oceans > Ocean Temperature > Sea Surface Temperature > Foundation Sea Surface Temperature (Sstfnd)',
								},
								'Sea Surface Skin Temperature': {
									count: 2,
									children: {},
									parent: 'Sea Surface Temperature',
									term:
										'Oceans > Ocean Temperature > Sea Surface Temperature > Sea Surface Skin Temperature',
								},
								'Sea Surface Skin Temperature (Sstskin)': {
									count: 3,
									children: {},
									parent: 'Sea Surface Temperature',
									term:
										'Oceans > Ocean Temperature > Sea Surface Temperature > Sea Surface Skin Temperature (Sstskin)',
								},
								'Sea Surface Temperature 1m': {
									count: 1,
									children: {},
									parent: 'Sea Surface Temperature',
									term:
										'Oceans > Ocean Temperature > Sea Surface Temperature > Sea Surface Temperature 1m',
								},
								'Skin Sea Surface Temperature': {
									count: 4,
									children: {},
									parent: 'Sea Surface Temperature',
									term:
										'Oceans > Ocean Temperature > Sea Surface Temperature > Skin Sea Surface Temperature',
								},
								'Skin Sea Surface Temperature (Sstskin)': {
									count: 1,
									children: {},
									parent: 'Sea Surface Temperature',
									term:
										'Oceans > Ocean Temperature > Sea Surface Temperature > Skin Sea Surface Temperature (Sstskin)',
								},
								'Subskin Sea Surface Temperature': {
									count: 6,
									children: {},
									parent: 'Sea Surface Temperature',
									term:
										'Oceans > Ocean Temperature > Sea Surface Temperature > Subskin Sea Surface Temperature',
								},
							},
							parent: 'Ocean Temperature',
							term: 'Oceans > Ocean Temperature > Sea Surface Temperature',
						},
						Thermocline: {
							count: 1,
							children: {},
							parent: 'Ocean Temperature',
							term: 'Oceans > Ocean Temperature > Thermocline',
						},
						'Water Temperature': {
							count: 49,
							children: {},
							parent: 'Ocean Temperature',
							term: 'Oceans > Ocean Temperature > Water Temperature',
						},
					},
					parent: 'Oceans',
					term: 'Oceans > Ocean Temperature',
				},
				'Ocean Waves': {
					count: 29,
					children: {
						'Sea State': {
							count: 2,
							children: {},
							parent: 'Ocean Waves',
							term: 'Oceans > Ocean Waves > Sea State',
						},
						Swells: {
							count: 2,
							children: {},
							parent: 'Ocean Waves',
							term: 'Oceans > Ocean Waves > Swells',
						},
						'Wave Height': {
							count: 25,
							children: {},
							parent: 'Ocean Waves',
							term: 'Oceans > Ocean Waves > Wave Height',
						},
						'Wave Period': {
							count: 24,
							children: {},
							parent: 'Ocean Waves',
							term: 'Oceans > Ocean Waves > Wave Period',
						},
						'Wave Speed/Direction': {
							count: 10,
							children: {},
							parent: 'Ocean Waves',
							term: 'Oceans > Ocean Waves > Wave Speed/Direction',
						},
					},
					parent: 'Oceans',
					term: 'Oceans > Ocean Waves',
				},
				'Ocean Winds': {
					count: 1,
					children: {
						'Wind Stress': {
							count: 1,
							children: {},
							parent: 'Ocean Winds',
							term: 'Oceans > Ocean Winds > Wind Stress',
						},
					},
					parent: 'Oceans',
					term: 'Oceans > Ocean Winds',
				},
				'Salinity/Density': {
					count: 45,
					children: {
						Conductivity: {
							count: 21,
							children: {},
							parent: 'Salinity/Density',
							term: 'Oceans > Salinity/Density > Conductivity',
						},
						Density: {
							count: 35,
							children: {},
							parent: 'Salinity/Density',
							term: 'Oceans > Salinity/Density > Density',
						},
						Salinity: {
							count: 45,
							children: {},
							parent: 'Salinity/Density',
							term: 'Oceans > Salinity/Density > Salinity',
						},
					},
					parent: 'Oceans',
					term: 'Oceans > Salinity/Density',
				},
				'Sea Ice': {
					count: 25,
					children: {
						'Ice Deformation': {
							count: 2,
							children: {},
							parent: 'Sea Ice',
							term: 'Oceans > Sea Ice > Ice Deformation',
						},
						'Ice Extent': {
							count: 24,
							children: {},
							parent: 'Sea Ice',
							term: 'Oceans > Sea Ice > Ice Extent',
						},
						'Ice Types': {
							count: 5,
							children: {},
							parent: 'Sea Ice',
							term: 'Oceans > Sea Ice > Ice Types',
						},
						'Sea Ice Concentration': {
							count: 1,
							children: {
								'Sea Ice Fraction': {
									count: 1,
									children: {},
									parent: 'Sea Ice Concentration',
									term:
										'Oceans > Sea Ice > Sea Ice Concentration > Sea Ice Fraction',
								},
							},
							parent: 'Sea Ice',
							term: 'Oceans > Sea Ice > Sea Ice Concentration',
						},
						'Sea Ice Motion': {
							count: 2,
							children: {},
							parent: 'Sea Ice',
							term: 'Oceans > Sea Ice > Sea Ice Motion',
						},
					},
					parent: 'Oceans',
					term: 'Oceans > Sea Ice',
				},
				Tides: {
					count: 8,
					children: {
						'Tidal Height': {
							count: 7,
							children: {},
							parent: 'Tides',
							term: 'Oceans > Tides > Tidal Height',
						},
					},
					parent: 'Oceans',
					term: 'Oceans > Tides',
				},
			},
			parent: null,
			term: 'Oceans',
		},
	},
	instruments: {
		AATSR: {
			count: 10,
			children: {},
			parent: null,
			term: 'AATSR > Advanced Along-Track Scanning Radiometer',
		},
		ADCP: {
			count: 6,
			children: {},
			parent: null,
			term: 'ADCP > Acoustic Doppler Current Profiler',
		},
		'AMSR-E': {
			count: 14,
			children: {},
			parent: null,
			term: 'AMSR-E > Advanced Microwave Scanning Radiometer-EOS',
		},
		AMSR2: {
			count: 7,
			children: {},
			parent: null,
			term: 'AMSR2 > Advanced Microwave Scanning Radiometer 2',
		},
		ANEMOMETERS: {
			count: 2,
			children: {},
			parent: null,
			term: 'ANEMOMETERS',
		},
		ATSR: {
			count: 1,
			children: {},
			parent: null,
			term: 'ATSR > Along Track Scanning Radiometer and Microwave Sounder',
		},
		'ATSR-2': {
			count: 1,
			children: {},
			parent: null,
			term: 'ATSR-2 > Along-Track Scanning Radiometer 2',
		},
		'AVHRR-2': {
			count: 2,
			children: {},
			parent: null,
			term: 'AVHRR-2 > Advanced Very High Resolution Radiometer-2',
		},
		'AVHRR-3': {
			count: 39,
			children: {},
			parent: null,
			term: 'AVHRR-3 > Advanced Very High Resolution Radiometer-3',
		},
		AWQT: {
			count: 1,
			children: {},
			parent: null,
			term: 'AWQT > Apparatus for Water Quality Test',
		},
		AWS: {
			count: 1,
			children: {},
			parent: null,
			term: 'AWS > Automated Weather System',
		},
		BAROMETERS: {
			count: 2,
			children: {},
			parent: null,
			term: 'BAROMETERS',
		},
		BATHYTHERMOGRAPHS: {
			count: 2,
			children: {},
			parent: null,
			term: 'BATHYTHERMOGRAPHS',
		},
		BINOCULAR: {
			count: 2,
			children: {},
			parent: null,
			term: 'BINOCULAR > BINOCULAR',
		},
		'CHN ANALYZERS': {
			count: 1,
			children: {},
			parent: null,
			term: 'CHN ANALYZERS > Carbon, Hydrogen, Nitrogen Analyzers',
		},
		COMPASSES: {
			count: 1,
			children: {},
			parent: null,
			term: 'COMPASSES',
		},
		CTD: {
			count: 20,
			children: {},
			parent: null,
			term: 'CTD > Conductivity, Temperature, Depth',
		},
		'CURRENT METERS': {
			count: 4,
			children: {},
			parent: null,
			term: 'CURRENT METERS',
		},
		'ECHO SOUNDERS': {
			count: 1,
			children: {},
			parent: null,
			term: 'ECHO SOUNDERS',
		},
		'FLOW CYTOMETRY': {
			count: 1,
			children: {},
			parent: null,
			term: 'FLOW CYTOMETRY',
		},
		'FLUORESCENCE MICROSCOPY': {
			count: 1,
			children: {},
			parent: null,
			term: 'FLUORESCENCE MICROSCOPY',
		},
		FLUOROMETERS: {
			count: 7,
			children: {},
			parent: null,
			term: 'FLUOROMETERS',
		},
		FRRF: {
			count: 1,
			children: {},
			parent: null,
			term: 'FRRF > Fast Repetition Rate Fluorometer',
		},
		'GOES-13 Imager': {
			count: 10,
			children: {},
			parent: null,
			term: 'GOES-13 Imager',
		},
		GPS: {
			count: 4,
			children: {},
			parent: null,
			term: 'GPS > Global Positioning System',
		},
		HYDROMETERS: {
			count: 1,
			children: {},
			parent: null,
			term: 'HYDROMETERS',
		},
		IASI: {
			count: 1,
			children: {},
			parent: null,
			term: 'IASI > Infrared Atmospheric Sounding Interferometer',
		},
		'ICE AUGERS': {
			count: 1,
			children: {},
			parent: null,
			term: 'ICE AUGERS',
		},
		INCLINOMETERS: {
			count: 1,
			children: {},
			parent: null,
			term: 'INCLINOMETERS',
		},
		MOCNESS: {
			count: 2,
			children: {},
			parent: null,
			term: 'MOCNESS > MOCNESS Plankton Net',
		},
		MODIS: {
			count: 2,
			children: {},
			parent: null,
			term: 'MODIS > Moderate-Resolution Imaging Spectroradiometer',
		},
		'MTSAT 1R Imager': {
			count: 1,
			children: {},
			parent: null,
			term: 'MTSAT 1R Imager',
		},
		'MTSAT 2 Imager': {
			count: 3,
			children: {},
			parent: null,
			term: 'MTSAT 2 Imager',
		},
		NETS: {
			count: 3,
			children: {},
			parent: null,
			term: 'NETS',
		},
		OPC: {
			count: 1,
			children: {},
			parent: null,
			term: 'OPC > Optical Plankton Counter',
		},
		OPTSPEC: {
			count: 1,
			children: {},
			parent: null,
			term: 'OPTSPEC > Optical Spectrometer',
		},
		'OXYGEN METERS': {
			count: 4,
			children: {},
			parent: null,
			term: 'OXYGEN METERS',
		},
		'PLANKTON NETS': {
			count: 4,
			children: {},
			parent: null,
			term: 'PLANKTON NETS',
		},
		'PRESSURE GAUGES': {
			count: 2,
			children: {},
			parent: null,
			term: 'PRESSURE GAUGES',
		},
		RADIOMETERS: {
			count: 1,
			children: {},
			parent: null,
			term: 'RADIOMETERS',
		},
		SALINOMETERS: {
			count: 3,
			children: {},
			parent: null,
			term: 'SALINOMETERS',
		},
		SCANFISH: {
			count: 2,
			children: {},
			parent: null,
			term: 'SCANFISH > SHIP-TOWED UNDULATING VEHICLE',
		},
		'SCINTILLATION COUNTERS': {
			count: 1,
			children: {},
			parent: null,
			term: 'SCINTILLATION COUNTERS',
		},
		SEAWIFS: {
			count: 1,
			children: {},
			parent: null,
			term: 'SEAWIFS > Sea-Viewing Wide Field-of-View Sensor',
		},
		'SECCHI DISKS': {
			count: 33,
			children: {},
			parent: null,
			term: 'SECCHI DISKS',
		},
		'SEDIMENT CORERS': {
			count: 1,
			children: {},
			parent: null,
			term: 'SEDIMENT CORERS',
		},
		SEVIRI: {
			count: 11,
			children: {},
			parent: null,
			term: 'SEVIRI > Spinning Enhanced Visible and Infrared Imager',
		},
		'SSM/I': {
			count: 3,
			children: {},
			parent: null,
			term: 'SSM/I > Special Sensor Microwave/Imager',
		},
		'TEMPERATURE SENSORS': {
			count: 1,
			children: {},
			parent: null,
			term: 'TEMPERATURE SENSORS',
		},
		THERMISTORS: {
			count: 5,
			children: {},
			parent: null,
			term: 'THERMISTORS',
		},
		THERMOMETERS: {
			count: 8,
			children: {},
			parent: null,
			term: 'THERMOMETERS',
		},
		THERMOSALINOGRAPHS: {
			count: 2,
			children: {},
			parent: null,
			term: 'THERMOSALINOGRAPHS',
		},
		'TIDE GAUGES': {
			count: 2,
			children: {},
			parent: null,
			term: 'TIDE GAUGES',
		},
		TMI: {
			count: 9,
			children: {},
			parent: null,
			term: 'TMI > TRMM Microwave Imager',
		},
		TOC: {
			count: 1,
			children: {},
			parent: null,
			term: 'TOC > Total Organic Carbon Analyzer',
		},
		TRANSMISSOMETERS: {
			count: 15,
			children: {},
			parent: null,
			term: 'TRANSMISSOMETERS',
		},
		TRAPS: {
			count: 1,
			children: {},
			parent: null,
			term: 'TRAPS',
		},
		TRAWL: {
			count: 3,
			children: {},
			parent: null,
			term: 'TRAWL',
		},
		'TURBIDITY METERS': {
			count: 1,
			children: {},
			parent: null,
			term: 'TURBIDITY METERS',
		},
		VIIRS: {
			count: 7,
			children: {},
			parent: null,
			term: 'VIIRS > Visible-Infrared Imager-Radiometer Suite',
		},
		'VISUAL OBSERVATIONS': {
			count: 12,
			children: {},
			parent: null,
			term: 'VISUAL OBSERVATIONS',
		},
		WINDSAT: {
			count: 5,
			children: {},
			parent: null,
			term: 'WINDSAT',
		},
		XBT: {
			count: 4,
			children: {},
			parent: null,
			term: 'XBT > Expendable Bathythermographs',
		},
	},
	platforms: {
		AIRCRAFT: {
			count: 7,
			children: {},
			parent: null,
			term: 'AIRCRAFT',
		},
		AQUA: {
			count: 14,
			children: {},
			parent: null,
			term: 'AQUA > Earth Observing System, AQUA',
		},
		BUOYS: {
			count: 6,
			children: {},
			parent: null,
			term: 'BUOYS',
		},
		CORIOLIS: {
			count: 5,
			children: {},
			parent: null,
			term: 'CORIOLIS > Coriolis',
		},
		'DMSP 5D-2/F15': {
			count: 3,
			children: {},
			parent: null,
			term: 'DMSP 5D-2/F15 > Defense Meteorological Satellite Program-F15',
		},
		'DMSP 5D-3/F17': {
			count: 1,
			children: {},
			parent: null,
			term: 'DMSP 5D-3/F17 > Defense Meteorological Satellite Program-F17',
		},
		'DMSP 5D-3/F18': {
			count: 1,
			children: {},
			parent: null,
			term: 'DMSP 5D-3/F18 > Defense Meteorological Satellite Program-F18',
		},
		ENVISAT: {
			count: 10,
			children: {},
			parent: null,
			term: 'ENVISAT > Environmental Satellite',
		},
		'ERS-1': {
			count: 1,
			children: {},
			parent: null,
			term: 'ERS-1 > European Remote Sensing Satellite-1',
		},
		'ERS-2': {
			count: 1,
			children: {},
			parent: null,
			term: 'ERS-2 > European Remote Sensing Satellite-2',
		},
		'GCOM-W1': {
			count: 7,
			children: {},
			parent: null,
			term: 'GCOM-W1 > Global Change Observation Mission 1st-Water',
		},
		'GOES-11': {
			count: 3,
			children: {},
			parent: null,
			term: 'GOES-11 > Geostationary Operational Environmental Satellite 11',
		},
		'GOES-12': {
			count: 2,
			children: {},
			parent: null,
			term: 'GOES-12 > Geostationary Operational Environmental Satellite 12',
		},
		'GOES-13': {
			count: 5,
			children: {},
			parent: null,
			term: 'GOES-13 > Geostationary Operational Environmental Satellite 13',
		},
		'GOES-15': {
			count: 3,
			children: {},
			parent: null,
			term: 'GOES-15 > Geostationary Operational Environmental Satellite 15',
		},
		'METEOSAT-10': {
			count: 3,
			children: {},
			parent: null,
			term: 'METEOSAT-10',
		},
		'METEOSAT-9': {
			count: 3,
			children: {},
			parent: null,
			term: 'METEOSAT-9',
		},
		'METOP-A': {
			count: 17,
			children: {},
			parent: null,
			term: 'METOP-A > Meteorological Operational Satellite - A',
		},
		'METOP-B': {
			count: 10,
			children: {},
			parent: null,
			term: 'METOP-B > Meteorological Operational Satellite - B',
		},
		MSG: {
			count: 8,
			children: {},
			parent: null,
			term: 'MSG > Meteosat Second Generation',
		},
		'MTSAT-1R': {
			count: 1,
			children: {},
			parent: null,
			term: 'MTSAT-1R > Multi-functional Transport Satellite 1 Replacement',
		},
		'MTSAT-2': {
			count: 3,
			children: {},
			parent: null,
			term: 'MTSAT-2 > The Multi-functional Transport Satellite 2',
		},
		'NOAA-11': {
			count: 2,
			children: {},
			parent: null,
			term: 'NOAA-11 > National Oceanic & Atmospheric Administration-11',
		},
		'NOAA-14': {
			count: 2,
			children: {},
			parent: null,
			term: 'NOAA-14 > National Oceanic & Atmospheric Administration-14',
		},
		'NOAA-16': {
			count: 8,
			children: {},
			parent: null,
			term: 'NOAA-16 > National Oceanic & Atmospheric Administration-16',
		},
		'NOAA-17': {
			count: 20,
			children: {},
			parent: null,
			term: 'NOAA-17 > National Oceanic & Atmospheric Administration-17',
		},
		'NOAA-18': {
			count: 21,
			children: {},
			parent: null,
			term: 'NOAA-18 > National Oceanic & Atmospheric Administration-18',
		},
		'NOAA-19': {
			count: 10,
			children: {},
			parent: null,
			term: 'NOAA-19 > National Oceanic & Atmospheric Administration-19',
		},
		'NOAA-7': {
			count: 2,
			children: {},
			parent: null,
			term: 'NOAA-7 > National Oceanic & Atmospheric Administration-7',
		},
		'NOAA-9': {
			count: 2,
			children: {},
			parent: null,
			term: 'NOAA-9 > National Oceanic & Atmospheric Administration-9',
		},
		SHIPS: {
			count: 27,
			children: {},
			parent: null,
			term: 'SHIPS',
		},
		'SUOMI-NPP': {
			count: 7,
			children: {},
			parent: null,
			term: 'SUOMI-NPP > Suomi National Polar-orbiting Partnership',
		},
		TERRA: {
			count: 2,
			children: {},
			parent: null,
			term: 'TERRA > Earth Observing System, TERRA (AM-1)',
		},
		TRMM: {
			count: 9,
			children: {},
			parent: null,
			term: 'TRMM > Tropical Rainfall Measuring Mission',
		},
	},
	projects: {
		CORSACS: {
			count: 1,
			children: {},
			parent: null,
			term: 'CORSACS > CONTROLS ON ROSS SEA ALGAL COMMUNITY STRUCTURE',
		},
		FOCI: {
			count: 1,
			children: {},
			parent: null,
			term: 'FOCI > Fisheries Oceanography Cooperative Investigation',
		},
		'GARP/FGGE': {
			count: 1,
			children: {},
			parent: null,
			term:
				'GARP/FGGE > Global Atmospheric Research Program/First Garp Global Experiment (GARP/FGGE)',
		},
		GHRSST: {
			count: 51,
			children: {},
			parent: null,
			term: 'GHRSST > Group for High Resolution Sea Surface Temperature',
		},
		GLOBEC: {
			count: 2,
			children: {},
			parent: null,
			term: 'GLOBEC > Global Ocean Ecosystem Dynamics, IGBP',
		},
		GODAR: {
			count: 13,
			children: {},
			parent: null,
			term: 'GODAR > Global Oceanographic Data Archaeology and Rescue Project',
		},
		JGOFS: {
			count: 2,
			children: {},
			parent: null,
			term: 'JGOFS > Joint Global Ocean Flux Study, IGBP',
		},
		'NOAA OneStop Project': {
			count: 52,
			children: {},
			parent: null,
			term: 'NOAA OneStop Project',
		},
		OCSEAP: {
			count: 1,
			children: {},
			parent: null,
			term: 'OCSEAP > Ocean Continental Shelf Environmental Assessment Project',
		},
		'U.S.GLOBEC-GB': {
			count: 1,
			children: {},
			parent: null,
			term:
				'U.S.GLOBEC-GB > U.S. Global Ocean Ecosystem Dynamics, Georges Bank',
		},
		'U.S.GLOBEC-NEP': {
			count: 1,
			children: {},
			parent: null,
			term:
				'U.S.GLOBEC-NEP > U.S. Global Ocean Ecosystem Dynamics, Northeast Pacific',
		},
		'U.S.GLOBEC-SO': {
			count: 1,
			children: {},
			parent: null,
			term:
				'U.S.GLOBEC-SO > U.S. Global Ocean Ecosystem Dynamics, Southern Ocean',
		},
		VENTS: {
			count: 2,
			children: {},
			parent: null,
			term: 'VENTS > VENTS Program, Pacific Marine Environmental Laboratory',
		},
		WOCE: {
			count: 1,
			children: {},
			parent: null,
			term: 'WOCE > World Ocean Circulation Experiment',
		},
	},
	dataCenters: {
		'DOC/NOAA/NESDIS/NCDC': {
			count: 2,
			children: {},
			parent: null,
			term:
				'DOC/NOAA/NESDIS/NCDC > National Climatic Data Center, NESDIS, NOAA, U.S. Department of Commerce',
		},
		'DOC/NOAA/NESDIS/NCEI': {
			count: 112,
			children: {},
			parent: null,
			term:
				'DOC/NOAA/NESDIS/NCEI > National Centers for Environmental Information, NESDIS, NOAA, U.S. Department of Commerce',
		},
		'DOC/NOAA/NESDIS/NODC': {
			count: 112,
			children: {},
			parent: null,
			term:
				'DOC/NOAA/NESDIS/NODC > National Oceanographic Data Center, NESDIS, NOAA, U.S. Department of Commerce',
		},
		'DOC/NOAA/NESDIS/OSDPD': {
			count: 11,
			children: {},
			parent: null,
			term:
				'DOC/NOAA/NESDIS/OSDPD > Office of Satellite Data Processing and Distribution, NESDIS, NOAA, U.S. Department of Commerce',
		},
		'DOC/NOAA/NMFS': {
			count: 2,
			children: {},
			parent: null,
			term:
				'DOC/NOAA/NMFS > National Marine Fisheries Service, NOAA, U.S. Department of Commerce',
		},
		'DOC/NOAA/NMFS/AFSC': {
			count: 2,
			children: {},
			parent: null,
			term:
				'DOC/NOAA/NMFS/AFSC > Alaska Fisheries Science Center, National Marine Fisheries Service, NOAA, U.S. Department of Commerce',
		},
		'DOC/NOAA/NMFS/AFSC/NMML': {
			count: 5,
			children: {},
			parent: null,
			term:
				'DOC/NOAA/NMFS/AFSC/NMML > National Marine Mammal Laboratory, Alaska Fisheries Science Center, National Marine Fisheries Service, NOAA, U.S. Department of Commerce',
		},
		'DOC/NOAA/NMFS/NEFSC': {
			count: 1,
			children: {},
			parent: null,
			term:
				'DOC/NOAA/NMFS/NEFSC > Northeast Fisheries Science Center, National Marine Fisheries Service, NOAA, U.S. Department of Commerce',
		},
		'DOC/NOAA/NMFS/NWFSC': {
			count: 1,
			children: {},
			parent: null,
			term:
				'DOC/NOAA/NMFS/NWFSC > Northwest Fisheries Science Center, National Marine Fisheries Service, NOAA. U.S. Department of Commerce',
		},
		'DOC/NOAA/NMFS/SEFSC': {
			count: 1,
			children: {},
			parent: null,
			term:
				'DOC/NOAA/NMFS/SEFSC > Southeast Fisheries Science Center, National Marine Fisheries Service, NOAA, U.S. Department of Commerce',
		},
		'DOC/NOAA/NOS': {
			count: 1,
			children: {},
			parent: null,
			term:
				'DOC/NOAA/NOS > National Ocean Service, NOAA, U.S. Department of Commerce',
		},
		'DOC/NOAA/NOS/CO-OPS': {
			count: 1,
			children: {},
			parent: null,
			term:
				'DOC/NOAA/NOS/CO-OPS > Center for Operational Oceanographic Products and Services, National Ocean Service, NOAA, U.S. Department of Commerce',
		},
		'DOC/NOAA/NOS/NCCOS': {
			count: 4,
			children: {},
			parent: null,
			term:
				'DOC/NOAA/NOS/NCCOS > National Centers for Coastal Ocean Science, National Ocean Service, NOAA, U.S. Department of Commerce',
		},
		'DOC/NOAA/NOS/NMS': {
			count: 2,
			children: {},
			parent: null,
			term:
				'DOC/NOAA/NOS/NMS > National Marine Sanctuaries, National Ocean Service, NOAA, U.S. Department of Commerce',
		},
		'DOC/NOAA/NOS/ORR': {
			count: 3,
			children: {},
			parent: null,
			term:
				'DOC/NOAA/NOS/ORR > Office of Response and Restoration, National Ocean Service, NOAA, U.S. Department of Commerce',
		},
		'DOC/NOAA/NWS/NDBC': {
			count: 1,
			children: {},
			parent: null,
			term:
				'DOC/NOAA/NWS/NDBC > National Data Buoy Center, National Weather Service, NOAA, U.S. Department of Commerce',
		},
		'DOC/NOAA/OAR/AOML': {
			count: 1,
			children: {},
			parent: null,
			term:
				'DOC/NOAA/OAR/AOML > Atlantic Oceanographic and Meteorological Laboratory, OAR, NOAA, U.S. Department of Commerce',
		},
		'DOC/NOAA/OAR/PMEL': {
			count: 3,
			children: {},
			parent: null,
			term:
				'DOC/NOAA/OAR/PMEL > Pacific Marine Environmental Laboratory, OAR, NOAA, U.S. Department of Commerce',
		},
		DOI: {
			count: 2,
			children: {},
			parent: null,
			term: 'DOI > U.S. Department of the Interior',
		},
		'DOI/BOEM': {
			count: 1,
			children: {},
			parent: null,
			term: 'DOI/BOEM',
		},
		'DOI/USFWS': {
			count: 2,
			children: {},
			parent: null,
			term:
				'DOI/USFWS > U.S. Fish and Wildlife Service, U.S. Department of the Interior',
		},
		'DOI/USGS': {
			count: 1,
			children: {},
			parent: null,
			term:
				'DOI/USGS > U.S. Geological Survey, U.S. Department of the Interior',
		},
		'GOVERNMENT AGENCIES-U.S. FEDERAL AGENCIES': {
			count: 1,
			children: {},
			parent: null,
			term:
				'GOVERNMENT AGENCIES-U.S. FEDERAL AGENCIES > DOC > NOAA > DOC/NOAA/NWS/NDBC > National Data Buoy Center, National Weather Service, NOAA, U.S. Department of Commerce > http://www.ndbc.noaa.gov/',
		},
		IOOS: {
			count: 3,
			children: {},
			parent: null,
			term:
				'IOOS > Integrated Ocean Observing System, NOAA, U.S. Department of Commerce',
		},
		'NASA/JPL/PODAAC': {
			count: 51,
			children: {},
			parent: null,
			term:
				'NASA/JPL/PODAAC > Physical Oceanography Distributed Active Archive Center, Jet Propulsion Laboratory, NASA',
		},
		'RU/ROSHYDROMET/FERHRI': {
			count: 3,
			children: {},
			parent: null,
			term:
				'RU/ROSHYDROMET/FERHRI > Far East Regional Hydrometeorological Research Institute, Russian Federal Service for Hydrometeorology and Environmental Monitoring, Russia',
		},
		WHOI: {
			count: 4,
			children: {},
			parent: null,
			term: 'WHOI > Woods Hole Oceanographic Institution',
		},
	},
};

export const metaFacetsConverter = facets => {
	let newFacets = {};

	for (let facetKey in facets) {
		let facetSection = {};
		let categoryHierarchies = facets[facetKey];

		for (let categoryHierarchy in categoryHierarchies) {
			let info = categoryHierarchies[categoryHierarchy];
			info['hierarchy'] = categoryHierarchy;

			// split category hierarchy string into trimmed array items
			let categoryHierarchyArray = categoryHierarchy
				.split('>')
				.map(x => x.trim());

			// convert category hierarchy array into nested object/keys
			let categoryHierarchyObject = {};
			categoryHierarchyArray.reduce((o, s) => {
				return (o[s] = {});
			}, categoryHierarchyObject);

			// set the info at the leaf node of the object for this category hierarchy
			_.set(categoryHierarchyObject, categoryHierarchyArray, info);

			// merge this object into the total newFacets object
			facetSection = _.merge(categoryHierarchyObject, facetSection);
		}

		switch (facetKey) {
			case 'science':
				newFacets['Data Theme'] = facetSection;
				break;
			case 'instruments':
				newFacets['Instruments'] = facetSection;
				break;
			case 'platforms':
				newFacets['Platforms'] = facetSection;
				break;
			case 'projects':
				newFacets['Projects'] = facetSection;
				break;
			case 'dataCenters':
				newFacets['Data Centers'] = facetSection;
				break;
			default:
				newFacets[facetKey] = facetSection;
		}
	}
	return newFacets;
};

const buildHierarchyMap = (category, terms) => {
  console.log(category)
  console.log(terms)

  var createChildrenHierarchy = (map, hierarchy, term, value) => {
    const lastTerm = hierarchy.pop()
    if(!_.isEmpty(hierarchy)) {
      let i;
      for(i = 0; i < hierarchy.length; i++) {
        // Since hierarchical strings are received in alphabetical order, this traversal
        // down the nested object won't error out
        //_.defaults(map, map[hierarchy[i]].children)
        map = map[hierarchy[i]].children = map[hierarchy[i]].children || {}
      }
    }

    map = map[lastTerm] = value
    return map
  }

  let categoryMap = {}

  Object.keys(terms).map( term => {
    let hierarchy = term.split(' > ')
    const parentTerm = hierarchy[hierarchy.length - 2]
    const value = {
      count: terms[term].count,
      children: {},
      parent: parentTerm ? parentTerm : null,
      term: term
    }

    createChildrenHierarchy(categoryMap, hierarchy, term, value)
  })

  console.log(categoryMap)
  return categoryMap
}

export const buildKeywordHierarchyMap = facetMap => {
  const hierarchyMap = {}
  _.map(facetMap, (terms, category) => {
    console.log(category)
    console.log(terms)
    if (!_.isEmpty(terms)) { // Don't load categories that have no results
      let heading
      let categoryMap = {}

      if(category === 'science') {
        heading = 'Data Theme'
        categoryMap = buildHierarchyMap(category, terms)
      }
      else {
        heading = _.startCase(_.toLower((category.split(/(?=[A-Z])/).join(" "))))
        Object.keys(terms).map( term => {
          const name = term.split(' > ')
          categoryMap[name[0]] = {
            count: terms[term].count,
            children: {},
            parent: null,
            term: term
          }
        })
      }

      hierarchyMap[heading] = categoryMap
    }
  })

  return hierarchyMap
}


