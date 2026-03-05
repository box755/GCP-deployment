const db = require('./models/main.js');
const { v4: uuidv4 } = require('uuid');

async function seedData() {
    try {
        await db.sequelize.sync({ force: true });
        console.log("Database reset.");

        // 1. Create Categories
        const category1 = await db.Category.create({ id: uuidv4(), name: '居家', picture: 'https://via.placeholder.com/150?text=Home' });
        const category2 = await db.Category.create({ id: uuidv4(), name: '美食', picture: 'https://via.placeholder.com/150?text=Food' });
        const category3 = await db.Category.create({ id: uuidv4(), name: '服飾', picture: 'https://via.placeholder.com/150?text=Clothes' });

        // 2. Create Brands
        const brand1 = await db.Brand.create({
            id: uuidv4(),
            name: '極簡家居',
            nameEn: 'Muji Style',
            logo: 'https://via.placeholder.com/150?text=Logo',
            picture: 'https://via.placeholder.com/300x150?text=Brand',
            place: '台灣'
        });

        // 4. Create Product
        const product1 = await db.Product.create({
            id: uuidv4(),
            name: '日系簡約純棉床包組',
            desc: '舒適純棉，給您一夜好眠',
            price: '1299.00',
            oldPrice: '1999.00',
            discount: 6.5,
            picture: 'https://via.placeholder.com/300x300?text=Product',
            inventory: 100,
            salesCount: 50,
            commentCount: 12,
            collectCount: 5,
            video: '',
            mainPictures: JSON.stringify(['https://via.placeholder.com/400?text=Main1', 'https://via.placeholder.com/400?text=Main2']),
            isPreSale: false,
            isHot: true,
            categoryId: category1.id,
            brandId: brand1.id
        });

        // 5. Create Specs
        const specColor = await db.Spec.create({ id: uuidv4(), name: '顏色' });
        const specSize = await db.Spec.create({ id: uuidv4(), name: '尺寸' });

        // 6. Create SpecValues
        const valWhite = await db.SpecValue.create({ id: uuidv4(), value: '米白色', specId: specColor.id });
        const valGray = await db.SpecValue.create({ id: uuidv4(), value: '淺灰色', specId: specColor.id });
        const valSingle = await db.SpecValue.create({ id: uuidv4(), value: '單人', specId: specSize.id });
        const valDouble = await db.SpecValue.create({ id: uuidv4(), value: '雙人', specId: specSize.id });

        // 7. Create SKU 1 (White, Single)
        const sku1 = await db.SKU.create({
            id: uuidv4(),
            skuCode: 'P001-WHT-SNG',
            price: '1299.00',
            oldPrice: '1999.00',
            inventory: 50,
            picture: 'https://via.placeholder.com/150?text=White',
            productId: product1.id
        });
        await sku1.addSpecValues([valWhite, valSingle]);

        // 8. Create SKU 2 (Gray, Double)
        const sku2 = await db.SKU.create({
            id: uuidv4(),
            skuCode: 'P001-GRY-DBL',
            price: '1599.00',
            oldPrice: '2299.00',
            inventory: 50,
            picture: 'https://via.placeholder.com/150?text=Gray',
            productId: product1.id
        });
        await sku2.addSpecValues([valGray, valDouble]);

        // 9. Add Banners
        const banner1 = await db.Banner.create({
            id: uuidv4(),
            targetUrl: `/product/${product1.id}`,
            redirectUrl: `/product/${product1.id}`,
            imgUrl: 'https://via.placeholder.com/1200x400?text=Banner+1',
            type: '1'
        });

        const banner2 = await db.Banner.create({
            id: uuidv4(),
            targetUrl: `/category/${category1.id}`,
            redirectUrl: `/category/${category1.id}`,
            imgUrl: 'https://via.placeholder.com/1200x400?text=Banner+2',
            type: '2',
            categoryId: category1.id
        });

        console.log("Mock data inserted successfully!");
        process.exit(0);

    } catch (error) {
        console.error("Error inserting mock data:", error);
        process.exit(1);
    }
}

seedData();
