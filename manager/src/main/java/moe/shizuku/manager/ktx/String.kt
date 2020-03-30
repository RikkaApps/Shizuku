package moe.shizuku.manager.ktx

import android.text.Spanned
import rikka.html.text.HtmlCompat

fun CharSequence.toHtml(flags: Int = 0): Spanned {
    return HtmlCompat.fromHtml(this.toString(), flags)
}

fun CharSequence.toHtml(tagHandler: HtmlCompat.TagHandler): Spanned {
    return HtmlCompat.fromHtml(this.toString(), null, tagHandler)
}

fun CharSequence.toHtml(flags: Int, tagHandler: HtmlCompat.TagHandler): Spanned {
    return HtmlCompat.fromHtml(this.toString(), flags, null, tagHandler)
}