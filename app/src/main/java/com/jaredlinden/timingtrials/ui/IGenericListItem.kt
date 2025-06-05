package com.jaredlinden.timingtrials.ui

interface IGenericListItem {
    val item1: GenericListItemField
    val item2: GenericListItemField
    val item3: GenericListItemField
}

data class GenericListItemField(val text: String, val next: GenericListItemNext = GenericListItemNext())
data class GenericListItemNext(val itemType: String = "", val nextId: Long? =null)