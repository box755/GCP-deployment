-- ============================================
-- Vue Shop 種子資料 (Seed Data)
-- 資料庫：myvueshop2
-- 產生時間：2026-03-09
-- 使用方式：先啟動 app.js 讓 Sequelize 自動建表
--          再執行此 SQL 插入假資料
-- ============================================

USE `myvueshop2`;

-- 清空所有表資料（依照外鍵依賴順序）
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE `sku_spec_values`;
TRUNCATE TABLE `skus`;
TRUNCATE TABLE `spec_values`;
TRUNCATE TABLE `specs`;
TRUNCATE TABLE `banners`;
TRUNCATE TABLE `products`;
TRUNCATE TABLE `brands`;
TRUNCATE TABLE `posts`;
TRUNCATE TABLE `categories`;
SET FOREIGN_KEY_CHECKS = 1;

-- ============================================
-- 1. 頂層分類 (parentId = NULL)
-- ============================================
INSERT INTO `categories` (`id`, `name`, `picture`, `parentId`, `saleInfo`, `createdAt`, `updatedAt`) VALUES
('cat-home',    '居家',   'https://yanxuan-item.nosdn.127.net/bb08893320a4ad50b71040c4d8a313d6.png', NULL, '居家好物\r\n溫馨生活', NOW(), NOW()),
('cat-food',    '美食',   'https://yanxuan-item.nosdn.127.net/15a3e09698c7a2e553e595decff8a8e6.png', NULL, '精選美食\r\n品味生活', NOW(), NOW()),
('cat-cloth',   '服飾',   'https://yanxuan-item.nosdn.127.net/b5977702e93282d498ada798ea28c1ee.png', NULL, '時尚穿搭\r\n質感單品', NOW(), NOW()),
('cat-digital', '數碼',   'https://yanxuan-item.nosdn.127.net/5c1d3b4a3a7e22b758624cce03498aef.png', NULL, '科技數碼\r\n智能生活', NOW(), NOW()),
('cat-beauty',  '美妝',   'https://yanxuan-item.nosdn.127.net/354e2239de1032f4bcb4f4e3d72741e4.png', NULL, '精緻美妝\r\n煥發光彩', NOW(), NOW());

-- ============================================
-- 2. 子分類 (parentId 指向頂層)
-- ============================================
INSERT INTO `categories` (`id`, `name`, `picture`, `parentId`, `saleInfo`, `createdAt`, `updatedAt`) VALUES
-- 居家子分類
('cat-bed',       '床品',     'https://yanxuan-item.nosdn.127.net/41065ad0eb7655e8bb0593206b0cf025.png', 'cat-home',    NULL, NOW(), NOW()),
('cat-kitchen',   '廚房用品', 'https://yanxuan-item.nosdn.127.net/14ff263e0a2407336cc4697a40ddc419.png', 'cat-home',    NULL, NOW(), NOW()),
('cat-bath',      '衛浴用品', 'https://yanxuan-item.nosdn.127.net/beaboread89a4d9dbe9b22ba50f2c097.png', 'cat-home',    NULL, NOW(), NOW()),
-- 美食子分類
('cat-snack',     '零食',     'https://yanxuan-item.nosdn.127.net/3e56e2bd7bd2c5e0baac69a5cec3f15d.png', 'cat-food',    NULL, NOW(), NOW()),
('cat-drink',     '飲品',     'https://yanxuan-item.nosdn.127.net/1e5281e4eab8a26f1a0292e8bf0b4497.png', 'cat-food',    NULL, NOW(), NOW()),
('cat-fresh',     '生鮮',     'https://yanxuan-item.nosdn.127.net/2eb4ab0c64d46ccc85820c9e09e09526.png', 'cat-food',    NULL, NOW(), NOW()),
-- 服飾子分類
('cat-men',       '男裝',     'https://yanxuan-item.nosdn.127.net/33cc6c5c42b27e42adbb4994a6736e0e.png', 'cat-cloth',   NULL, NOW(), NOW()),
('cat-women',     '女裝',     'https://yanxuan-item.nosdn.127.net/60d0d3efbce31aaaf498e1c359805d84.png', 'cat-cloth',   NULL, NOW(), NOW()),
('cat-shoes',     '鞋靴',     'https://yanxuan-item.nosdn.127.net/c598f93a89a7d6072cbab9c893eaf8f5.png', 'cat-cloth',   NULL, NOW(), NOW()),
-- 數碼子分類
('cat-phone',     '手機配件', 'https://yanxuan-item.nosdn.127.net/53994cd6050aa08f7e80aadd7ebfb875.png', 'cat-digital', NULL, NOW(), NOW()),
('cat-computer',  '電腦週邊', 'https://yanxuan-item.nosdn.127.net/5c1d3b4a3a7e22b758624cce03498aef.png', 'cat-digital', NULL, NOW(), NOW()),
-- 美妝子分類
('cat-skincare',  '護膚',     'https://yanxuan-item.nosdn.127.net/354e2239de1032f4bcb4f4e3d72741e4.png', 'cat-beauty',  NULL, NOW(), NOW()),
('cat-makeup',    '彩妝',     'https://yanxuan-item.nosdn.127.net/e11d8c0a0fa73e68788ecaba3950565c.png', 'cat-beauty',  NULL, NOW(), NOW());

