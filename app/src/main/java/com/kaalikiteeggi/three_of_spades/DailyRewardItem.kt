package com.kaalikiteeggi.three_of_spades

class DailyRewardItem {
    var imageID: Int? = 0
    var textDescription: String ?= null

    constructor(icons: Int?, name: String?){
        this.imageID = icons
        this.textDescription = name
    }
}