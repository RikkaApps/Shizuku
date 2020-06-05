package moe.shizuku.manager.starter

import android.content.Context
import android.os.Bundle
import android.view.MenuItem
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.observe
import com.topjohnwu.superuser.CallbackList
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import moe.shizuku.manager.AppConstants.EXTRA
import moe.shizuku.manager.R
import moe.shizuku.manager.adb.AdbClient
import moe.shizuku.manager.app.AppBarActivity
import moe.shizuku.manager.databinding.StarterActivityBinding
import moe.shizuku.manager.viewmodel.viewModels
import rikka.material.widget.BorderView

class StarterActivity : AppBarActivity() {

    private val viewModel by viewModels {
        ViewModel(this,
                intent.getBooleanExtra(EXTRA_IS_ROOT, true),
                intent.getStringExtra(EXTRA_HOST),
                intent.getIntExtra(EXTRA_PORT, 0))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        appBar?.setDisplayHomeAsUpEnabled(true)
        appBar?.setHomeAsUpIndicator(R.drawable.ic_close_24)

        val binding = StarterActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.list.borderVisibilityChangedListener = BorderView.OnBorderVisibilityChangedListener { top, _, _, _ ->
            appBar?.setRaised(!top)
        }

        viewModel.output.observe(this) {
            binding.text1.text = it.trim()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {

        const val EXTRA_IS_ROOT = "$EXTRA.IS_ROOT"
        const val EXTRA_HOST = "$EXTRA.HOST"
        const val EXTRA_PORT = "$EXTRA.PORT"
    }
}

private class ViewModel(context: Context, root: Boolean, host: String?, port: Int) : androidx.lifecycle.ViewModel() {

    private val sb = StringBuilder()
    private val _output = MutableLiveData(sb)

    private val rootFailedText = context.getString(R.string.start_with_root_failed)

    val output = _output as LiveData<StringBuilder>

    init {
        if (root) {
            startRoot()

            if (Starter.getCommand() == null) {
                Starter.writeFiles(context)
            }
        } else {
            startAdb(host!!, port)
        }
    }

    private fun notifyOutput() {
        _output.postValue(sb)
    }

    private fun startRoot() {
        if (!Shell.rootAccess()) {
            sb.append(rootFailedText)

            return
        }

        Shell.su(Starter.getCommand()).to(object : CallbackList<String?>() {
            override fun onAddElement(s: String?) {
                _output.value!!.append(s).append('\n')
                notifyOutput()
            }
        }).submit {
            if (it.code != 0) {
                sb.append('\n').append("Send this to developer may help solve the problem.")
                notifyOutput()
            }
        }
    }

    private fun startAdb(host: String, port: Int) {
        GlobalScope.launch(Dispatchers.IO) {
            AdbClient(host, port).runCatching {
                connect()
                shellCommand(Starter.getCommand()) {
                    sb.append(String(it))
                    notifyOutput()
                }
                close()
            }.onFailure {
                sb.append('\n')
                        .append(it.toString())
                /*.append("\n\n")
                .append("Send this to developer may help solve the problem.")*/
                notifyOutput()
            }
        }
    }
}