-- ============================================
-- 3. 品牌
-- ============================================
INSERT INTO `brands` (`id`, `name`, `nameEn`, `logo`, `picture`, `type`, `description`, `place`, `createdAt`, `updatedAt`) VALUES
('brand-muji',    '極簡家居',   'Muji Style',       'https://yanxuan-item.nosdn.127.net/logo-muji.png',    'https://yanxuan-item.nosdn.127.net/brand-muji.png',    '家居', '以簡約設計聞名的居家品牌，追求自然質樸的生活美學。', '日本',   NOW(), NOW()),
('brand-hola',    '生活工場',   'HOLA',              'https://yanxuan-item.nosdn.127.net/logo-hola.png',    'https://yanxuan-item.nosdn.127.net/brand-hola.png',    '家居', '提供多元的居家用品，讓生活更便利更美好。',           '台灣',   NOW(), NOW()),
('brand-uniqlo',  '優衣庫',     'UNIQLO',            'https://yanxuan-item.nosdn.127.net/logo-uniqlo.png',  'https://yanxuan-item.nosdn.127.net/brand-uniqlo.png',  '服飾', '日本快時尚品牌，以高品質基本款服飾著稱。',           '日本',   NOW(), NOW()),
('brand-apple',   '蘋果',       'Apple',             'https://yanxuan-item.nosdn.127.net/logo-apple.png',   'https://yanxuan-item.nosdn.127.net/brand-apple.png',   '數碼', '全球知名的科技品牌，創新設計領導者。',               '美國',   NOW(), NOW()),
('brand-shiseido','資生堂',     'Shiseido',          'https://yanxuan-item.nosdn.127.net/logo-shiseido.png','https://yanxuan-item.nosdn.127.net/brand-shiseido.png','美妝', '日本百年美妝品牌，融合東方美學與先端科技。',         '日本',   NOW(), NOW()),
('brand-lays',    '樂事',       'Lay\'s',            'https://yanxuan-item.nosdn.127.net/logo-lays.png',    'https://yanxuan-item.nosdn.127.net/brand-lays.png',    '食品', '全球最受歡迎的薯片品牌，口味豐富多樣。',             '美國',   NOW(), NOW());

-- ============================================
-- 4. 商品
-- ============================================
INSERT INTO `products` (`id`, `name`, `picture`, `desc`, `price`, `orderNum`, `discount`, `evaluateNum`, `oldPrice`, `inventory`, `mainVideos`, `videoScale`, `mainPictures`, `salesCount`, `commentCount`, `collectCount`, `details`, `isPreSale`, `spuCode`, `isCollect`, `brandId`, `categoryId`, `createdAt`, `updatedAt`) VALUES
-- 居家 > 床品
('prod-001', '日系簡約純棉床包組',       'https://yanxuan-item.nosdn.127.net/3dda16a0ad847a317e585cdee8bd1c3a.png', '舒適純棉，給您一夜好眠',       '1299.00', '9800', '6.5', '328', '1999.00', '200',  NULL, NULL, '["https://yanxuan-item.nosdn.127.net/3dda16a0ad847a317e585cdee8bd1c3a.png","https://yanxuan-item.nosdn.127.net/49223c0e48b2fe4dae498beaf91e2c17.png"]', '1580', '328', '256', '{"pictures":["https://yanxuan-item.nosdn.127.net/detail-bed1.png","https://yanxuan-item.nosdn.127.net/detail-bed2.png"]}', 'false', 'SPU-BED-001', 'false', 'brand-muji', 'cat-bed', NOW(), NOW()),
('prod-002', '天絲四件套床組',           'https://yanxuan-item.nosdn.127.net/1541e434c1e tried3e31aebdbf4ddb10c.png', '絲滑觸感，奢華享受',           '2599.00', '8500', '7.0', '215', '3699.00', '150',  NULL, NULL, '["https://yanxuan-item.nosdn.127.net/49223c0e48b2fe4dae498beaf91e2c17.png"]',                                            '920',  '215', '180', '{"pictures":["https://yanxuan-item.nosdn.127.net/detail-silk1.png"]}', 'false', 'SPU-BED-002', 'false', 'brand-muji', 'cat-bed', NOW(), NOW()),

-- 居家 > 廚房用品
('prod-003', '不鏽鋼真空保溫壺 1L',     'https://yanxuan-item.nosdn.127.net/f7c0e35f0e83a7256a7acf6a78f76f70.png', '長效保溫，居家外出必備',       '399.00',  '7200', '8.0', '456', '499.00',  '500',  NULL, NULL, '["https://yanxuan-item.nosdn.127.net/f7c0e35f0e83a7256a7acf6a78f76f70.png"]',                                            '3200', '456', '312', NULL, 'false', 'SPU-KIT-001', 'false', 'brand-hola', 'cat-kitchen', NOW(), NOW()),
('prod-004', '日式陶瓷碗盤套組 12 件',   'https://yanxuan-item.nosdn.127.net/a8c79c2df2faef0d74f17e2f3fc8cefc.png', '質感餐具，品味生活每一餐',     '899.00',  '6800', '7.5', '189', '1199.00', '300',  NULL, NULL, '["https://yanxuan-item.nosdn.127.net/a8c79c2df2faef0d74f17e2f3fc8cefc.png"]',                                            '1450', '189', '98',  NULL, 'false', 'SPU-KIT-002', 'false', 'brand-hola', 'cat-kitchen', NOW(), NOW()),

