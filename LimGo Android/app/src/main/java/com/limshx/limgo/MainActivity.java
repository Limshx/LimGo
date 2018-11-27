package com.limshx.limgo;
import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private DrawTable drawTable;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // API level 23以上需要申请权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && PackageManager.PERMISSION_GRANTED != checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            requestPermissions(new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        } else {
            if (initDirectoryFailed()) {
                Toast.makeText(this, "Create path failed!", Toast.LENGTH_SHORT).show();
            }
        }

        context = this;

        drawTable = findViewById(R.id.paint_board);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawTable.adapter.placeStone();
            }
        });

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

//        这两句也能实现透明状态栏，但是是半透明之还有一定的alpha值
//        Window window = getWindow();
//        window.setStatusBarColor(Color.TRANSPARENT);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        } else {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        drawer.setFitsSystemWindows(false);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //这个0是requestCode，上面requestPermissions有用到
        // If request is cancelled, the result arrays are empty.
        if (requestCode == 0) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (initDirectoryFailed()) {
                    Toast.makeText(this, "Create path failed!", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            // 按下返回键后不销毁当前活动而是切入后台，以加速热启动实现秒开
            moveTaskToBack(false);
        }
    }

    private int selectedItem;

    abstract class FileOperation {
        abstract void operateFile();

        void selectFile() {
            File[] files = new File(homeDirectory).listFiles();

            if (files.length == 0) {
                Toast.makeText(context, "There are no projects.", Toast.LENGTH_SHORT).show();
                return;
            }

            final String[] items = new String[files.length];
            for (int i = 0; i < files.length; i++) {
                items[i] = files[i].getName();
            }
            selectedItem = 0;
            InfoBox infoBox = new InfoBox(null, "Cancel", "OK", null, context) {
                @Override
                void onNegative() {

                }
                @Override
                void onPositive() {
                    openedFile = new File(homeDirectory + items[selectedItem]);
                    new InfoBox(  "Import \"" + openedFile.getName() + "\" ?", "Cancel", "OK", null, context) {
                        @Override
                        void onNegative() {

                        }
                        @Override
                        void onPositive() {
                            operateFile();
                        }
                    }.showDialog();
                }
            };
            infoBox.getAdb().setSingleChoiceItems(items, selectedItem, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    selectedItem = i;
                }
            });
            infoBox.showDialog();
        }
    }

    private File openedFile;
    private final String homeDirectory = "/storage/emulated/0/LimGo/";

    private boolean initDirectoryFailed() {
        File file = new File(homeDirectory);
        return !file.exists() && !file.mkdir();
    }

    private void importFromFile() {
        if (drawTable.adapter.getPlacedStonesFromFile(openedFile)) {
            Toast.makeText(this, "Imported \"" + openedFile.getName() + "\"", Toast.LENGTH_SHORT).show();
        }
    }

    private void exportToFile() {
        if (drawTable.adapter.setPlacedStonesToFile(openedFile)) {
            Toast.makeText(this, "Exported \"" + openedFile.getName() + "\"", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteFile() {
        if (openedFile.delete()) {
            Toast.makeText(context, "Deleted \"" + openedFile.getName() + "\"", Toast.LENGTH_SHORT).show();
        }
    }

    private void clear() {
        openedFile = null;
        drawTable.adapter.init(true);
        drawTable.doRepaint();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.Import:
                new FileOperation() {
                    @Override
                    void operateFile() {
                        importFromFile();
                        drawTable.doRepaint();
                    }
                }.selectFile();
                break;
            case R.id.Export:
                InfoBox infoBox = new InfoBox("Input a file name :", "Cancel", "OK", new EditText(context), context) {
                    @Override
                    void onNegative() {

                    }
                    @Override
                    void onPositive() {
                        String fileName = ((EditText) getView()).getText().toString();
                        if (!fileName.equals("")) {
                            openedFile = new File(homeDirectory + fileName);
                            if (openedFile.exists()) {
                                new InfoBox("File \"" + openedFile.getName() + "\" exists, overwrite it?", "Cancel", "OK", null, context) {
                                    @Override
                                    void onNegative() {

                                    }
                                    @Override
                                    void onPositive() {
                                        exportToFile();
                                    }
                                }.showDialog();
                            } else {
                                exportToFile();
                            }
                        } else {
                            Toast.makeText(context, "The name of file can not be empty!", Toast.LENGTH_SHORT).show();
                        }
                    }
                };
                infoBox.showDialog();
                if (openedFile != null) {
                    ((EditText) infoBox.getView()).setText(openedFile.getName());
                }
                break;
            case R.id.Clear:
                new InfoBox("Close current project without saving?", "Cancel", "OK", null, context) {
                    @Override
                    void onNegative() {

                    }
                    @Override
                    void onPositive() {
                        clear();
                    }
                }.showDialog();
                break;
            case R.id.Delete:
                if (null != openedFile) {
                    new InfoBox("Delete \"" + openedFile.getName() + "\" ?", "Cancel", "OK", null, context) {
                        @Override
                        void onNegative() {

                        }
                        @Override
                        void onPositive() {
                            deleteFile();
                            clear();
                        }
                    }.showDialog();
                } else {
                    Toast.makeText(context, "Please import a project first!", Toast.LENGTH_SHORT).show();
                }
                break;
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuItem[] menuItem = new MenuItem[4];

//        menuItem[0] = menu.add(0, 0, 0, "Analyze");
//        menuItem[0].setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
//            @Override
//            public boolean onMenuItemClick(MenuItem item) {
//                drawTable.adapter.showScore = !drawTable.adapter.showScore;
//                drawTable.invalidate();
//                return true;
//            }
//        });

        menuItem[0] = menu.add(0, 0, 0, "Jump");
        menuItem[0].setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (-1 == drawTable.adapter.getImportedStonesNum()) {
                    Toast.makeText(context, "There are no imported stones!", Toast.LENGTH_SHORT).show();
                    return false;
                }

                new InfoBox("0~" + drawTable.adapter.getImportedStonesNum() + " :", "Cancel", "OK", new EditText(context), context) {
                    @Override
                    void onNegative() {

                    }
                    @Override
                    void onPositive() {
                        try {
                            drawTable.adapter.jumpToStone(Integer.parseInt(((EditText) getView()).getText().toString()));
                            getAlertDialog().cancel();
                        } catch (NumberFormatException e) {
                            Toast.makeText(context, "Please input an integer.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }.showDialog(false);
                return true;
            }
        });

        menuItem[1] = menu.add(0, 0, 0, "Random Play");
        menuItem[1].setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                // 不能在子线程中更新UI，又不能阻塞UI线程，看似无解。AsyncTask又太麻烦，也没用成功过。其实只要把更新UI的接口方法包在post()里即可，这里即showMessage()和doRepaint()。
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        drawTable.adapter.randomPlay();
                    }
                }).start();
                return true;
            }
        });

        menuItem[2] = menu.add(0, 0, 0, "Score");
        menuItem[2].setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                drawTable.adapter.score();
                return true;
            }
        });

        return true;
    }

}
