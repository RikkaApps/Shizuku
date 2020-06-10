package moe.shizuku.manager.starter

import android.content.Context
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
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
import moe.shizuku.manager.ShizukuSettings
import moe.shizuku.manager.adb.AdbClient
import moe.shizuku.manager.adb.AdbKey
import moe.shizuku.manager.adb.AdbKeyException
import moe.shizuku.manager.adb.PreferenceAdbKeyStore
import moe.shizuku.manager.app.AppBarActivity
import moe.shizuku.manager.databinding.StarterActivityBinding
import moe.shizuku.manager.viewmodel.Resource
import moe.shizuku.manager.viewmodel.Status
import moe.shizuku.manager.viewmodel.viewModels
import rikka.material.widget.BorderView
import java.net.ConnectException
import javax.net.ssl.SSLProtocolException

private class NotRootedException : Exception()

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
            val output = it.data!!.trim()
            binding.text1.text = output
            if (output.endsWith("info: shizuku_starter exit with 0")) {
                finish()
            } else if (it.status == Status.ERROR) {
                var message = 0
                when (it.error) {
                    is AdbKeyException -> {
                        message = R.string.adb_error_key_store
                    }
                    is NotRootedException -> {
                        message = R.string.start_with_root_failed
                    }
                    is ConnectException -> {
                        message = R.string.cannot_connect_port
                    }
                    is SSLProtocolException -> {
                        message = R.string.adb_pair_required
                    }
                }

                if (message != 0) {
                    AlertDialog.Builder(this)
                            .setMessage(message)
                            .setPositiveButton(android.R.string.ok, null)
                            .show()
                }
            }
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
    private val _output = MutableLiveData<Resource<StringBuilder>>()

    val output = _output as LiveData<Resource<StringBuilder>>

    init {
        if (root) {
            Starter.writeFiles(context)
            startRoot()
        } else {
            startAdb(host!!, port)
        }
    }

    private fun postResult(throwable: Throwable? = null) {
        if (throwable == null)
            _output.postValue(Resource.success(sb))
        else
            _output.postValue(Resource.error(throwable, sb))
    }

    private fun startRoot() {
        sb.append("Starting with root...").append('\n').append('\n')
        postResult()

        if (!Shell.rootAccess()) {
            sb.append("Can't start root shell.")
            return
        }

        Shell.su(Starter.command).to(object : CallbackList<String?>() {
            override fun onAddElement(s: String?) {
                sb.append(s).append('\n')
                postResult()
            }
        }).submit {
            if (it.code != 0) {
                sb.append('\n').append("Send this to developer may help solve the problem.")
                postResult()
            }
        }
    }

    private fun startAdb(host: String, port: Int) {
        sb.append("Starting with wireless adb...").append('\n').append('\n')

        GlobalScope.launch(Dispatchers.IO) {
            val key = try {
                AdbKey(PreferenceAdbKeyStore(ShizukuSettings.getPreferences()), "shizuku")
            } catch (e: Throwable) {
                e.printStackTrace()
                sb.append('\n').append(e.toString())

                postResult(AdbKeyException(e))
                return@launch
            }

            AdbClient(host, port, key).runCatching {
                connect()
                shellCommand(Starter.command) {
                    sb.append(String(it))
                    postResult()
                }
                close()
            }.onFailure {
                it.printStackTrace()

                sb.append('\n').append(it.toString())
                postResult(it)
            }
        }
    }
}