-- 美食 > 零食
('prod-005', '經典原味洋芋片 大包裝',   'https://yanxuan-item.nosdn.127.net/5f79a6ef6e96e2c7a517e2c7c38a800f.png', '酥脆口感，停不下來',           '59.00',   '9500', '9.0', '1024','69.00',   '2000', NULL, NULL, '["https://yanxuan-item.nosdn.127.net/5f79a6ef6e96e2c7a517e2c7c38a800f.png"]',                                            '8500', '1024','523', NULL, 'false', 'SPU-SNK-001', 'false', 'brand-lays',  'cat-snack', NOW(), NOW()),
('prod-006', '綜合堅果禮盒 500g',       'https://yanxuan-item.nosdn.127.net/89781be8a3d2a28eea4bf8cf5ee12b4d.png', '嚴選堅果，健康零嘴',           '399.00',  '8200', '8.5', '567', '499.00',  '800',  NULL, NULL, '["https://yanxuan-item.nosdn.127.net/89781be8a3d2a28eea4bf8cf5ee12b4d.png"]',                                            '4200', '567', '388', NULL, 'false', 'SPU-SNK-002', 'false', NULL,          'cat-snack', NOW(), NOW()),

-- 美食 > 飲品
('prod-007', '台灣高山烏龍茶 150g',     'https://yanxuan-item.nosdn.127.net/9c73c94ccb1c64b22cfb3c0ade5d8f37.png', '原葉茶香，回甘悠長',           '580.00',  '7600', '7.0', '342', '780.00',  '600',  NULL, NULL, '["https://yanxuan-item.nosdn.127.net/9c73c94ccb1c64b22cfb3c0ade5d8f37.png"]',                                            '2800', '342', '201', NULL, 'false', 'SPU-DRK-001', 'false', NULL,          'cat-drink', NOW(), NOW()),

-- 服飾 > 男裝
('prod-008', '純棉圓領 T-shirt',         'https://yanxuan-item.nosdn.127.net/3bb2e16a4c5183b1ebe74c62ad2f06bf.png', '百搭基本款，舒適好穿',         '299.00',  '9200', '7.5', '876', '399.00',  '1000', NULL, NULL, '["https://yanxuan-item.nosdn.127.net/3bb2e16a4c5183b1ebe74c62ad2f06bf.png"]',                                            '6700', '876', '445', NULL, 'false', 'SPU-MEN-001', 'false', 'brand-uniqlo','cat-men',   NOW(), NOW()),
('prod-009', '彈力修身牛仔褲',           'https://yanxuan-item.nosdn.127.net/6a7fc9b7d2b8f0c0cc180c70a0bab4d2.png', '經典版型，四季百搭',           '799.00',  '8800', '6.5', '534', '1199.00', '600',  NULL, NULL, '["https://yanxuan-item.nosdn.127.net/6a7fc9b7d2b8f0c0cc180c70a0bab4d2.png"]',                                            '3900', '534', '267', NULL, 'false', 'SPU-MEN-002', 'false', 'brand-uniqlo','cat-men',   NOW(), NOW()),

-- 服飾 > 女裝
('prod-010', '法式碎花連衣裙',           'https://yanxuan-item.nosdn.127.net/60d0d3efbce31aaaf498e1c359805d84.png', '浪漫碎花，優雅氣質',           '1099.00', '8600', '7.0', '423', '1599.00', '400',  NULL, NULL, '["https://yanxuan-item.nosdn.127.net/60d0d3efbce31aaaf498e1c359805d84.png"]',                                            '2100', '423', '310', NULL, 'false', 'SPU-WMN-001', 'false', NULL,          'cat-women', NOW(), NOW()),
('prod-011', '羊毛混紡大衣',             'https://yanxuan-item.nosdn.127.net/c598f93a89a7d6072cbab9c893eaf8f5.png', '質感大衣，溫暖整個冬天',       '2899.00', '7800', '6.0', '198', '4599.00', '200',  NULL, NULL, '["https://yanxuan-item.nosdn.127.net/c598f93a89a7d6072cbab9c893eaf8f5.png"]',                                            '850',  '198', '156', NULL, 'false', 'SPU-WMN-002', 'false', 'brand-uniqlo','cat-women', NOW(), NOW()),

-- 服飾 > 鞋靴
('prod-012', '真皮休閒運動鞋',           'https://yanxuan-item.nosdn.127.net/33cc6c5c42b27e42adbb4994a6736e0e.png', '舒適好走，每日穿搭',           '1599.00', '8100', '7.5', '312', '2199.00', '350',  NULL, NULL, '["https://yanxuan-item.nosdn.127.net/33cc6c5c42b27e42adbb4994a6736e0e.png"]',                                            '1800', '312', '189', NULL, 'false', 'SPU-SHO-001', 'false', NULL,          'cat-shoes', NOW(), NOW()),

