package cn.cyanc.xposed.noregionlimits

import de.robv.android.xposed.*
import de.robv.android.xposed.IXposedHookZygoteInit.StartupParam
import de.robv.android.xposed.callbacks.XC_LoadPackage
import java.util.*
import com.highcapable.yukihookapi.hook.core.api.compat.

class HookMain : IXposedHookLoadPackage, IXposedHookZygoteInit {

    private val prefixFilteredList = mutableListOf(
        "android.",
        "android.view.",
        "androidx",
        "com.android.",
        "android.content.",
        "org.chromium.content.browser.",
        "com.google.",
        "com.google.android",
        "de.robv.android",
        "org.lsposed"
        )

    companion object {
        private fun getPref(path: String) : XSharedPreferences? {
            val pref = XSharedPreferences("cn.cyanc.xposed.noregionlimits", path)
            return if(pref.file.canRead()) pref else null
        }

        // lazy loads when needed
        val prefConf: XSharedPreferences? by lazy { getPref("conf") }
    }

    init {
        if (prefConf?.getBoolean("hack_android", false) == true){
            prefixFilteredList.remove("android.")
            prefixFilteredList.remove("com.android.")
        }
        if (prefConf?.getBoolean("hack_android_content", true) != false){
            prefixFilteredList.remove("android.view.")
            prefixFilteredList.remove("android.content.")
        }
        if (prefConf?.getBoolean("hack_androidx", false) == true){
            prefixFilteredList.remove("androidx.")
        }
        if (prefConf?.getBoolean("hack_google_components", true) != false){
            prefixFilteredList.remove("com.google.")
            prefixFilteredList.remove("com.google.android.")
        }
        if (prefConf?.getBoolean("hack_chromium", false) == true){
            prefixFilteredList.remove("org.chromium.content.browser.")
        }
        if (prefConf?.getBoolean("hack_framework", false) == true){
            prefixFilteredList.remove("de.robv.android")
            prefixFilteredList.remove("org.lsposed")
        }
    }

    private fun doNotHack(throwable: Throwable): Boolean {
        if (throwable.stackTrace.size >= 3) {
            val stackTraceElement = throwable.stackTrace[3]
            return prefixFilteredList.map(stackTraceElement.className::startsWith)
                .any { x -> x }
        }
        return false
    }

    class ReplaceLocale(private val doNotHack: (Throwable) -> Boolean) : XC_MethodHook() {
        override fun beforeHookedMethod(param: MethodHookParam) {
            val throwable = Throwable()
            if (doNotHack(throwable)) return
            param.result = prefConf?.getString("language", "jp")?.let { language ->
                val country = prefConf!!.getString("country", "ja")!!
                val variant = prefConf!!.getString("variant", "")!!
                Locale(
                    language,
                    country,
                    variant
                )
            }
        }
    }

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        XposedBridge.log("handleLoadPackage...")
        XposedHelpers.findAndHookMethod(
            "java.util.Locale",
            lpparam.classLoader,
            "getDefault",
            ReplaceLocale { throwable: Throwable -> doNotHack(throwable) }
        )

        XposedHelpers.findMethodExact(
            "cn.cyanc.xposed.noregionlimits.ModuleStatus\$Status",
            lpparam.classLoader,
            "isModuleEnabled"
        )

        XposedHelpers.findAndHookMethod(
            "cn.cyanc.xposed.noregionlimits.ModuleStatus\$Status",
            lpparam.classLoader,
            "isModuleEnabled",
            object : XC_MethodReplacement() {
                override fun replaceHookedMethod(param: MethodHookParam?): Boolean {
                    param?.result = true
                    return true
                }
            }
        )

        YukiHookAPI.
    }

    override fun initZygote(startupParam: StartupParam) {
        XposedBridge.log("Pass list: $prefixFilteredList")
        XposedBridge.log("initZygote: " + startupParam.modulePath)
        XposedHelpers.findAndHookMethod(
            "java.util.Locale",
            null,
            "initDefault",
            ReplaceLocale { throwable: Throwable -> doNotHack(throwable) }
        )
        XposedBridge.getXposedVersion()
    }

    /*override fun onHook() = encase {
        loadApp(isExcludeSelf = true) {
            findClass("java.util.Locale", appClassLoader=this).hook {
                injectMember {
                    method {
                        name = "getDefault"
                        emptyParam()
                    }

                    val throwable = Throwable()
                    if (doNotHack(throwable)) return@encase

                    beforeHook {
                        val throwable = Throwable()
                        if (doNotHack(throwable)) return@beforeHook
                        result = Locale.JAPAN
                    }
                }
            }
        }

        loadZygote {
            findClass("java.util.Locale").hook {
                injectMember {
                    method {
                        name = "initDefault"
                    }

                    val throwable = Throwable()
                    if (doNotHack(throwable)) return@encase

                    beforeHook {
                        val throwable = Throwable()
                        if (doNotHack(throwable)) return@beforeHook
                        result = Locale.JAPAN
                    }
                }
            }
        }
    }*/
}