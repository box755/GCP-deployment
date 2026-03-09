<script setup>
import HomeCategory from './components/HomeCategory.vue'
import HomeBanner from './components/HomeBanner.vue'
import HomeNew from './components/HomeNew.vue'
import HomeHot from './components/HomeHot.vue'
import HomeBestSeller from './components/HomeBestSeller.vue'
import HomeMustBuy from './components/HomeMustBuy.vue'
import HomeProduct from './components/HomeProduct.vue'

</script>

<template>
  <div class="container">
    <HomeCategory />
    <HomeBanner />
  </div>
  <HomeNew />
  <HomeHot />
  <HomeBestSeller />
  <HomeMustBuy />
  <HomeProduct />
</template>