-- 數碼 > 手機配件
('prod-013', 'MagSafe 磁吸無線充電器',   'https://yanxuan-item.nosdn.127.net/53994cd6050aa08f7e80aadd7ebfb875.png', '快速充電，吸附穩固',           '1290.00', '9100', '8.5', '645', '1490.00', '800',  NULL, NULL, '["https://yanxuan-item.nosdn.127.net/53994cd6050aa08f7e80aadd7ebfb875.png"]',                                            '5200', '645', '401', NULL, 'false', 'SPU-PHN-001', 'false', 'brand-apple', 'cat-phone', NOW(), NOW()),

-- 數碼 > 電腦週邊
('prod-014', '機械鍵盤 87 鍵青軸',       'https://yanxuan-item.nosdn.127.net/5c1d3b4a3a7e22b758624cce03498aef.png', '清脆手感，高效打字',           '1899.00', '7500', '7.0', '289', '2499.00', '250',  NULL, NULL, '["https://yanxuan-item.nosdn.127.net/5c1d3b4a3a7e22b758624cce03498aef.png"]',                                            '1600', '289', '178', NULL, 'false', 'SPU-CMP-001', 'false', NULL,          'cat-computer', NOW(), NOW()),

-- 美妝 > 護膚
('prod-015', '極潤保濕精華液 50ml',      'https://yanxuan-item.nosdn.127.net/354e2239de1032f4bcb4f4e3d72741e4.png', '深層保濕，水潤透亮',           '1680.00', '8900', '6.5', '723', '2580.00', '500',  NULL, NULL, '["https://yanxuan-item.nosdn.127.net/354e2239de1032f4bcb4f4e3d72741e4.png"]',                                            '3800', '723', '512', NULL, 'false', 'SPU-SKC-001', 'false', 'brand-shiseido','cat-skincare', NOW(), NOW()),

-- 美妝 > 彩妝
('prod-016', '絲絨霧面唇膏組合',        'https://yanxuan-item.nosdn.127.net/e11d8c0a0fa73e68788ecaba3950565c.png', '持久顯色，不乾不裂',           '890.00',  '8400', '7.5', '456', '1290.00', '700',  NULL, NULL, '["https://yanxuan-item.nosdn.127.net/e11d8c0a0fa73e68788ecaba3950565c.png"]',                                            '4100', '456', '334', NULL, 'false', 'SPU-MKP-001', 'false', 'brand-shiseido','cat-makeup', NOW(), NOW()),

-- 居家 > 床品
('prod-017', '太空記憶枕 慢回彈',        'https://yanxuan-item.nosdn.127.net/3dda16a0ad847a317e585cdee8bd1c3a.png', '護頸設計，深度好眠',           '599.00',  '9600', '5.5', '892', '1099.00', '600',  NULL, NULL, '["https://yanxuan-item.nosdn.127.net/3dda16a0ad847a317e585cdee8bd1c3a.png"]',                                            '9200', '892', '678', NULL, 'false', 'SPU-BED-003', 'false', 'brand-muji', 'cat-bed', NOW(), NOW()),

-- 居家 > 衛浴用品
('prod-018', '植物精油香氛蠟燭 200g',    'https://yanxuan-item.nosdn.127.net/f7c0e35f0e83a7256a7acf6a78f76f70.png', '天然植萃，舒緩放鬆',           '280.00',  '7400', '5.0', '345', '560.00',  '900',  NULL, NULL, '["https://yanxuan-item.nosdn.127.net/f7c0e35f0e83a7256a7acf6a78f76f70.png"]',                                            '6800', '345', '290', NULL, 'false', 'SPU-BTH-001', 'false', 'brand-hola', 'cat-bath', NOW(), NOW()),

-- 美食 > 零食
('prod-019', '手工曲奇餅乾禮盒 12入',   'https://yanxuan-item.nosdn.127.net/89781be8a3d2a28eea4bf8cf5ee12b4d.png', '奶香濃郁，送禮首選',           '320.00',  '8300', '5.5', '678', '580.00',  '1200', NULL, NULL, '["https://yanxuan-item.nosdn.127.net/89781be8a3d2a28eea4bf8cf5ee12b4d.png"]',                                            '7500', '678', '445', NULL, 'false', 'SPU-SNK-003', 'false', NULL,          'cat-snack', NOW(), NOW()),

-- 美食 > 飲品
('prod-020', '氣泡礦泉水 330ml x 24',   'https://yanxuan-item.nosdn.127.net/9c73c94ccb1c64b22cfb3c0ade5d8f37.png', '天然氣泡，清新解渴',           '480.00',  '8000', '6.0', '234', '780.00',  '400',  NULL, NULL, '["https://yanxuan-item.nosdn.127.net/9c73c94ccb1c64b22cfb3c0ade5d8f37.png"]',                                            '5600', '234', '167', NULL, 'false', 'SPU-DRK-002', 'false', NULL,          'cat-drink', NOW(), NOW()),

-- 服飾 > 男裝
('prod-021', '輕量防風連帽外套',         'https://yanxuan-item.nosdn.127.net/3bb2e16a4c5183b1ebe74c62ad2f06bf.png', '防風防潑水，戶外必備',         '1499.00', '9400', '5.0', '567', '2999.00', '350',  NULL, NULL, '["https://yanxuan-item.nosdn.127.net/3bb2e16a4c5183b1ebe74c62ad2f06bf.png"]',                                            '8800', '567', '401', NULL, 'false', 'SPU-MEN-003', 'false', 'brand-uniqlo','cat-men',   NOW(), NOW()),

