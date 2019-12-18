package com.android.jared.linden.timingtrials.ui

interface IGenericListItem {
    val itemText1: String
    val itemText2: String
    val itemText3: String



}

interface IGenericListItemClick{
    val id:Long
    val itemType:Int
}