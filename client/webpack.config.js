var webpack = require('webpack');
var HtmlWebpackPlugin = require('html-webpack-plugin');
var postcss = require("postcss");
var precss = require ('precss');
var autoprefixer = require ('autoprefixer');
var postcssImport = require('postcss-import');

module.exports = {
  entry: [
    'webpack-dev-server/client?http://localhost:8080',
    'webpack/hot/only-dev-server',
    './src/index.jsx'
  ],
  module: {
    preLoaders: [{
      test: /\.js$/,
      loader: "eslint-loader",
      exclude: /node_modules/
    }],
    loaders: [{
      test: /\.jsx?$/,
      exclude: /node_modules/,
      loader: 'react-hot!babel'
    }, {
      test: /\.css$/,
      loaders: [
        'style?sourceMap',
        'css?modules&importLoaders=1&localIdentName=[name]__[local]___[hash:base64:5]',
        'postcss']
    }, {
      test: /\.jpg$/,
      exclude: /node_modules/,
      loader: 'file?name=[path][name]-[hash].[ext]'
    }]
  },
  postcss: function(webpack){
    return [precss, autoprefixer, postcssImport({addDependencyTo:webpack})]
  },
  resolve: {
    extensions: ['', '.js', '.jsx']
  },
  output: {
    path: __dirname + '/dist',
    publicPath: '/',
    filename: 'bundle-[hash].js'
  },
  devtool: 'source-map',
  devServer: {
    contentBase: './dist',
    hot: true
  },
  plugins: [
    new webpack.HotModuleReplacementPlugin(),
    new webpack.optimize.UglifyJsPlugin(),
    new HtmlWebpackPlugin({
      title: 'NOAA OneStop'
    })
  ]
};
