package com.kaalikiteeggi.three_of_spades

class GenericItemDescription(icons: Int=0, name: String, val imageUrl: String ="") {
    var imageID: Int = icons
    var textDescription: String = name

}
class WinnerItemDescription(val playerName: String, val imageUrl: String ="", val scored:Int=0, val target:Int=175, val points:Int=0) {

}