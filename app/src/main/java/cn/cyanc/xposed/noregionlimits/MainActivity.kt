package cn.cyanc.xposed.noregionlimits

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.view.WindowCompat
import androidx.core.widget.addTextChangedListener
import cn.cyanc.xposed.noregionlimits.databinding.ActivityMainBinding
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import java.util.*

class LocaleAdapter(private val context: Context, private val onClickView: (Locale, View) -> Unit) :
    BaseAdapter(), Filterable {

    var items: List<Locale>

    // A copy of the original items list for filtering
    private var originalItems: List<Locale>? = null

    private val lock = Any()

    init {
        items = Locale.getAvailableLocales().toList()
    }

    private val filter = object : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val results = FilterResults()
            if (constraint.isNullOrEmpty()) {
                // No filter, return the whole list
                synchronized(lock) {
                    results.values = originalItems
                    results.count = originalItems?.size ?: 0
                }
            } else {
                // Perform the filtering based on the constraint
                val filterString = constraint.toString().lowercase(Locale.getDefault())
                val filteredList = mutableListOf<Locale>()
                synchronized(lock) {
                    originalItems?.forEach { item ->
                        if (item.country.lowercase(Locale.getDefault()).contains(filterString)
                            || item.language.lowercase(Locale.getDefault()).contains(filterString)
                            || item.displayLanguage.lowercase(Locale.getDefault())
                                .contains(filterString)
                            || item.displayCountry.lowercase(Locale.getDefault())
                                .contains(filterString)
                            || item.toLanguageTag().lowercase(Locale.getDefault())
                                .contains(filterString)
                        ) {
                            filteredList.add(item)
                        }
                    }
                }
                results.values = filteredList
                results.count = filteredList.size
            }
            return results
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            // Update the items list with the filtered results and notify the UI
            val values = results?.values
            if (values is List<*>) {
                items = values.filterIsInstance<Locale>()
                notifyDataSetChanged()
            }
        }
    }

    init {
        originalItems = items
    }

    override fun getCount(): Int = items.count()

    override fun getItem(position: Int): Locale = items[position]

    override fun getItemId(position: Int): Long = position.toLong()

    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = LayoutInflater.from(this.context)
            .inflate(R.layout.lang_sel_lv_item, parent, false)

        val locale = this.getItem(position)

        view.findViewById<TextView>(R.id.lslv_item_country).text = locale.country
        view.findViewById<TextView>(R.id.lslv_item_lang).text = locale.language
        view.findViewById<TextView>(R.id.lslv_item_country_display).text = locale.displayCountry
        view.findViewById<TextView>(R.id.lslv_item_lang_display).text = locale.displayLanguage
        view.findViewById<TextView>(R.id.lslv_item_ltag).text = locale.toLanguageTag()
        view.setOnClickListener {
            this.onClickView(locale, it)
        }

        return view
    }

    override fun getFilter(): Filter {
        return filter
    }

}

class ModuleStatus {
    companion object Status {
        private fun isModuleEnabled() = false
        val moduleEnabled by lazy(::isModuleEnabled)
        private fun getXposedVersion() = -1
        val xposedVersion by lazy(::getXposedVersion)
        private fun getXposedExecutorName() = "unknown"
        val xposedExecutorName by lazy(::getXposedExecutorName)
    }
}

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    @SuppressLint("WorldReadableFiles")
    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        @Suppress("DEPRECATION") val pref = try {
            this.getSharedPreferences("conf", Context.MODE_WORLD_READABLE)
        } catch (e: SecurityException) {
            // The new XSharedPreferences is not enabled or module's not loading
            null // other fallback, if any
        }

        val lv = findViewById<ListView>(R.id.langSelListView)
        lv.adapter = LocaleAdapter(this) { locale, _ ->
            pref?.edit {
                putString("language", locale.language)
                putString("country", locale.country)
                putString("variant", locale.variant)
                Toast.makeText(
                    this@MainActivity,
                    "Selected: ${locale.language}-${locale.country}!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        findViewById<MaterialCardView>(R.id.moduleStatusCard).apply {
            val enabled = ModuleStatus.moduleEnabled
            fun resolveBackground() =
                getColor(if (enabled) R.color.moduleCardViewBackgroundEnabled else R.color.moduleCardViewBackgroundNotEnabled)

            fun resolveForeground() =
                getColor(if (enabled) R.color.moduleCardViewForegroundEnabled else R.color.moduleCardViewForegroundNotEnabled)
            setCardBackgroundColor(resolveBackground())
            findViewById<ImageView>(R.id.statusIcon).apply {
                setColorFilter(resolveForeground())
                if (enabled) setImageResource(R.drawable.ic_verified)
                else setImageResource(R.drawable.ic_error)
            }
            findViewById<TextView>(R.id.moduleStatusText).apply {
                setText(
                    if (ModuleStatus.moduleEnabled) R.string.module_status_enabled else R.string.module_status_not_enabled
                )
                setTextColor(resolveForeground())
            }
            findViewById<TextView>(R.id.moduleVersionText).apply {
                text = resources.getQuantityString(
                    R.plurals.module_version_format,
                    2,
                    BuildConfig.VERSION_NAME,
                    BuildConfig.VERSION_CODE
                )
                setTextColor(resolveForeground())
            }
            findViewById<TextView>(R.id.moduleFrameworkInfoText).apply {
                setTextColor(resolveForeground())
                val ver = ModuleStatus.xposedVersion
            }
        }

        findViewById<TextInputLayout>(R.id.searchInput).addOnEditTextAttachedListener {
            it.editText?.addTextChangedListener { edit ->
                (lv.adapter as LocaleAdapter).filter.filter(edit)
            }
        }

        binding.fab.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.app_name)
                .setMessage(
                    "${getString(R.string.app_desc)}\n" +
                            "Author: ${getString(R.string.author)}"
                )
                .show()
        }

        if (!ModuleStatus.moduleEnabled)
            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.module_not_enable_alert_title)
                .setMessage(R.string.module_not_enabled_alert_message)
                .setPositiveButton(android.R.string.ok) { dialogInterface: DialogInterface, _: Int -> dialogInterface.dismiss() }
                .show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.top_app_bar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.menu_settings -> {
                if (!ModuleStatus.moduleEnabled) return false
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}