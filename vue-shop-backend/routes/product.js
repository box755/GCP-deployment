const { getHotProducts, getNewProducts, getBestSellerProducts, getMustBuyProducts, getProductDetailById } = require('../controllers/product');
const express = require("express");

const router = express.Router();

module.exports = (app) => {

    router.get('/getHotProducts', getHotProducts);
    router.get('/getNewProducts', getNewProducts);
    router.get('/getBestSellerProducts', getBestSellerProducts);
    router.get('/getMustBuyProducts', getMustBuyProducts);
    router.get('/getProductDetail', getProductDetailById);

    app.use('/api/product/', router)

}
