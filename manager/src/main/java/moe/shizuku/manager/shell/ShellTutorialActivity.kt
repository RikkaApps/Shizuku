package moe.shizuku.manager.shell

import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import moe.shizuku.manager.Helps
import moe.shizuku.manager.R
import moe.shizuku.manager.app.AppBarActivity
import moe.shizuku.manager.databinding.TerminalTutorialActivityBinding
import moe.shizuku.manager.ktx.toHtml
import moe.shizuku.manager.utils.CustomTabsHelper
import rikka.html.text.HtmlCompat
import rikka.insets.*
import kotlin.math.roundToInt

class ShellTutorialActivity : AppBarActivity() {

    companion object {

        private val SH_NAME = "rish"
        private val DEX_NAME = "rish_shizuku.dex"
    }

    private val openDocumentsTree =
        registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { tree: Uri? ->
            if (tree == null) return@registerForActivityResult

            val cr = contentResolver
            val doc = DocumentsContract.buildDocumentUriUsingTree(tree, DocumentsContract.getTreeDocumentId(tree))
            val child =
                DocumentsContract.buildChildDocumentsUriUsingTree(tree, DocumentsContract.getTreeDocumentId(tree))

            cr.query(
                child,
                arrayOf(DocumentsContract.Document.COLUMN_DOCUMENT_ID, DocumentsContract.Document.COLUMN_DISPLAY_NAME),
                null,
                null,
                null
            )?.use {
                while (it.moveToNext()) {
                    val id = it.getString(0)
                    val name = it.getString(1)
                    if (name == SH_NAME || name == DEX_NAME) {
                        DocumentsContract.deleteDocument(cr, DocumentsContract.buildDocumentUriUsingTree(tree, id))
                    }
                }
            }

            fun writeToDocument(name: String) {
                DocumentsContract.createDocument(contentResolver, doc, "application/octet-stream", name)?.runCatching {
                    cr.openOutputStream(this)?.let { assets.open(name).copyTo(it) }
                }
            }

            writeToDocument(SH_NAME)
            writeToDocument(DEX_NAME)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = TerminalTutorialActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.content.apply {
            setInitialPadding(
                initialPaddingLeft,
                initialPaddingTop + (resources.displayMetrics.density * 8).roundToInt(),
                initialPaddingRight,
                initialPaddingBottom
            )
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.apply {
            val shName = "<font face=\"monospace\">$SH_NAME</font>"
            val dexName = "<font face=\"monospace\">$DEX_NAME</font>"

            summary.text =
                getString(R.string.rish_description, shName).toHtml(HtmlCompat.FROM_HTML_OPTION_TRIM_WHITESPACE)

            text1.text = getString(R.string.terminal_tutorial_1, shName, dexName).toHtml()

            text2.text = getString(R.string.terminal_tutorial_2, shName).toHtml()
            summary2.text = getString(
                R.string.terminal_tutorial_2_description,
                "Termux",
                "<font face=\"monospace\">PKG</font>",
                "<font face=\"monospace\">com.termux</font>",
                "<font face=\"monospace\">com.termux</font>",
            ).toHtml()

            text3.text = getString(
                R.string.terminal_tutorial_3,
                "<font face=\"monospace\">sh $SH_NAME</font>",
            ).toHtml()
            summary3.text = getString(
                R.string.terminal_tutorial_3_description,
                shName, "<font face=\"monospace\">PATH</font>"
            ).toHtml()

            button1.setOnClickListener { openDocumentsTree.launch(null) }
            button2.setOnClickListener { v: View -> CustomTabsHelper.launchUrlOrCopy(v.context, Helps.RISH.get()) }
        }
    }
}
