package com.chico.myhomebookkeeping.domain

import com.chico.myhomebookkeeping.db.dao.ParentCategoriesDao
import com.chico.myhomebookkeeping.db.entity.ParentCategories

object ParentCategoriesUseCase {
    suspend fun getAllParentCategoriesSortNameAsc(db: ParentCategoriesDao): List<ParentCategories> {
        return db.getAllParentCategoriesSortNameAsc()
    }

    suspend fun addNewParentCategory(
        db: ParentCategoriesDao,
        newParentCategory: ParentCategories
    ):Long {
        return db.addNewParentCategory(newParentCategory)
    }
}