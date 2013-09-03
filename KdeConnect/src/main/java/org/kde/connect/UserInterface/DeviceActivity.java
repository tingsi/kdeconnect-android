package org.kde.connect.UserInterface;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import org.kde.connect.BackgroundService;
import org.kde.connect.Device;
import org.kde.connect.Plugins.Plugin;
import org.kde.connect.UserInterface.List.ButtonItem;
import org.kde.connect.UserInterface.List.ListAdapter;
import org.kde.connect.UserInterface.List.SectionItem;
import org.kde.kdeconnect.R;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class DeviceActivity extends ActionBarActivity {

    private String deviceId;
    private Device device;

    private Device.PluginsChangedListener pluginsChangedListener = new Device.PluginsChangedListener() {
        @Override
        public void onPluginsChanged(final Device device) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.e("MainActivity", "updateComputerList");

                    //Errors list
                    final HashMap<String, Plugin> failedPlugins = device.getFailedPlugins();
                    final String[] ids = failedPlugins.keySet().toArray(new String[failedPlugins.size()]);
                    String[] names = new String[failedPlugins.size()];
                    for(int i = 0; i < ids.length; i++) {
                        Plugin p = failedPlugins.get(ids[i]);
                        names[i] = p.getDisplayName();
                    }
                    ListView errorList = (ListView)findViewById(R.id.errors_list);
                    if (!failedPlugins.isEmpty() && errorList.getHeaderViewsCount() == 0) {
                        TextView header = new TextView(DeviceActivity.this);
                        header.setPadding(0,24,0,0);
                        header.setText("Plugins failed to load (tap for more info):"); //TODO: i18n
                        errorList.addHeaderView(header);
                    }
                    errorList.setAdapter(new ArrayAdapter<String>(DeviceActivity.this, android.R.layout.simple_list_item_1, names));
                    errorList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                            Plugin p = failedPlugins.get(ids[position - 1]); //Header is position 0, so we have to substract one
                            p.getErrorDialog(DeviceActivity.this).show();
                        }
                    });

                    //Buttons list
                    ArrayList<ListAdapter.Item> items = new ArrayList<ListAdapter.Item>();
                    final Collection<Plugin> plugins = device.getLoadedPlugins().values();
                    for (Plugin p : plugins) {
                        Button b = p.getInterfaceButton(DeviceActivity.this);
                        if (b != null) {
                            items.add(new SectionItem(p.getDisplayName()));
                            items.add(new ButtonItem(b));
                        }
                    }
                    ListView buttonsList = (ListView)findViewById(R.id.buttons_list);
                    buttonsList.setAdapter(new ListAdapter(DeviceActivity.this, items));

                }
            });

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE);
        actionBar.setDisplayHomeAsUpEnabled(true);

        deviceId = getIntent().getStringExtra("deviceId");

        BackgroundService.RunCommand(DeviceActivity.this, new BackgroundService.InstanceCallback() {
            @Override
            public void onServiceStart(BackgroundService service) {
                device = service.getDevice(deviceId);
                setTitle(device.getName());
                device.addPluginsChangedListener(pluginsChangedListener);
                pluginsChangedListener.onPluginsChanged(device);
            }
        });

    }

    @Override
    protected void onDestroy() {
        BackgroundService.RunCommand(DeviceActivity.this, new BackgroundService.InstanceCallback() {
            @Override
            public void onServiceStart(BackgroundService service) {
                Device device = service.getDevice(deviceId);
                device.removePluginsChangedListener(pluginsChangedListener);
            }
        });
        super.onDestroy();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.clear();
        if (device.isPaired()) {
            //TODO: i18n
            menu.add("Select plugins").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    Intent intent = new Intent(DeviceActivity.this, SettingsActivity.class);
                    intent.putExtra("deviceId", deviceId);
                    startActivity(intent);
                    return true;
                }
            });
            //TODO: i18n
            menu.add("Unpair").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    device.unpair();
                    finish();
                    return true;
                }
            });
            return true;
        } else {
            return false;
        }

    }
}
