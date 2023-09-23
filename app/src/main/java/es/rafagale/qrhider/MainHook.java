package es.rafagale.qrhider;


import android.app.AndroidAppHelper;
import android.content.Context;
import android.graphics.drawable.Drawable;

import java.util.List;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;


public class MainHook implements IXposedHookLoadPackage {

    public static final List<String> hookPackages = List.of(
            "org.mozilla.fennec_fdroid",
            "org.mozilla.firefox",
            "org.mozilla.firefox_beta",
            "org.mozilla.fenix"
    );

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) {
        if (hookPackages.contains(lpparam.packageName)) {
            try {
                XposedHelpers.findAndHookMethod("androidx.appcompat.content.res.AppCompatResources",
                        lpparam.classLoader, "getDrawable", Context.class, int.class, new XC_MethodHook() {
                            @Override
                            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                                int drawableResId = (int) param.args[1];
                                if (drawableResId == getDrawableResourceIdByName(lpparam.packageName)) {
                                    Drawable drawable = (Drawable) param.getResult();
                                    if (drawable != null) {
                                        drawable.setAlpha(0);
                                    }
                                }
                            }
                        });
            } catch (Exception ex) {
                XposedBridge.log("Unexpected error: " + ex);
            }
        }
    }

    private int getDrawableResourceIdByName(String packageName) {
        try {
            Context context = AndroidAppHelper.currentApplication().createPackageContext(packageName,
                    Context.CONTEXT_IGNORE_SECURITY);
            return context.getResources().getIdentifier("ic_qr", "drawable", packageName);
        } catch (Exception ex) {
            return 0;
        }
    }
}
