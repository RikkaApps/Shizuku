package moe.shizuku.manager.adb

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import moe.shizuku.manager.MainActivity
import moe.shizuku.manager.R
import moe.shizuku.manager.ShizukuSettings
import moe.shizuku.manager.app.AppActivity
import moe.shizuku.manager.databinding.AdbSplitPairingActivityBinding

@RequiresApi(Build.VERSION_CODES.S_V2)
class AdbSplitPairingActivity : AppActivity() {

    private lateinit var binding: AdbSplitPairingActivityBinding
    private lateinit var discovery: AdbMdns
    private val keys = mutableListOf<MaterialButton>()
    private var port = -1
    private var code = ""
    private var pairing = false
    private var discoveryTimeout: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AdbSplitPairingActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())
            view.updatePadding(left = bars.left, top = bars.top, right = bars.right, bottom = bars.bottom)
            insets
        }
        ViewCompat.requestApplyInsets(binding.root)

        addKeypad()
        binding.settings.setOnClickListener { openSettings() }
        binding.notification.setOnClickListener {
            startActivity(Intent(this, AdbPairingTutorialActivity::class.java))
            finish()
        }

        discovery = AdbMdns(applicationContext, AdbMdns.TLS_PAIRING) { discoveredPort ->
            runOnUiThread {
                if (!isDestroyed && !isFinishing) {
                    port = discoveredPort
                    submitIfReady()
                }
            }
        }
        discovery.start()
        render()

        if (savedInstanceState == null && !isInMultiWindowMode) {
            binding.root.post { if (!isFinishing && !isDestroyed) openSettings() }
        }
    }

    override fun onMultiWindowModeChanged(isInMultiWindowMode: Boolean) {
        super.onMultiWindowModeChanged(isInMultiWindowMode)
        if (::binding.isInitialized) render()
    }

    private fun openSettings() {
        val wireless = Intent("android.settings.WIRELESS_DEBUGGING_SETTINGS")
        val developer = Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS).apply {
            putExtra(":settings:fragment_args_key", "toggle_adb_wireless")
            putExtra(":settings:show_fragment_args", Bundle().apply {
                putString(":settings:fragment_args_key", "toggle_adb_wireless")
            })
        }
        for (intent in listOf(wireless, developer)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK or Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT)
            try {
                startActivity(intent)
                return
            } catch (_: ActivityNotFoundException) {
                // Try the developer-options entry on devices without a direct entry.
            } catch (_: SecurityException) {
                // Some system builds restrict the direct entry.
            }
        }
        binding.status.setText(R.string.adb_split_pairing_settings_unavailable)
    }

    private fun addKeypad() {
        for (digits in listOf("123", "456", "789", " 0⌫")) {
            val row = LinearLayout(this)
            binding.keypad.addView(row, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(8) })
            digits.forEachIndexed { index, digit ->
                val params = LinearLayout.LayoutParams(0, dp(48), 1f).apply {
                    if (index > 0) marginStart = dp(8)
                }
                if (digit == ' ') {
                    row.addView(View(this), params)
                } else {
                    val key = layoutInflater.inflate(R.layout.adb_pairing_key, row, false) as MaterialButton
                    key.text = digit.toString()
                    if (digit == '⌫') key.contentDescription = getString(R.string.adb_split_pairing_delete)
                    key.setOnClickListener { editCode(digit) }
                    keys += key
                    row.addView(key, params)
                }
            }
        }
    }

    private fun editCode(digit: Char) {
        if (pairing) return
        discoveryTimeout?.cancel()
        discoveryTimeout = null
        binding.status.text = ""
        code = if (digit == '⌫') code.dropLast(1) else (code + digit).take(CODE_LENGTH)
        render()
        if (code.length < CODE_LENGTH) return
        if (port > 0) {
            submitIfReady()
        } else {
            binding.status.setText(R.string.dialog_adb_pairing_discovery)
            discoveryTimeout = lifecycleScope.launch {
                delay(10_000)
                discoveryTimeout = null
                code = ""
                binding.status.setText(R.string.adb_split_pairing_code_window)
                render()
            }
        }
    }

    private fun submitIfReady() {
        if (port <= 0 || code.length != CODE_LENGTH || pairing) return

        val pairingPort = port
        val pairingCode = code
        discoveryTimeout?.cancel()
        discoveryTimeout = null
        code = ""
        pairing = true
        binding.status.setText(R.string.notification_adb_pairing_working_title)
        render()

        lifecycleScope.launch {
            val result = try {
                withContext(Dispatchers.IO) {
                    val key = try {
                        AdbKey(PreferenceAdbKeyStore(ShizukuSettings.getPreferences()), "shizuku")
                    } catch (e: Exception) {
                        throw AdbKeyException(e)
                    }
                    AdbPairingClient("127.0.0.1", pairingPort, pairingCode, key).use { it.start() }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: AdbInvalidPairingCodeException) {
                pairingFailed(R.string.paring_code_is_wrong)
                return@launch
            } catch (_: AdbKeyException) {
                pairingFailed(R.string.adb_error_key_store)
                return@launch
            } catch (_: Exception) {
                pairingFailed(R.string.adb_split_pairing_code_window)
                return@launch
            }

            if (result) {
                Toast.makeText(this@AdbSplitPairingActivity, R.string.notification_adb_pairing_succeed_title, Toast.LENGTH_SHORT).show()
                startActivity(Intent(this@AdbSplitPairingActivity, MainActivity::class.java).addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                ))
                finishAndRemoveTask()
            } else {
                pairingFailed(R.string.notification_adb_pairing_failed_title)
            }
        }
    }

    private fun pairingFailed(message: Int) {
        pairing = false
        binding.status.setText(message)
        render()
    }

    private fun render() {
        binding.code.text = (0 until CODE_LENGTH).joinToString("  ") { index ->
            code.getOrNull(index)?.toString() ?: "–"
        }
        binding.code.contentDescription = getString(R.string.adb_split_pairing_progress, code.length)
        binding.instructions.isVisible = !isInMultiWindowMode
        binding.settings.isVisible = !isInMultiWindowMode
        binding.notification.isVisible = !isInMultiWindowMode
        binding.progress.isVisible = pairing
        keys.forEach { it.isEnabled = !pairing }
    }

    override fun onDestroy() {
        if (::discovery.isInitialized) discovery.stop()
        super.onDestroy()
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density + 0.5f).toInt()

    private companion object {
        const val CODE_LENGTH = 6
    }
}
