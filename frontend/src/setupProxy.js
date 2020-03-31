const {createProxyMiddleware} = require('http-proxy-middleware');

module.exports = function (app) {
    app.use(createProxyMiddleware('/api/',
        {
            target: 'http://localhost:8081', // target host
            changeOrigin: true, // needed for virtual hosted sites
            ws: true, // proxy websockets
            pathRewrite: {
                '^/api/': '/', // rewrite path
            }
        }
    ));
};