-- 服飾 > 女裝
('prod-022', '柔軟親膚針織毛衣',         'https://yanxuan-item.nosdn.127.net/60d0d3efbce31aaaf498e1c359805d84.png', '溫柔觸感，秋冬百搭',           '899.00',  '8700', '5.5', '412', '1599.00', '500',  NULL, NULL, '["https://yanxuan-item.nosdn.127.net/60d0d3efbce31aaaf498e1c359805d84.png"]',                                            '6200', '412', '356', NULL, 'false', 'SPU-WMN-003', 'false', NULL,          'cat-women', NOW(), NOW()),

-- 數碼 > 電腦週邊
('prod-023', '真無線藍牙降噪耳機',       'https://yanxuan-item.nosdn.127.net/53994cd6050aa08f7e80aadd7ebfb875.png', '主動降噪，沉浸體驗',           '2490.00', '9300', '5.0', '834', '4980.00', '300',  NULL, NULL, '["https://yanxuan-item.nosdn.127.net/53994cd6050aa08f7e80aadd7ebfb875.png"]',                                            '7100', '834', '523', NULL, 'false', 'SPU-CMP-002', 'false', 'brand-apple', 'cat-computer', NOW(), NOW()),

-- 美妝 > 護膚
('prod-024', '清爽防曬乳 SPF50+ 60ml',  'https://yanxuan-item.nosdn.127.net/354e2239de1032f4bcb4f4e3d72741e4.png', '高效防曬，清爽不黏膩',         '450.00',  '8800', '5.5', '923', '820.00',  '800',  NULL, NULL, '["https://yanxuan-item.nosdn.127.net/354e2239de1032f4bcb4f4e3d72741e4.png"]',                                            '8100', '923', '612', NULL, 'false', 'SPU-SKC-002', 'false', 'brand-shiseido','cat-skincare', NOW(), NOW());

-- ============================================
-- 5. 輪播圖 (Banners)
-- ============================================
INSERT INTO `banners` (`id`, `imgUrl`, `categoryId`, `type`, `createdAt`, `updatedAt`) VALUES
('banner-001', 'https://yanxuan-item.nosdn.127.net/0fc3doea4c3de408a014ca2052623c1f.jpg', 'cat-home',    '1', NOW(), NOW()),
('banner-002', 'https://yanxuan-item.nosdn.127.net/c618f6e01a6f759e0fbe2c0af3bc26c0.jpg', 'cat-food',    '1', NOW(), NOW()),
('banner-003', 'https://yanxuan-item.nosdn.127.net/91da49c21d26b23459be4b1c3bb10f83.jpg', 'cat-cloth',   '1', NOW(), NOW()),
('banner-004', 'https://yanxuan-item.nosdn.127.net/10547dc38a6b18b2a56d2ae18dcb41ab.jpg', 'cat-digital', '1', NOW(), NOW()),
('banner-005', 'https://yanxuan-item.nosdn.127.net/02d0a1f9a8d6a39d5fefc85a685828f8.jpg', 'cat-beauty',  '1', NOW(), NOW());

-- ============================================
-- 6. 文章 (Posts)
-- ============================================
INSERT INTO `posts` (`title`, `content`, `createdAt`, `updatedAt`) VALUES
('2026 春季居家佈置攻略',   '春天到了，是時候給家裡換個新面貌！從色彩搭配到軟裝選擇，本文帶你打造溫馨舒適的春季居家空間。推薦使用淺色系床品搭配綠植，讓整個房間充滿生機。', NOW(), NOW()),
('健康零食怎麼選？營養師推薦', '想要吃零食又怕不健康？營養師推薦堅果、果乾等天然零食，既能滿足口腹之慾，又能補充營養。每天一小把堅果，對心血管健康大有益處。', NOW(), NOW()),
('男士春季穿搭指南',         '春季穿搭以層次感為重點，基本款 T-shirt 搭配薄外套是最實用的組合。選擇質感好的單品，簡單搭配就能穿出時尚感。', NOW(), NOW()),
('科技新品速報：最新無線充電技術', '新一代無線充電技術帶來更快的充電速度和更高的轉換效率。MagSafe 技術讓充電變得更加方便，手機放上即充，告別線材糾纏。', NOW(), NOW()),
('春季護膚攻略：換季保養重點',     '換季時肌膚容易敏感不穩定，保濕是最重要的基礎工作。推薦使用精華液搭配面霜，為肌膚打好水潤基底，再搭配溫和的防曬產品。', NOW(), NOW());

-- ============================================
-- 7. 規格 (Specs)
-- ============================================
INSERT INTO `specs` (`id`, `name`, `createdAt`, `updatedAt`) VALUES
('spec-color',    '顏色', NOW(), NOW()),
('spec-size',     '尺寸', NOW(), NOW()),
('spec-capacity', '容量', NOW(), NOW()),
('spec-flavor',   '口味', NOW(), NOW());

