package net.mineking.beautybloodtr.bAdminLogin.managers;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;

public class ConfigManager {

    private JavaPlugin plugin;
    private FileConfiguration config;
    private File configFile;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        createConfig();
    }

    private void createConfig() {
        configFile = new File(plugin.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            plugin.saveResource("config.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    public void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void reloadConfig() {
        config = YamlConfiguration.loadConfiguration(configFile);
    }
    public String getAddPermission() {
        return config.getString("ingame.permission.add", "badminlogin.add");
    }

    public String getSetPasswordPermission() {
        return config.getString("ingame.permission.setpassword", "badminlogin.setpassword");
    }


    public String getSetDiscordPermission() {
        return config.getString("ingame.permission.setdiscord", "badminlogin.setdiscord");
    }

    public String getRemovePermission() {
        return config.getString("ingame.permission.remove", "badminlogin.remove");
    }



    public String getBotToken() {
        return config.getString("discord.bot-token");
    }

    public boolean isActivityEnabled() {
        return config.getBoolean("discord.activity.enabled");
    }

    public String getActivityType() {
        return config.getString("discord.activity.activity");
    }

    public String getActivityStatus() {
        return config.getString("discord.activity.activityStatus");
    }

    public String getLogChannel() {
        return config.getString("discord.channels.log");
    }

    public boolean shouldSendNewPasswordToAdmin() {
        return config.getBoolean("discord.send-new-password-on-discord-to-admin");
    }

    public boolean shouldSendWrongTriesToChannel() {
        return config.getBoolean("discord.send-wrong-tries-to-discord-channel");
    }

    public boolean shouldCensorLogChannelPasswords() {
        return config.getBoolean("discord.censor-log-channel-passwords");
    }

    public String getUsePermission() {
        return config.getString("ingame.permission.use");
    }

    public String getChangePasswordPermission() {
        return config.getString("ingame.permission.changepassword");
    }

    public String getChangeOthersPasswordsPermission() {
        return config.getString("ingame.permission.change-others-passwords");
    }
}