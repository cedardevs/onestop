const webpack = require('webpack')
const HtmlWebpackPlugin = require('html-webpack-plugin')
const SpeedMeasurePlugin = require('speed-measure-webpack-plugin')
const ESLintPlugin = require('eslint-webpack-plugin')

const path = require('path')
require('babel-polyfill')

const nodeEnv = process.env.NODE_ENV || 'development'
const isProd = nodeEnv === 'production'

const rootPath = 'onestop'
const assetPath = 'static'

const smp = new SpeedMeasurePlugin()

const basePlugins = [
  new HtmlWebpackPlugin({
    inject: true,
    lang: 'en-US',
  }),
  new ESLintPlugin()
]

const devPlugins = [
]

const prodPlugins = [
  new webpack.DefinePlugin({
    'process.env': {
      NODE_ENV: JSON.stringify('production'),
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
  // ensure host and port here matches the host and port specified in the `devServer` section
  // otherwise, you may see console warnings like: `sockjs-node ERR_CONNECTION_REFUSED`
  // see: https://github.com/webpack/webpack-dev-server/issues/416#issuecomment-287797086
  'webpack-dev-server/client?http://localhost:8888',

  // bundle the client for hot reloading hot reload for successful updates
  'webpack/hot/only-dev-server',

  './index.jsx',
]

const prodEntryPoints = ['babel-polyfill', './index.jsx']

module.exports = (env) => {
  return smp.wrap({
    mode: isProd ? 'production' : 'development',
    entry: isProd ? prodEntryPoints : devEntryPoints,
    output: {
      path: path.resolve(__dirname, 'build/webpack'),
      publicPath: `/${rootPath}/`,
      filename: '[name]-[hash].bundle.js',
    },
    context: path.resolve(__dirname, 'src'),
    devtool: isProd ? false : 'cheap-module-source-map',
    devServer: isProd
      ? {}
      : {
          devMiddleware: {publicPath: `/${rootPath}/`},
          historyApiFallback: {
            index: `/${rootPath}/`,
          },
          // ensure host and port here matches the host and port specified in the `devEntryPoints` above
          // otherwise, you may see console warnings like: `sockjs-node ERR_CONNECTION_REFUSED`
          // see: https://github.com/webpack/webpack-dev-server/issues/416#issuecomment-287797086
          host: 'localhost',
          port: 8888,
          // This is set to all so we can still use virtualbox for ie and edge testing
          allowedHosts: ['all'],
          hot: true,
          proxy: {
            '/onestop/api/search/*': {
              target: `${env.URL_API_GATEWAY}`,
              secure: false,
            },
            // '!/onestop/*': {
            //   target: `${env.URL_API_GATEWAY}/`,
            //   secure: false,
            // },
          },
        },
    module: {
      rules: [
        {
          test: /\.jsx?$/,
          exclude: /node_modules/,
          use: {
            loader: 'babel-loader',
            options: {
              presets: ['@babel/preset-env'],
              babelrc: true,
              babelrcRoots: ['.', '../'],
            },
          },
        },
        {
          test: /\.css$/,
          include: /node_modules/,
          use: [
            {
              loader: 'style-loader',
            },
            {
              loader: 'css-loader',
            },
          ],
        },
        {
          test: /\.css$/,
          exclude: /node_modules/,
          use: [
            {
              loader: 'style-loader',
            },
            {
              loader: 'css-loader',
              options: { url: false },
            },
          ],
        },
        {
          test: /\.(jpe?g|png|gif|svg)$/,
          type: 'asset/resource',
          generator: {
            filename: `${assetPath}/img/[hash][ext]`,// Don't need period, seems to included in [ext]
          }
        },
        {
          test: /\.(ico)$/,
          exclude: /node_modules/,
          include: /img/,
          type: 'asset/resource',
          generator: {
            filename: `${assetPath}/[name][ext]`,// Don't need period, seems to included in [ext]
          }
        },
        {
          test: /\.(ttf|otf|eot|woff(2)?)(\?[a-z0-9]+)?$/,
          type: 'asset/resource',
          generator: {
            filename: `${assetPath}/fonts/[name][ext]`,// Don't need period, seems to included in [ext]
          }
        },
      ],
    },
    resolve: {
      modules: [
        path.resolve('./node_modules/leaflet/dist', 'root'),
        'node_modules',
        path.resolve('./src/common/link'),
      ],
      extensions: ['.js', '.jsx'],
      unsafeCache: !isProd,
      alias: {
        fa: path.resolve(__dirname, 'img/font-awesome/white/svg/'),
      },
    },
    plugins: isProd
      ? basePlugins.concat(prodPlugins)
      : basePlugins.concat(devPlugins),
  })
}
