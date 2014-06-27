package org.kde.kdeconnect.Plugins.MousePadPlugin;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.Button;

import org.kde.kdeconnect.NetworkPackage;
import org.kde.kdeconnect.Plugins.Plugin;
import org.kde.kdeconnect_tp.R;

public class MousePadPlugin extends Plugin {
    @Override
    public String getPluginName() {
        return "plugin_mousepad";
    }

    @Override
    public String getDisplayName() {
        return context.getString(R.string.pref_plugin_mousepad);
    }

    @Override
    public String getDescription() {
        return context.getString(R.string.pref_plugin_mousepad_desc);
    }

    @Override
    public Drawable getIcon() {
        return context.getResources().getDrawable(R.drawable.icon);
    }

    @Override
    public boolean isEnabledByDefault() {
        return true;
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public boolean onPackageReceived(NetworkPackage np) {
        if (!np.getType().equals(NetworkPackage.PACKAGE_TYPE_MOUSEPAD)) return false;
        return true;
    }

    @Override
    public AlertDialog getErrorDialog(Context baseContext) { return null; }

    public void sendPoints(float dx, float dy) {
        NetworkPackage np = new NetworkPackage(NetworkPackage.PACKAGE_TYPE_MOUSEPAD);
        np.set("dx", dx);
        np.set("dy", dy);
        device.sendPackage(np);
    }

    public void sendSingleClick() {
        NetworkPackage np = new NetworkPackage(NetworkPackage.PACKAGE_TYPE_MOUSEPAD);
        np.set("singleclick", true);
        device.sendPackage(np);
    }

    public void sendDoubleClick() {
        NetworkPackage np = new NetworkPackage(NetworkPackage.PACKAGE_TYPE_MOUSEPAD);
        np.set("doubleclick", true);
        device.sendPackage(np);
    }

    @Override
    public Button getInterfaceButton(final Activity activity) {
        Button button = new Button(activity);
        button.setText(R.string.open_mousepad);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, MousePadActivity.class);
                intent.putExtra("deviceId", device.getDeviceId());
                activity.startActivity(intent);
            }
        });
        return button;
    }
}
