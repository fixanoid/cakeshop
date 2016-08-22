var path = require('path');
var webpack = require('webpack');

module.exports = {
  entry: "./js/index.js",
  output: {
    path: path.join(__dirname, 'js'),
    filename: "index-gen.js",
  },
  plugins: [
    new webpack.ProvidePlugin({
      $: "jquery",
      jQuery: "jquery",
      "window.jQuery": "jquery",
      d3: "d3",
      Backbone: "backbone",
      _: "underscore",
      SockJS: "sockjs-client",
      moment: "moment",
    }),
  ],
  module: {
    loaders: [
      {
        test: /\.js$/,
        include: [
          path.resolve(__dirname, "js"),
          path.resolve(__dirname, "node_modules/dashboard-framework"),
        ],
        loader: 'babel',
        query: {
          presets: ['es2015'],
          plugins: [
            'transform-promise-to-bluebird',
            'transform-runtime'
          ],
        }
      }
    ]
  }
};
