package com.example.terramaster

data class Guide(
    val title: String = "",
    val steps: MutableList<Step> = mutableListOf()
)

data class Step(
    val title: String = "",
    val description: String = ""
)