-- ============================================
-- 8. 規格值 (SpecValues)
-- ============================================
INSERT INTO `spec_values` (`id`, `value`, `picture`, `description`, `specId`, `createdAt`, `updatedAt`) VALUES
-- 顏色
('sv-white',     '米白色',   'https://yanxuan-item.nosdn.127.net/color-white.png',  '經典米白', 'spec-color', NOW(), NOW()),
('sv-gray',      '淺灰色',   'https://yanxuan-item.nosdn.127.net/color-gray.png',   '質感淺灰', 'spec-color', NOW(), NOW()),
('sv-navy',      '藏青色',   'https://yanxuan-item.nosdn.127.net/color-navy.png',   '沉穩藏青', 'spec-color', NOW(), NOW()),
('sv-black',     '黑色',     'https://yanxuan-item.nosdn.127.net/color-black.png',  '經典黑',   'spec-color', NOW(), NOW()),
('sv-pink',      '粉色',     'https://yanxuan-item.nosdn.127.net/color-pink.png',   '柔美粉',   'spec-color', NOW(), NOW()),
('sv-blue',      '天藍色',   'https://yanxuan-item.nosdn.127.net/color-blue.png',   '清爽天藍', 'spec-color', NOW(), NOW()),
('sv-red',       '紅色',     'https://yanxuan-item.nosdn.127.net/color-red.png',    '熱情紅',   'spec-color', NOW(), NOW()),
-- 尺寸
('sv-single',    '單人',     NULL, '150x200cm',       'spec-size', NOW(), NOW()),
('sv-double',    '雙人',     NULL, '180x200cm',       'spec-size', NOW(), NOW()),
('sv-queen',     '加大',     NULL, '200x220cm',       'spec-size', NOW(), NOW()),
('sv-s',         'S',        NULL, '適合身高155-165', 'spec-size', NOW(), NOW()),
('sv-m',         'M',        NULL, '適合身高160-170', 'spec-size', NOW(), NOW()),
('sv-l',         'L',        NULL, '適合身高165-175', 'spec-size', NOW(), NOW()),
('sv-xl',        'XL',       NULL, '適合身高170-180', 'spec-size', NOW(), NOW()),
('sv-39',        '39',       NULL, '腳長24.5cm',      'spec-size', NOW(), NOW()),
('sv-40',        '40',       NULL, '腳長25cm',        'spec-size', NOW(), NOW()),
('sv-41',        '41',       NULL, '腳長25.5cm',      'spec-size', NOW(), NOW()),
('sv-42',        '42',       NULL, '腳長26cm',        'spec-size', NOW(), NOW()),
-- 容量
('sv-50ml',      '50ml',     NULL, '體驗裝',  'spec-capacity', NOW(), NOW()),
('sv-100ml',     '100ml',    NULL, '正裝',    'spec-capacity', NOW(), NOW()),
('sv-200ml',     '200ml',    NULL, '家庭裝',  'spec-capacity', NOW(), NOW()),
-- 口味
('sv-original',  '原味',     NULL, NULL, 'spec-flavor', NOW(), NOW()),
('sv-bbq',       '燒烤味',   NULL, NULL, 'spec-flavor', NOW(), NOW()),
('sv-sourcream', '酸奶油洋蔥','NULL', NULL, 'spec-flavor', NOW(), NOW()),
('sv-green-tea', '抹茶味',   NULL, NULL, 'spec-flavor', NOW(), NOW());

-- ============================================
-- 9. SKU (庫存單位)
-- ============================================
INSERT INTO `skus` (`id`, `skuCode`, `price`, `oldPrice`, `inventory`, `picture`, `productId`, `createdAt`, `updatedAt`) VALUES
-- prod-001 日系純棉床包組 (顏色 x 尺寸)
('sku-001', 'BED001-WHT-SNG', '1299.00', '1999.00', '50',  'https://yanxuan-item.nosdn.127.net/sku-bed-white.png', 'prod-001', NOW(), NOW()),
('sku-002', 'BED001-WHT-DBL', '1599.00', '2299.00', '40',  'https://yanxuan-item.nosdn.127.net/sku-bed-white.png', 'prod-001', NOW(), NOW()),
('sku-003', 'BED001-GRY-SNG', '1299.00', '1999.00', '60',  'https://yanxuan-item.nosdn.127.net/sku-bed-gray.png',  'prod-001', NOW(), NOW()),
('sku-004', 'BED001-GRY-DBL', '1599.00', '2299.00', '50',  'https://yanxuan-item.nosdn.127.net/sku-bed-gray.png',  'prod-001', NOW(), NOW()),

-- prod-005 洋芋片 (口味)
('sku-005', 'SNK001-ORIG',    '59.00',   '69.00',   '800', 'https://yanxuan-item.nosdn.127.net/sku-chips-orig.png','prod-005', NOW(), NOW()),
('sku-006', 'SNK001-BBQ',     '59.00',   '69.00',   '600', 'https://yanxuan-item.nosdn.127.net/sku-chips-bbq.png', 'prod-005', NOW(), NOW()),
('sku-007', 'SNK001-SC',      '59.00',   '69.00',   '600', 'https://yanxuan-item.nosdn.127.net/sku-chips-sc.png',  'prod-005', NOW(), NOW()),

