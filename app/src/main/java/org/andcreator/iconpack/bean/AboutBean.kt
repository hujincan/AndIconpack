package org.andcreator.iconpack.bean

data class AboutBean(val title: String,
                     val context: String,
                     val photo: Int,
                     val banner: Int,
                     val buttons: ArrayList<String>,
                     val links: ArrayList<String>,
                     val egg: String)