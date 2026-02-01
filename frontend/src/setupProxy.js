const {createProxyMiddleware} = require('http-proxy-middleware');

module.exports = function (app) {
    app.use(createProxyMiddleware('/api',{
            target: 'http://localhost:8083', // target host
            changeOrigin: true, // needed for virtual hosted sites
            ws: true, // proxy websockets
            pathRewrite: {
                '^/api/products': '/products', // rewrite path
            }
        }
    ));

    // Cart service
    app.use(createProxyMiddleware('/api/cart', {
            target: 'http://localhost:8086',
            changeOrigin: true,
            ws: true,
            pathRewrite: {
                '^/api/cart': '/cart',
            }
        })
    );
};