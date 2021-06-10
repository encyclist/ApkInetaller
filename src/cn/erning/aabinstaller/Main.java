package cn.erning.aabinstaller;

import cn.erning.aabinstaller.entity.Device;
import cn.erning.aabinstaller.exit.ExitException;
import cn.erning.aabinstaller.exit.NoExitSecurityManager;
import cn.erning.aabinstaller.util.Installer;
import cn.erning.aabinstaller.util.PropertiesUtil;
import cn.erning.aabinstaller.view.ConsolePane;
import cn.erning.aabinstaller.view.JTextFieldHintListener;
import cn.erning.aabinstaller.view.MyFileFilter;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.io.PrintStream;
import java.util.Map;

import static cn.erning.aabinstaller.util.PropertiesUtil.*;

/**
 * @author erning
 * @date 2021-06-08 16:10
 * des:
 */
public class Main {
    private static final int SCREEN_WIDTH = 1920;
    private static final int SCREEN_HEIGHT = 1080;
    private static final int WIDTH = 600;
    private static final int HEIGHT = 400;
    private static final int WIDTH_SHADOW = 14; // 窗口阴影的大小
    private static final int HEIGHT_SHADOW = 38; // 窗口阴影的大小

    // 用来阻止exit
    private static final NoExitSecurityManager noExitSecurityManager = new NoExitSecurityManager();
    private static final PrintStream printErrStream = System.err;

    /**
     * 程序运行
     */
    public static void main(String[] args) {
        // 拦截exit
        System.setSecurityManager(noExitSecurityManager);
        noExitSecurityManager.exitFilter = false;

        initView();
    }

