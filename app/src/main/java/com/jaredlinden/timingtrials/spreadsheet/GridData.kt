package com.jaredlinden.timingtrials.spreadsheet

class GridData(val data: List<List<String>>){

    val numberOfColumns = data.first().size

    val columWidth = if(data.isNotEmpty())  (data.flatten().maxBy { it.length }?.length ?: 1) * 20 else 40

    val rowHeight = 40

    val rowMarkerWidth = 60

}