-- prod-008 純棉T-shirt (顏色 x 尺寸)
('sku-008', 'MEN001-WHT-M',   '299.00',  '399.00',  '150', 'https://yanxuan-item.nosdn.127.net/sku-tee-white.png', 'prod-008', NOW(), NOW()),
('sku-009', 'MEN001-WHT-L',   '299.00',  '399.00',  '120', 'https://yanxuan-item.nosdn.127.net/sku-tee-white.png', 'prod-008', NOW(), NOW()),
('sku-010', 'MEN001-BLK-M',   '299.00',  '399.00',  '180', 'https://yanxuan-item.nosdn.127.net/sku-tee-black.png', 'prod-008', NOW(), NOW()),
('sku-011', 'MEN001-BLK-L',   '299.00',  '399.00',  '130', 'https://yanxuan-item.nosdn.127.net/sku-tee-black.png', 'prod-008', NOW(), NOW()),
('sku-012', 'MEN001-NVY-M',   '299.00',  '399.00',  '100', 'https://yanxuan-item.nosdn.127.net/sku-tee-navy.png',  'prod-008', NOW(), NOW()),
('sku-013', 'MEN001-NVY-L',   '299.00',  '399.00',  '90',  'https://yanxuan-item.nosdn.127.net/sku-tee-navy.png',  'prod-008', NOW(), NOW()),

-- prod-012 真皮休閒運動鞋 (顏色 x 尺寸)
('sku-014', 'SHO001-WHT-40',  '1599.00', '2199.00', '80',  'https://yanxuan-item.nosdn.127.net/sku-shoe-white.png','prod-012', NOW(), NOW()),
('sku-015', 'SHO001-WHT-41',  '1599.00', '2199.00', '70',  'https://yanxuan-item.nosdn.127.net/sku-shoe-white.png','prod-012', NOW(), NOW()),
('sku-016', 'SHO001-BLK-40',  '1599.00', '2199.00', '90',  'https://yanxuan-item.nosdn.127.net/sku-shoe-black.png','prod-012', NOW(), NOW()),
('sku-017', 'SHO001-BLK-42',  '1599.00', '2199.00', '60',  'https://yanxuan-item.nosdn.127.net/sku-shoe-black.png','prod-012', NOW(), NOW()),

-- prod-015 極潤保濕精華液 (容量)
('sku-018', 'SKC001-50ML',    '1680.00', '2580.00', '300', 'https://yanxuan-item.nosdn.127.net/sku-serum-50.png',  'prod-015', NOW(), NOW()),
('sku-019', 'SKC001-100ML',   '2980.00', '4580.00', '200', 'https://yanxuan-item.nosdn.127.net/sku-serum-100.png', 'prod-015', NOW(), NOW()),

-- prod-016 絲絨霧面唇膏 (顏色)
('sku-020', 'MKP001-PINK',    '890.00',  '1290.00', '250', 'https://yanxuan-item.nosdn.127.net/sku-lip-pink.png',  'prod-016', NOW(), NOW()),
('sku-021', 'MKP001-RED',     '890.00',  '1290.00', '200', 'https://yanxuan-item.nosdn.127.net/sku-lip-red.png',   'prod-016', NOW(), NOW()),

-- prod-017 太空記憶枕 (顏色)
('sku-022', 'BED003-WHT',     '599.00',  '1099.00', '300', 'https://yanxuan-item.nosdn.127.net/sku-bed-white.png', 'prod-017', NOW(), NOW()),
('sku-023', 'BED003-GRY',     '599.00',  '1099.00', '300', 'https://yanxuan-item.nosdn.127.net/sku-bed-gray.png',  'prod-017', NOW(), NOW()),

-- prod-019 手工曲奇餅乾 (口味)
('sku-024', 'SNK003-ORIG',    '320.00',  '580.00',  '600', 'https://yanxuan-item.nosdn.127.net/sku-chips-orig.png','prod-019', NOW(), NOW()),
('sku-025', 'SNK003-GT',      '320.00',  '580.00',  '600', 'https://yanxuan-item.nosdn.127.net/sku-chips-orig.png','prod-019', NOW(), NOW()),

-- prod-021 防風外套 (顏色 x 尺寸)
('sku-026', 'MEN003-BLK-M',   '1499.00', '2999.00', '100', 'https://yanxuan-item.nosdn.127.net/sku-tee-black.png', 'prod-021', NOW(), NOW()),
('sku-027', 'MEN003-BLK-L',   '1499.00', '2999.00', '80',  'https://yanxuan-item.nosdn.127.net/sku-tee-black.png', 'prod-021', NOW(), NOW()),
('sku-028', 'MEN003-NVY-M',   '1499.00', '2999.00', '90',  'https://yanxuan-item.nosdn.127.net/sku-tee-navy.png',  'prod-021', NOW(), NOW()),
('sku-029', 'MEN003-NVY-L',   '1499.00', '2999.00', '80',  'https://yanxuan-item.nosdn.127.net/sku-tee-navy.png',  'prod-021', NOW(), NOW()),

-- prod-023 藍牙耳機 (顏色)
('sku-030', 'CMP002-BLK',     '2490.00', '4980.00', '150', 'https://yanxuan-item.nosdn.127.net/sku-shoe-black.png','prod-023', NOW(), NOW()),
('sku-031', 'CMP002-WHT',     '2490.00', '4980.00', '150', 'https://yanxuan-item.nosdn.127.net/sku-shoe-white.png','prod-023', NOW(), NOW()),