    /**
     * 初始化页面
     */
    private static void initView() {
        JFrame f = new JFrame("APK安装器");
        f.setSize(WIDTH + WIDTH_SHADOW, HEIGHT + HEIGHT_SHADOW);//设置容器尺寸
        f.setMinimumSize(new Dimension(WIDTH + WIDTH_SHADOW, HEIGHT + HEIGHT_SHADOW)); // 设置最小大小
        f.setLocation((SCREEN_WIDTH - WIDTH) / 2, (SCREEN_HEIGHT - HEIGHT) / 2);//设置容器位置
        f.setLayout(null);//设置布局。
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);//界面关闭后程序结束
//        f.setResizable(false); // 不允许调整大小
//        f.setAlwaysOnTop(true);
        addView(f);
        f.setVisible(true);//界面可视化。
    }

    /**
     * 添加子控件
     */
    private static void addView(JFrame f) {
        // 获取已保存的配置
        Map<String, String> prop = PropertiesUtil.read();
        // 首先添加日志输出框
        addConsolePane(f);
        // 添加 文本框 安装包路径
        JLabel apkPathLabel = new JLabel("");
        if(prop.containsKey(KEY_APK_PATH)){
            apkPathLabel.setText(prop.get(KEY_APK_PATH));
        }
        apkPathLabel.setBounds(200, 10, WIDTH - 10 - 200, 30);
        f.add(apkPathLabel);
        // 添加 按钮 选择安装包(aab或apks)
        JButton apkButton = new JButton("选择安装包");
        apkButton.setBounds(10, 10, 180, 30);//设置按钮在容器中的位置
        apkButton.addActionListener(e -> {
            File file = selectFile(PropertiesUtil.get(KEY_APK_PATH),"aab", "apks", "apk");
            if (file != null) {
                PropertiesUtil.put(KEY_APK_PATH,file.getAbsolutePath());
                apkPathLabel.setText(file.getAbsolutePath());
            }
        });
        f.add(apkButton);//将按钮加在容器上
        // 添加 文本框 密钥文件路径
        JLabel jksPathLabel = new JLabel("");
        if(prop.containsKey(KEY_JKS_PATH)){
            jksPathLabel.setText(prop.get(KEY_JKS_PATH));
        }
        jksPathLabel.setBounds(200, 50, WIDTH - 10 - 200, 30);
        f.add(jksPathLabel);
        // 添加 按钮 选择密钥
        JButton jksButton = new JButton("选择密钥(aab用)");
        jksButton.setBounds(10, 50, 180, 30);
        jksButton.addActionListener(e -> {
            File file = selectFile(PropertiesUtil.get(KEY_JKS_PATH),"jks");
            if (file != null) {
                PropertiesUtil.put(KEY_JKS_PATH,file.getAbsolutePath());
                jksPathLabel.setText(file.getAbsolutePath());
            }
        });
        f.add(jksButton);
        // 添加 输入框 文件密码
        JTextField jksPassTextField = new JTextField();
        jksPassTextField.setBounds(10, 90, WIDTH - 10 - 10, 30);
        jksPassTextField.addFocusListener(new JTextFieldHintListener("签名文件密码(aab用)", jksPassTextField));
        if(prop.containsKey(KEY_JKS_PASS)){
            jksPassTextField.setText(prop.get(KEY_JKS_PASS));
        }
        f.add(jksPassTextField);
        // 添加 输入框 别名
        JTextField jksAliasTextField = new JTextField();
        jksAliasTextField.setBounds(10, 130, WIDTH - 10 - 10, 30);
        jksAliasTextField.addFocusListener(new JTextFieldHintListener("密钥别名(aab用)", jksAliasTextField));
        if(prop.containsKey(KEY_JKS_ALIAS)){
            jksAliasTextField.setText(prop.get(KEY_JKS_ALIAS));
        }
        f.add(jksAliasTextField);
        // 添加 输入框 别名密码
        JTextField jksAliasPassTextField = new JTextField();
        jksAliasPassTextField.setBounds(10, 170, WIDTH - 10 - 10, 30);
        jksAliasPassTextField.addFocusListener(new JTextFieldHintListener("密钥密码(aab用)", jksAliasPassTextField));
        if(prop.containsKey(KEY_JKS_ALIAS_PASS)){
            jksAliasPassTextField.setText(prop.get(KEY_JKS_ALIAS_PASS));
        }
        f.add(jksAliasPassTextField);
        // 添加 按钮 aab转apks
        JButton convertButton = new JButton("aab转apks");
        convertButton.setBounds(10, 210, 100, 30);
        convertButton.addActionListener(e -> {
            String aabPath = apkPathLabel.getText();
            String jksPath = jksPathLabel.getText();
            String jksPass = jksPassTextField.getText();
            String jksAlias = jksAliasTextField.getText();
            String jksAliasPass = jksAliasPassTextField.getText();
            PropertiesUtil.put(KEY_JKS_PASS,jksPass);
            PropertiesUtil.put(KEY_JKS_ALIAS,jksAlias);
            PropertiesUtil.put(KEY_JKS_ALIAS_PASS,jksAliasPass);
            buildApks(f, aabPath, jksPath, jksPass, jksAlias, jksAliasPass);
        });
        f.add(convertButton);
        // 添加 下拉框 设备列表
        JComboBox<String> jcb1 = new JComboBox<>();
        refreshDeviceListBox(jcb1);
        jcb1.setBounds(120, 210, 200, 30);
        f.add(jcb1);
        // 添加 按钮 刷新设备列表
        JButton refreshButton = new JButton();
        refreshButton.setBounds(320,210,30,30);
        Icon icon = new ImageIcon("src/res/ic_refresh.png","刷新");
        refreshButton.setIcon(icon);
        refreshButton.addActionListener(e -> {
            refreshDeviceListBox(jcb1);
        });
        f.add(refreshButton);
        // 添加 按钮 安装
        JButton installButton = new JButton("安装(apk/apks)");
        installButton.setBounds(360, 210, 140, 30);
        installButton.addActionListener(e -> {
            int index = jcb1.getSelectedIndex();
            Device device = null;
            if (index != 0){
                device = Installer.getDevices()[index-1];
            }
            String apksPath = apkPathLabel.getText();
            installApks(f, apksPath,device);
        });
        f.add(installButton);
    }

    /**
     * 刷新设备列表
     */
    private static void refreshDeviceListBox(JComboBox<String> jcb1){
        Device[] devices = Installer.getDevices();
        String[] jg = new String[devices.length+1];
        for (int i=0;i< jg.length;i++){
            if(i == 0){
                jg[i] = "选择要安装的设备";
            }else{
                jg[i] = devices[i-1].getStr();
            }
        }

        jcb1.setModel(new DefaultComboBoxModel<>(jg));
    }

    /**
     * 添加日志框
     */
    private static void addConsolePane(JFrame f){
        // 日志输出窗口
        JScrollPane jScrollPane = ConsolePane.getInstance();
        jScrollPane.setBounds(10, 260, WIDTH - 10 - 10, HEIGHT - 260 - 10);
        f.add(jScrollPane);
        // 窗口大小改变时更改日志输出区大小
        f.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent evt) {
                Dimension size = f.getSize();
                jScrollPane.setBounds(10, 260, (int) size.getWidth() - 10 - 10 - WIDTH_SHADOW, (int) size.getHeight() - 260 - 10 - HEIGHT_SHADOW);
            }
        });
        f.addWindowStateListener(e -> {
            if (e.getOldState() != e.getNewState()) {
                switch (e.getNewState()) {
                    case Frame.MAXIMIZED_VERT://最大化
                    case Frame.MAXIMIZED_BOTH://最大化
                    case Frame.ICONIFIED://最小化
                    case Frame.NORMAL://恢复
                        jScrollPane.setBounds(10, 260, f.getWidth() - 10 - 10, f.getHeight() - 260 - 10 );
                    default:
                        break;
                }
            }
        });
    }

    /**
     * 生成apks文件
     */
    private static void buildApks(JFrame f, String aabPath, String jksPath, String jksPass, String jksAlias, String jksAliasPass) {
        try {
            noExitSecurityManager.exitFilter = true;
            Installer.buildApks(aabPath, jksPath, jksPass, jksAlias, jksAliasPass);
        } catch (ExitException e) {
            showInfoDialog(f, "完成");
        } catch (Exception e) {
            e.printStackTrace(printErrStream);
            showErrorDialog(f, e);
        } finally {
            noExitSecurityManager.exitFilter = false;
            Installer.resetAdbServer();
        }
    }

    /**
     * 安装apks/apk文件
     * @param device 要安装的设备：为null则不指定，由ADB选择
     */
    private static void installApks(JFrame f, String aabPath,Device device) {
        try {
            if(aabPath.endsWith("apk")){
                Installer.installApk(aabPath,device);
            }else{
                noExitSecurityManager.exitFilter = true;
                Installer.installApks(aabPath,device);
            }
        } catch (ExitException e) {
            showInfoDialog(f, "完成");
        } catch (Exception e) {
            e.printStackTrace(printErrStream);
            showErrorDialog(f, e);
        } finally {
            if(!aabPath.endsWith("apk")){
                Installer.resetAdbServer();
                noExitSecurityManager.exitFilter = false;
            }
        }
    }

    /**
     * 显示错误弹窗
     */
    private static void showErrorDialog(JFrame jf, Exception ex) {
        System.err.println(ex.getMessage());
        JOptionPane.showMessageDialog(
                jf,
                ex.getMessage(),
                "错误",
                JOptionPane.ERROR_MESSAGE
        );
    }

    /**
     * 显示提示弹窗
     */
    private static void showInfoDialog(JFrame jf, String message) {
        JOptionPane.showMessageDialog(
                jf,
                message,
                "成功",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    /**
     * 选择文件并返回
     * @param suffixs 允许的文件格式
     */
    private static File selectFile(String defaultPath,String... suffixs) {
        JFileChooser jfc = new JFileChooser();
        //设置当前路径为桌面路径,否则将我的文档作为默认路径
        if(defaultPath != null){
            File file = new File(defaultPath);
            if(file.exists()){
                jfc.setCurrentDirectory(file.getParentFile());
            }
        }else{
            FileSystemView fsv = FileSystemView.getFileSystemView();
            jfc.setCurrentDirectory(fsv.getHomeDirectory());
        }
        jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        if (suffixs != null && suffixs.length > 0) {
            MyFileFilter filter = new MyFileFilter();
            for (String suffix : suffixs) {
                filter.addExtension(suffix);
                filter.addExtension(suffix);
            }
//            filter.setDescription("JPG & GIF Images");
            jfc.setFileFilter(filter);
        }

        //弹出的提示框的标题
        jfc.showDialog(new JLabel(), "确定");
        //用户选择的路径或文件
        return jfc.getSelectedFile();
    }
}