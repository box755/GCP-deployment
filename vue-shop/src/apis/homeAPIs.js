import httpInstance from "@/utils/http.js"

export function getNewGoodsAPI() {
  return httpInstance({
    url: '/api/product/getNewProducts'
  })
}

export function getHotGoodsAPI() {
  return httpInstance({
    url: '/api/product/getHotProducts'
  })
}

export function getBestSellerAPI() {
  return httpInstance({
    url: '/api/product/getBestSellerProducts'
  })
}

export function getMustBuyAPI() {
  return httpInstance({
    url: '/api/product/getMustBuyProducts'
  })
}
