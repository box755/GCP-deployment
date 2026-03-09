<script setup>
</script>

<template>
  <nav class="app-topnav">
    <div class="container">
      <ul>
        <template v-if="true">
          <li><a href="javascript:;"><i class="iconfont icon-user"></i>周杰倫</a></li>
          <li><a href="javascript:;" class="disabled-link">退出登入（待開發）</a></li>
          <li><a href="javascript:;" class="disabled-link">會員中心（待開發）</a></li>
          <li><a href="javascript:;" class="disabled-link">我的訂單（待開發）</a></li>
        </template>
        <template v-else>
          <li><a href="javascript:;">請先登入</a></li>
          <li><a href="javascript:;">幫助中心</a></li>
          <li><a href="javascript:;">關於我們</a></li>
        </template>
      </ul>
    </div>
  </nav>
</template>


<style scoped lang="scss">
@use "sass:color";
.app-topnav {
  background: #333;
  border-bottom: 1px solid color.adjust(#333, $lightness: -10%);
  padding: 2px 0;

  .container {
    display: flex;
    justify-content: flex-end;
    align-items: center;
    max-width: 1200px;
    margin: 0 auto;
  }

  ul {
    display: flex;
    align-items: center;

    li {
      position: relative;

      a {
        padding: 10px 18px;
        color: #cdcdcd;
        font-size: 14px;
        display: flex;
        align-items: center;
        transition: color 0.3s ease, background 0.3s ease;
        border-radius: 4px;

        i {
          font-size: 16px;
          margin-right: 5px;
        }

        &:hover {
          color: color.adjust($xtxColor, $lightness: -10%);
          background: rgba(255, 255, 255, 0.1);
        }

        &.disabled-link {
          color: #888;
          cursor: not-allowed;
          opacity: 0.6;

          &:hover {
            color: #888;
            background: none;
          }
        }
      }

      &:not(:last-child) {
        a {
          border-right: 2px solid color.adjust(#666, $lightness: -10%);
        }
      }
    }
  }
}

</style>
