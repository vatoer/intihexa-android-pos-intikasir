package id.stargan.intikasir.feature.product.data.mapper

import id.stargan.intikasir.data.local.entity.ProductEntity
import id.stargan.intikasir.data.local.entity.CategoryEntity
import id.stargan.intikasir.domain.model.Product
import id.stargan.intikasir.domain.model.Category

object ProductMapper {

    fun ProductEntity.toDomain(categoryName: String? = null): Product {
        return Product(
            id = this.id,
            name = this.name,
            sku = this.sku,
            barcode = this.barcode,
            categoryId = this.categoryId,
            categoryName = categoryName,
            description = this.description,
            price = this.price,
            cost = this.cost,
            stock = this.stock,
            minStock = this.minStock,
            lowStockThreshold = this.lowStockThreshold,
            imageUrl = this.imageUrl,
            isActive = !this.isDeleted && this.isActive,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt
        )
    }

    fun Product.toEntity(): ProductEntity {
        return ProductEntity(
            id = this.id,
            name = this.name,
            sku = this.sku,
            barcode = this.barcode,
            categoryId = this.categoryId,
            description = this.description,
            price = this.price,
            cost = this.cost ?: 0.0,
            stock = this.stock,
            minStock = this.minStock ?: 0,
            imageUrl = this.imageUrl,
            isActive = this.isActive,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
            lowStockThreshold = this.lowStockThreshold ?: 0
        )
    }

    fun CategoryEntity.toDomain(): Category {
        return Category(
            id = this.id,
            name = this.name,
            description = this.description,
            color = this.color,
            icon = this.icon,
            order = 0, // default value, entity tidak punya order
            isActive = true, // default value, entity tidak punya isActive
            createdAt = this.createdAt,
            updatedAt = this.updatedAt
        )
    }

    fun Category.toEntity(): CategoryEntity {
        return CategoryEntity(
            id = this.id,
            name = this.name,
            description = this.description,
            color = this.color,
            icon = this.icon,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt
        )
    }
}

// Jika error tetap muncul, pastikan modul feature memiliki dependency ke modul data di build.gradle
