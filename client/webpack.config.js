const webpack = require('webpack')
const HtmlWebpackPlugin = require('html-webpack-plugin')
const path = require('path')
require('babel-polyfill')
const modernizrrc = path.resolve(__dirname, '.modernizrrc.json')
require(modernizrrc)

module.exports = {
  entry: [
    'babel-polyfill',
    modernizrrc,
    'react-hot-loader/patch',
    // activate HMR for React

    'webpack-dev-server/client?http://localhost:8080',
    // bundle the client for webpack-dev-server
    // and connect to the provided endpoint

    'webpack/hot/only-dev-server',
    // bundle the client for hot reloading
    // hot reload for successful updates
    './index.jsx'
  ],
  output: {
    path: path.resolve(__dirname, 'dist'),
    publicPath: './',
    filename: '[name].bundle.js'
  },
  context: path.resolve(__dirname, 'src'),
  devtool: 'cheap-module-eval-source-map',
  devServer: {
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
              'react', 'stage-0'
          ]
        }
      }
    }, {
      test: /\.css$/,
      include: /node_modules/,
      use: [{
        loader: 'style-loader',
        options: {
          sourceMap: true
        }
      }, {
        loader: 'css-loader'
      }]
    }, {
      test: /\.css$/,
      exclude: /node_modules/,
      use: [{
        loader: 'style-loader',
        options: {
          sourceMap: true
        }
      }, {
        loader: 'css-loader',
        options: {
          modules: true,
          importLoaders: true,
          localIdentName: '[name]__[local]___[hash:base64:5]',
          plugins: function () {
            return [
              require('precss'),
              require('autoprefixer')
            ]
          }
        }
      }, {
        loader: 'postcss-loader'
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
    unsafeCache: true,
    alias: {
      'fa': path.resolve(__dirname, 'img/font-awesome/white/svg/'),
      modernizr$: path.resolve(__dirname, ".modernizrrc.json")
    }
  },
  plugins: [
    new HtmlWebpackPlugin({
      inject: false,
      template: require('html-webpack-template'),
      title: 'NOAA OneStop',
      favicon: '../img/noaa-favicon.ico',
      lang: 'en-US'//,
      // googleAnalytics: {
      //   trackingId: '',
      //   pageViewOnLoad: true
      // }
    }),
    new webpack.optimize.CommonsChunkPlugin({
      name: 'vendor',
      minChunks: function (module) {
        return module.context && module.context.indexOf('node_modules') !== -1
      }
    }),
    //new webpack.optimize.CommonsChunkPlugin("vendor", "vendor-bundle-[hash].js")
    new webpack.HotModuleReplacementPlugin(),
    // enable HMR globally

    new webpack.NamedModulesPlugin()
    // prints more readable module names in the browser console on HMR updates
  ]
}
