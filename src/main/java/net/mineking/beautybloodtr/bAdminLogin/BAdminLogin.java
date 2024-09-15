package net.mineking.beautybloodtr.bAdminLogin;

import net.mineking.beautybloodtr.bAdminLogin.managers.ConfigManager;
import net.mineking.beautybloodtr.bAdminLogin.managers.DatabaseManager;
import net.mineking.beautybloodtr.bAdminLogin.managers.JDAManager;
import net.mineking.beautybloodtr.bAdminLogin.managers.CommandManager;
import net.mineking.beautybloodtr.bAdminLogin.managers.LoginManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public final class BAdminLogin extends JavaPlugin {

    private ConfigManager configManager;
    private DatabaseManager databaseManager;
    private JDAManager jdaManager;
    private LoginManager loginManager;

    @Override
    public void onEnable() {
        configManager = new ConfigManager(this);
        databaseManager = new DatabaseManager(configManager);
        jdaManager = new JDAManager(configManager);

        if (isAuthMeEnabled()) {
            loginManager = new LoginManager(databaseManager, jdaManager, true); // AuthMe yüklü ise true
            getLogger().info("AuthMe bulundu, AuthMe entegrasyonu etkinleştirildi.");
        } else {
            loginManager = new LoginManager(databaseManager, jdaManager, false); // AuthMe yoksa false
            getLogger().info("AuthMe bulunamadı, BAdminLogin normal çalışıyor.");
        }
        getCommand("badminlogin").setExecutor(new CommandManager(databaseManager, jdaManager, configManager, loginManager));

        getServer().getPluginManager().registerEvents(loginManager, this);

        jdaManager.start();

        getLogger().info("BAdminLogin Plugin Başlatıldı!");
    }

    @Override
    public void onDisable() {
        getLogger().info("BAdminLogin Plugin Kapatıldı!");
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public JDAManager getJdaManager() {
        return jdaManager;
    }

    public LoginManager getLoginManager() {
        return loginManager;
    }

    private boolean isAuthMeEnabled() {
        Plugin authMe = Bukkit.getPluginManager().getPlugin("AuthMe");
        return authMe != null && authMe.isEnabled();
    }
}
