const webpack = require('webpack')
const HtmlWebpackPlugin = require('html-webpack-plugin')
const path = require('path')
require('babel-polyfill')
const modernizrrc = path.resolve(__dirname, '.modernizrrc.json')
require(modernizrrc)
const nodeEnv = process.env.NODE_ENV || "development"
const isProd = nodeEnv === "production"

const basePlugins = [
  new HtmlWebpackPlugin({
    inject: false,
    title: 'NOAA OneStop',
    template: require('html-webpack-template'),
    lang: 'en-US',
    favicon: '../img/noaa-favicon.ico',
    meta: [
      {
        property: 'dcterms.format', content:'text/html'
      },
      {
        property: 'og:type',
        content: 'website',
      },
    ]
  }),
  new webpack.optimize.CommonsChunkPlugin({
    name: 'vendor',
    minChunks: function (module) {
      return module.context && module.context.indexOf('node_modules') !== -1
    }
  }),
]

const devPlugins = [
  // enable HMR globally
  new webpack.HotModuleReplacementPlugin(),

  // prints more readable module names in the browser console on HMR updates
  new webpack.NamedModulesPlugin(),
]

const prodPlugins = [
  new webpack.DefinePlugin({
    'process.env':{
      'NODE_ENV': JSON.stringify('production')
    }
  }),
  new webpack.optimize.UglifyJsPlugin({
    compress: {warnings: false}
  }),
  new webpack.LoaderOptionsPlugin({
    minimize: true,
    debug: false
  }),
]

const devEntryPoints = [
  'babel-polyfill',
  modernizrrc,

  // bundle the client for webpack-dev-server and connect to the provided endpoint
  'webpack-dev-server/client?http://localhost:8080',

  // bundle the client for hot reloading hot reload for successful updates
  'webpack/hot/only-dev-server',

  './index.jsx',
]

const prodEntryPoints = [
  'babel-polyfill',
  modernizrrc,
  './index.jsx'
]

module.exports = {
  entry: isProd ? prodEntryPoints : devEntryPoints,
  output: {
    path: path.resolve(__dirname, 'build/dist'),
    publicPath: './',
    filename: '[name]-[hash].bundle.js'
  },
  context: path.resolve(__dirname, 'src'),
  devtool: isProd ? false : 'cheap-module-eval-source-map',
  devServer: isProd ? {} : {
    publicPath: '/onestop/',
    disableHostCheck: true,
    hot: true,
    proxy: {
      '/onestop/api/*': {
        target: 'http://localhost:8097/',
        secure: false
      }
    }
  },
  module: {
    rules: [{
      test: /\.modernizrrc.json/,
      use: [ 'modernizr-loader', 'json-loader']
    }, {
      enforce: 'pre',
      test: /\.js$/,
      use: 'eslint-loader',
      exclude: /node_modules/
    }, {
      test: /\.jsx?$/,
      exclude: /node_modules/,
      use: {
        loader: 'babel-loader',
        options: {
          presets: [
            [ 'env', { modules: false } ],
            'react',
            'stage-0'
          ]
        }
      }
    }, {
      test: /\.css$/,
      include: /node_modules/,
      use: [{
        loader: 'style-loader',
        options: {
          sourceMap: !isProd
        }
      }, {
        loader: 'css-loader'
      }]
    },  {
      test: /\.css$/,
      exclude: /node_modules/,
      use: [{
        loader: 'style-loader',
        options: {
          sourceMap: !isProd
        }
      }, {
        loader: 'css-loader'
      }],
    }, {
      test: /\.(jpe?g|png|gif)$/,
      use: [
          {
            loader: 'file-loader',
              options: {
                hash: 'sha512',
                  digestType: 'hex',
                  name: '[hash].[ext]'
              }
          },
      ],
    }, {
      test: /\.(ttf|otf|eot|svg|woff(2)?)(\?[a-z0-9]+)?$/,
      use: [{loader: 'file-loader?name=fonts/[name].[ext]'}]
    }]
  },
  resolve: {
    modules: [path.resolve('./node_modules/leaflet/dist', 'root'), 'node_modules',
      path.resolve('./src/common/link')],
    extensions: ['.js', '.jsx'],
    unsafeCache: !isProd,
    alias: {
      'fonts': path.resolve(__dirname, 'fonts/'),
      'fa': path.resolve(__dirname, 'img/font-awesome/white/svg/'),
      modernizr$: path.resolve(__dirname, ".modernizrrc.json")
    }
  },
  plugins: isProd ? basePlugins.concat(prodPlugins) : basePlugins.concat(devPlugins)
}
