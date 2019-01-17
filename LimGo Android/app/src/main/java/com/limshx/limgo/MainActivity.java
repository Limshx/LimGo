package com.limshx.limgo;

import android.Manifest;
import android.app.AlertDialog;
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
import android.text.util.Linkify;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private DrawTable drawTable;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        drawTable = findViewById(R.id.paint_board);
        InfoBox.adb = new AlertDialog.Builder(context);
        InfoBox.inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        // API level 23以上需要申请权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && PackageManager.PERMISSION_GRANTED != checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        } else {
            if (initDirectoryFailed()) {
                drawTable.showMessage("创建主目录失败！");
            }
        }
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawTable.adapter.placeStone();
                drawTable.doRepaint();
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
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
                    drawTable.showMessage("创建主目录失败！");
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

    private String[] demos = {"AlphaGo Zero左右互搏"};

    private boolean download(String fileName) {
        try {
            StringBuilder stringBuilder = new StringBuilder();
            String[] strings = fileName.split(" ");
            for (int i = 0; i < strings.length; i++) {
                stringBuilder.append(URLEncoder.encode(strings[i], "UTF-8"));
                if (i != strings.length - 1) {
                    stringBuilder.append("%20");
                }
            }
            URL url = new URL("https://raw.githubusercontent.com/Limshx/LimGo/master/Demos/" + stringBuilder.toString());
            URLConnection urlConnection = url.openConnection();
            InputStream inputStream = urlConnection.getInputStream();
            FileOutputStream fileOutputStream = new FileOutputStream(homeDirectory + fileName);
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                System.out.println(bytesRead);
                fileOutputStream.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            // new Handler().post()也会“java.lang.RuntimeException: Can't create handler inside thread that has not called Looper.prepare()”
            drawTable.post(new Runnable() {
                @Override
                public void run() {
                    drawTable.showMessage("无法连接到GitHub！");
                }
            });
            return false;
        }
        return true;
    }

    private void downloadDemos() {
        new InfoBox("当前没有任何项目，下载演示项目？", "取消", "确定", null) {
            @Override
            void onNegative() {

            }

            @Override
            void onPositive() {
                drawTable.showMessage("正在下载演示项目...");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        for (String demo : demos) {
                            if (!download(demo)) {
                                return;
                            }
                        }
                        drawTable.showMessage("演示项目已下载完成！");
                    }
                }).start();
            }
        }.showDialog();
    }

    private int selectedItem;

    abstract class FileOperation {
        abstract void operateFile();

        void selectFile() {
            File[] files = new File(homeDirectory).listFiles();

            if (0 == files.length) {
                downloadDemos();
                return;
            }

            final String[] items = new String[files.length];
            selectedItem = 0;
            for (int i = 0; i < files.length; i++) {
                items[i] = files[i].getName();
                if (null != openedFile && openedFile.getName().equals(items[i])) {
                    selectedItem = i;
                }
            }
            InfoBox infoBox = new InfoBox(null, "取消", "确定", null) {
                @Override
                void onNegative() {

                }

                @Override
                void onPositive() {
                    openedFile = new File(homeDirectory + items[selectedItem]);
                    new InfoBox("导入 \"" + openedFile.getName() + "\" ？", "取消", "确定", null) {
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
            drawTable.showMessage("已导入 \"" + openedFile.getName() + "\"");
        }
    }

    private void exportToFile() {
        if (drawTable.adapter.setPlacedStonesToFile(openedFile)) {
            drawTable.showMessage("已导出 \"" + openedFile.getName() + "\"");
        }
    }

    private void deleteFile() {
        if (openedFile.delete()) {
            drawTable.showMessage("已删除 \"" + openedFile.getName() + "\"");
        }
    }

    private void clear() {
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
                final EditText editText = new EditText(context);
                InfoBox infoBox = new InfoBox("输入项目名：", "取消", "确定", editText) {
                    @Override
                    void onNegative() {

                    }

                    @Override
                    void onPositive() {
                        String fileName = editText.getText().toString();
                        if (!fileName.equals("")) {
                            openedFile = new File(homeDirectory + fileName);
                            if (openedFile.exists()) {
                                new InfoBox("项目 \"" + openedFile.getName() + "\" 已存在，覆盖？", "取消", "确定", null) {
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
                            drawTable.showMessage("项目名不能为空！");
                        }
                    }
                };
                infoBox.showDialog();
                if (openedFile != null) {
                    editText.setText(openedFile.getName());
                }
                break;
            case R.id.Clear:
                new InfoBox("清空工作区？", "取消", "确定", null) {
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
                    new InfoBox("删除 \"" + openedFile.getName() + "\" ？", "取消", "确定", null) {
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
                    drawTable.showMessage("请先导入项目！");
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

        menuItem[0] = menu.add(0, 0, 0, "跳转");
        menuItem[0].setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (-1 == drawTable.adapter.getImportedStonesNum()) {
                    drawTable.showMessage("请先导入项目！");
                    return false;
                }
                final EditText editText = new EditText(context);
                new InfoBox("0~" + drawTable.adapter.getImportedStonesNum() + "：", "取消", "确定", editText) {
                    @Override
                    void onNegative() {

                    }

                    @Override
                    void onPositive() {
                        try {
                            drawTable.adapter.jumpToStone(Integer.parseInt(editText.getText().toString()));
                            getAlertDialog().cancel();
                        } catch (NumberFormatException e) {
                            drawTable.showMessage("请输入整数！");
                        }
                    }
                }.showDialog(false);
                editText.setText("0");
                return true;
            }
        });

        menuItem[1] = menu.add(0, 0, 0, "随机");
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

        menuItem[2] = menu.add(0, 0, 0, "数目");
        menuItem[2].setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                drawTable.adapter.score();
                return true;
            }
        });

        menuItem[3] = menu.add(0, 0, 0, "帮助");
        menuItem[3].setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                TextView textView = new TextView(context);
                textView.setTextColor(Color.BLACK);
                textView.setAutoLinkMask(Linkify.ALL);
                String text = "在棋盘上选中一个空点，然后点主界面右下角的浮动按钮确认落子。\n\n在抽屉菜单中，点“导出”可以导出当前棋局，点“导入”可以导入保存的棋局。\n\n导入棋局后点工具菜单的“跳转”可以跳转到棋局中的某一手棋，这时候如果不选中空点直接点浮动按钮就会按照保存的棋局落子，否则进入试下模式，试下模式中不选择空点直接按浮动按钮则会继续按照保存的棋局落子。\n\nLimGo项目已在GitHub上以GPL-3.0开源，同时提供有安卓版和桌面版。\n\n项目地址：https://github.com/Limshx/LimGo";
                textView.setText(text);
                ScrollView scrollView = new ScrollView(context);
                scrollView.addView(textView);
                InfoBox infoBox = new InfoBox(null, "取消", "确定", scrollView) {
                    @Override
                    void onNegative() {

                    }

                    @Override
                    void onPositive() {

                    }
                };
                infoBox.showDialog();
                return true;
            }
        });

        return true;
    }

}
