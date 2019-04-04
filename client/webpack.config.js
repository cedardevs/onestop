const webpack = require('webpack')
const HtmlWebpackPlugin = require('html-webpack-plugin')

const SpeedMeasurePlugin = require("speed-measure-webpack-plugin")

const path = require('path')
require('babel-polyfill')

const nodeEnv = process.env.NODE_ENV || 'development'
const isProd = nodeEnv === 'production'

const rootPath = 'onestop'
const assetPath = 'static'

const smp = new SpeedMeasurePlugin()

const basePlugins = [
  new HtmlWebpackPlugin({
    inject: false,
    template: require('html-webpack-template'),
    lang: 'en-US',
  })
]

const devPlugins = [
  // enable HMR globally
  new webpack.HotModuleReplacementPlugin(),

  // prints more readable module names in the browser console on HMR updates
  new webpack.NamedModulesPlugin(),
]

const prodPlugins = [
  new webpack.DefinePlugin({
    'process.env': {
      'NODE_ENV': JSON.stringify('production'),
    },
  }),
  new webpack.LoaderOptionsPlugin({
    minimize: true,
    debug: false,
  }),
]

const devEntryPoints = [
  'babel-polyfill',

  // bundle the client for webpack-dev-server and connect to the provided endpoint
  'webpack-dev-server/client?http://localhost:8080',

  // bundle the client for hot reloading hot reload for successful updates
  'webpack/hot/only-dev-server',

  './index.jsx',
]

const prodEntryPoints = [
  'babel-polyfill',
  './index.jsx',
]

module.exports = env => {
  return smp.wrap({
    entry: isProd ? prodEntryPoints : devEntryPoints,
    output:
        {
          path: path.resolve(__dirname, 'build/dist'),
          publicPath: `/${rootPath}/`,
          filename: '[name]-[hash].bundle.js',
        }
    ,
    context: path.resolve(__dirname, 'src'),
    devtool:
        isProd ? false : 'cheap-module-eval-source-map',
    devServer:
        isProd ? {} : {
          publicPath: `/${rootPath}/`,
          historyApiFallback: {
            index: `/${rootPath}/`,
          },
          disableHostCheck: true,
          hot: true,
          proxy: {
            '/onestop-search/*': {
              target: `${env.URL_API_SEARCH}/`,
              secure: false,
            },
          },
        },
    module:
        {
          rules: [{
            enforce: 'pre',
            test: /\.js$/,
            use: 'eslint-loader',
            exclude: /node_modules/,
          }, {
            test: /\.jsx?$/,
            exclude: /node_modules/,
            use: {
              loader: 'babel-loader',
              options: {
                babelrc: true,
                babelrcRoots: ['.', '../'],
              },
            },
          }, {
            test: /\.css$/,
            include: /node_modules/,
            use: [{
              loader: 'style-loader',
              options: {
                sourceMap: !isProd,
              },
            }, {
              loader: 'css-loader',
            }],
          }, {
            test: /\.css$/,
            exclude: /node_modules/,
            use: [{
              loader: 'style-loader',

            }, {
              loader: 'css-loader', options: {url:false}
            }],
          }, {
            test: /\.(jpe?g|png|gif|svg)$/,
            use: [
              {
                loader: 'file-loader',
                options: {
                  hash: 'sha512',
                  digestType: 'hex',
                  name: '[hash].[ext]',
                  outputPath: `${assetPath}/img`
                },
              },
            ],
          }, {
            test: /\.(ico)$/,
            exclude: /node_modules/,
            include: /img/,
            use: [
              {
                loader: 'file-loader',
                options: {
                  name: '[name].[ext]',
                  outputPath: assetPath
                },
              },
            ],
          }, {
            test: /\.(ttf|otf|eot|woff(2)?)(\?[a-z0-9]+)?$/,
            use: [{loader: 'file-loader'
              , options: {
                name: '/[name].[ext]',
                outputPath: `${assetPath}/fonts`
            }
          }]
          }],
        }
    ,
    resolve: {
      modules: [path.resolve('./node_modules/leaflet/dist', 'root'), 'node_modules',
        path.resolve('./src/common/link')],
      extensions:
          ['.js', '.jsx'],
      unsafeCache:
          !isProd,
      alias:
          {
            'fa':
                path.resolve(__dirname, 'img/font-awesome/white/svg/'),
          },
    }
    ,
    plugins: isProd ? basePlugins.concat(prodPlugins) : basePlugins.concat(devPlugins),
  })
}
