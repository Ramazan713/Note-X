package com.masterplus.notex.roomdb.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "books")
data class Book(
    var name:String,
    @PrimaryKey(autoGenerate = true)
    var bookId:Long=0,
    var isVisibleItems:Boolean=true
)