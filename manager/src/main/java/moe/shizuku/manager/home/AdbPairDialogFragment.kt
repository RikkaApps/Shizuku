package moe.shizuku.manager.home

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import moe.shizuku.manager.R
import moe.shizuku.manager.ShizukuSettings
import moe.shizuku.manager.adb.AdbKey
import moe.shizuku.manager.adb.AdbPairingClient
import moe.shizuku.manager.adb.PreferenceAdbKeyStore
import moe.shizuku.manager.databinding.AdbPairDialogBinding
import moe.shizuku.manager.viewmodel.activityViewModels
import java.net.Inet4Address

class AdbPairDialogFragment : DialogFragment() {

    private lateinit var binding: AdbPairDialogBinding

    private val viewModel by activityViewModels { ViewModel() }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val context = requireContext()
        binding = AdbPairDialogBinding.inflate(LayoutInflater.from(context))

        val builder = AlertDialog.Builder(context).apply {
            setTitle(R.string.dialog_adb_pairing_title)
            setView(binding.root)
            setNegativeButton(android.R.string.cancel, null)
            setPositiveButton(android.R.string.ok, null)
        }
        val dialog = builder.create()
        dialog.setCanceledOnTouchOutside(false)
        dialog.setOnShowListener { onDialogShow(dialog) }
        return dialog
    }

    private fun onDialogShow(dialog: AlertDialog) {
        binding.port.editText!!.doAfterTextChanged {
            binding.port.error = null
        }

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val context = it.context
            val port = try {
                binding.port.editText!!.text.toString().toInt()
            } catch (e: Exception) {
                -1
            }
            if (port > 65535 || port < 1) {
                binding.port.error = context.getString(R.string.dialog_adb_invalid_port)
                return@setOnClickListener
            }

            val password = binding.pairingCode.editText!!.text.toString()

            var hostName: String? = null
            runBlocking {
                GlobalScope.launch(Dispatchers.IO) {
                    hostName = Inet4Address.getLoopbackAddress().hostName
                }.join()
            }

            viewModel.run(hostName!!, port, password)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    fun show(fragmentManager: FragmentManager) {
        if (fragmentManager.isStateSaved) return
        show(fragmentManager, javaClass.simpleName)
    }
}

@SuppressLint("NewApi")
private class ViewModel : androidx.lifecycle.ViewModel() {

    private val _result = MutableLiveData<Throwable?>()

    val result = _result as LiveData<Throwable?>

    fun run(host: String, port: Int, password: String) {
        GlobalScope.launch(Dispatchers.IO) {
            val key = AdbKey(PreferenceAdbKeyStore(ShizukuSettings.getPreferences()), "shizuku")

            AdbPairingClient(host, port, password, key).runCatching {
                start()
            }.onFailure {
                it.printStackTrace()
            }
        }
    }
}