-- prod-024 防曬乳 (容量)
('sku-032', 'SKC002-50ML',    '450.00',  '820.00',  '400', 'https://yanxuan-item.nosdn.127.net/sku-serum-50.png',  'prod-024', NOW(), NOW()),
('sku-033', 'SKC002-100ML',   '780.00',  '1420.00', '400', 'https://yanxuan-item.nosdn.127.net/sku-serum-100.png', 'prod-024', NOW(), NOW());

-- ============================================
-- 10. SKU ↔ SpecValue 多對多關聯
-- ============================================
INSERT INTO `sku_spec_values` (`skuId`, `specValueId`, `createdAt`, `updatedAt`) VALUES
-- 床包組 SKU
('sku-001', 'sv-white',    NOW(), NOW()),
('sku-001', 'sv-single',   NOW(), NOW()),
('sku-002', 'sv-white',    NOW(), NOW()),
('sku-002', 'sv-double',   NOW(), NOW()),
('sku-003', 'sv-gray',     NOW(), NOW()),
('sku-003', 'sv-single',   NOW(), NOW()),
('sku-004', 'sv-gray',     NOW(), NOW()),
('sku-004', 'sv-double',   NOW(), NOW()),

-- 洋芋片 SKU
('sku-005', 'sv-original', NOW(), NOW()),
('sku-006', 'sv-bbq',      NOW(), NOW()),
('sku-007', 'sv-sourcream', NOW(), NOW()),

-- T-shirt SKU
('sku-008', 'sv-white',    NOW(), NOW()),
('sku-008', 'sv-m',        NOW(), NOW()),
('sku-009', 'sv-white',    NOW(), NOW()),
('sku-009', 'sv-l',        NOW(), NOW()),
('sku-010', 'sv-black',    NOW(), NOW()),
('sku-010', 'sv-m',        NOW(), NOW()),
('sku-011', 'sv-black',    NOW(), NOW()),
('sku-011', 'sv-l',        NOW(), NOW()),
('sku-012', 'sv-navy',     NOW(), NOW()),
('sku-012', 'sv-m',        NOW(), NOW()),
('sku-013', 'sv-navy',     NOW(), NOW()),
('sku-013', 'sv-l',        NOW(), NOW()),

-- 鞋子 SKU
('sku-014', 'sv-white',    NOW(), NOW()),
('sku-014', 'sv-40',       NOW(), NOW()),
('sku-015', 'sv-white',    NOW(), NOW()),
('sku-015', 'sv-41',       NOW(), NOW()),
('sku-016', 'sv-black',    NOW(), NOW()),
('sku-016', 'sv-40',       NOW(), NOW()),
('sku-017', 'sv-black',    NOW(), NOW()),
('sku-017', 'sv-42',       NOW(), NOW()),

-- 精華液 SKU
('sku-018', 'sv-50ml',     NOW(), NOW()),
('sku-019', 'sv-100ml',    NOW(), NOW()),

-- 唇膏 SKU
('sku-020', 'sv-pink',     NOW(), NOW()),
('sku-021', 'sv-black',    NOW(), NOW()),

-- 記憶枕 SKU
('sku-022', 'sv-white',    NOW(), NOW()),
('sku-023', 'sv-gray',     NOW(), NOW()),

-- 手工曲奇 SKU
('sku-024', 'sv-original', NOW(), NOW()),
('sku-025', 'sv-green-tea', NOW(), NOW()),

-- 防風外套 SKU
('sku-026', 'sv-black',    NOW(), NOW()),
('sku-026', 'sv-m',        NOW(), NOW()),
('sku-027', 'sv-black',    NOW(), NOW()),
('sku-027', 'sv-l',        NOW(), NOW()),
('sku-028', 'sv-navy',     NOW(), NOW()),
('sku-028', 'sv-m',        NOW(), NOW()),
('sku-029', 'sv-navy',     NOW(), NOW()),
('sku-029', 'sv-l',        NOW(), NOW()),

-- 藍牙耳機 SKU
('sku-030', 'sv-black',    NOW(), NOW()),
('sku-031', 'sv-white',    NOW(), NOW()),

-- 防曬乳 SKU
('sku-032', 'sv-50ml',     NOW(), NOW()),
('sku-033', 'sv-100ml',    NOW(), NOW());

-- ============================================
-- 完成！
-- ============================================
SELECT '✅ 種子資料插入完成！' AS result;
SELECT CONCAT('分類: ', COUNT(*)) AS summary FROM categories
UNION ALL
SELECT CONCAT('品牌: ', COUNT(*)) FROM brands
UNION ALL
SELECT CONCAT('商品: ', COUNT(*)) FROM products
UNION ALL
SELECT CONCAT('SKU:  ', COUNT(*)) FROM skus
UNION ALL
SELECT CONCAT('規格: ', COUNT(*)) FROM specs
UNION ALL
SELECT CONCAT('規格值: ', COUNT(*)) FROM spec_values
UNION ALL
SELECT CONCAT('輪播圖: ', COUNT(*)) FROM banners
UNION ALL
SELECT CONCAT('文章: ', COUNT(*)) FROM posts;
