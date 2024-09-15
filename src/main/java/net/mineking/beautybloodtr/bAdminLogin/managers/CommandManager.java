package net.mineking.beautybloodtr.bAdminLogin.managers;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.security.SecureRandom;

public class CommandManager implements CommandExecutor {

    private final DatabaseManager databaseManager;
    private final JDAManager jdaManager;
    private final ConfigManager configManager;
    private final LoginManager loginManager;

    public CommandManager(DatabaseManager databaseManager, JDAManager jdaManager, ConfigManager configManager, LoginManager loginManager) {
        this.databaseManager = databaseManager;
        this.jdaManager = jdaManager;
        this.configManager = configManager;
        this.loginManager = loginManager;
    }
    String prefix = "§6bAdminLogin §8» §7";

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (command.getName().equalsIgnoreCase("badminlogin")) {
            if (args.length == 1) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage(prefix + "Bu komut sadece oyuncular içindir.");
                    return true;
                }

                Player player = (Player) sender;
                String enteredPassword = args[0];

                if (loginManager.isPlayerLoggedIn(player)) {
                    player.sendMessage(prefix + "Zaten giriş yaptınız.");
                    return true;
                }

                if (databaseManager.checkPassword(player.getName(), enteredPassword)) {
                    loginManager.removeLoginRestrictions(player);
                    player.sendMessage(prefix + "Başarıyla giriş yaptınız!");
                } else {
                    player.sendMessage(prefix + "Hatalı şifre, lütfen tekrar deneyin.");
                }
                return true;
            }

            if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
                sendHelpMessage(sender);
                return true;
            }

            String subCommand = args[0];

            switch (subCommand.toLowerCase()) {
                case "add":
                    if (sender.hasPermission(configManager.getAddPermission())) {
                        if (args.length != 4) {
                            sender.sendMessage(prefix + "Kullanım: /badminlogin add <oyun-ici-isim> <password> <discord-id>");
                        } else {
                            String username = args[1];
                            String password = args[2];
                            String discordId = args[3];

                            if (databaseManager.playerExists(username)) {
                                sender.sendMessage(prefix + "Bu kullanıcı zaten veritabanında kayıtlı.");
                            } else {
                                databaseManager.addPlayer(username, discordId, password);
                                sender.sendMessage(prefix + "Kullanıcı başarıyla eklendi.");
                                jdaManager.sendDiscordNotification(discordId, "Kullanıcı eklendi: " + username);
                            }
                        }
                    } else {
                        sender.sendMessage(prefix + "Bu komutu kullanma izniniz yok.");
                    }
                    break;

                case "setpassword":
                    if (sender.hasPermission(configManager.getSetPasswordPermission())) {
                        if (args.length != 3) {
                            sender.sendMessage(prefix + "Kullanım: /badminlogin setpassword <oyun-ici-isim> <password/random>");
                        } else {
                            String username = args[1];
                            String newPassword = args[2].equalsIgnoreCase("random") ? generateRandomPassword(10) : args[2];
                            databaseManager.setPassword(username, newPassword);
                            sender.sendMessage("Şifre başarıyla güncellendi.");
                            jdaManager.sendDiscordNotification(username, "Şifreniz başarıyla güncellendi.");
                        }
                    } else {
                        sender.sendMessage(prefix + "Bu komutu kullanma izniniz yok.");
                    }
                    break;

                case "changepassword":
                    if (sender.hasPermission(configManager.getChangePasswordPermission())) {
                        if (args.length != 2) {
                            sender.sendMessage(prefix + "Kullanım: /badminlogin changepassword <password/random>");
                        } else {
                            String newPassword = args[1].equalsIgnoreCase("random") ? generateRandomPassword(10) : args[1];
                            databaseManager.setPassword(sender.getName(), newPassword);
                            sender.sendMessage(prefix + "Şifreniz başarıyla değiştirildi.");
                            jdaManager.sendDiscordNotification(sender.getName(), "Şifreniz başarıyla değiştirildi.");
                        }
                    } else {
                        sender.sendMessage(prefix + "Bu komutu kullanma izniniz yok.");
                    }
                    break;

                case "setdiscord":
                    if (sender.hasPermission(configManager.getSetDiscordPermission())) {
                        if (args.length != 3) {
                            sender.sendMessage(prefix + "Kullanım: /badminlogin setdiscord <oyun-ici-isim> <discord-id>");
                        } else {
                            String username = args[1];
                            String newDiscordId = args[2];
                            databaseManager.setDiscord(username, newDiscordId);
                            sender.sendMessage(prefix + "Discord ID başarıyla güncellendi.");
                            jdaManager.sendDiscordNotification(newDiscordId, "Discord ID'niz başarıyla güncellendi.");
                        }
                    } else {
                        sender.sendMessage(prefix + "Bu komutu kullanma izniniz yok.");
                    }
                    break;

                case "remove":
                    if (sender.hasPermission(configManager.getRemovePermission())) {
                        if (args.length != 2) {
                            sender.sendMessage(prefix + "Kullanım: /badminlogin remove <oyun-ici-isim/discord-id>");
                        } else {
                            String identifier = args[1];
                            if (databaseManager.removePlayer(identifier)) {
                                sender.sendMessage(prefix + "Kullanıcı başarıyla silindi.");
                                jdaManager.sendDiscordNotification(identifier, "Kullanıcı verileriniz veritabanından silindi.");
                            } else {
                                sender.sendMessage(prefix + "Kullanıcı bulunamadı.");
                            }
                        }
                    } else {
                        sender.sendMessage(prefix + "Bu komutu kullanma izniniz yok.");
                    }
                    break;

                default:
                    sendHelpMessage(sender);
                    break;
            }
        }
        return true;
    }


    private void sendHelpMessage(CommandSender sender) {
        sender.sendMessage("§a--- BAdminLogin Yardım ---");
        sender.sendMessage("§e/badminlogin add <oyun-ici-isim> <password> <discord-id> §7- Kullanıcı ekler. İzin: " + configManager.getAddPermission());
        sender.sendMessage("§e/badminlogin setpassword <oyun-ici-isim> <password/random> §7- Şifreyi günceller. İzin: " + configManager.getSetPasswordPermission());
        sender.sendMessage("§e/badminlogin changepassword <password/random> §7- Kendi şifrenizi değiştirir. İzin: " + configManager.getChangePasswordPermission());
        sender.sendMessage("§e/badminlogin setdiscord <oyun-ici-isim> <discord-id> §7- Discord ID'sini günceller. İzin: " + configManager.getSetDiscordPermission());
        sender.sendMessage("§e/badminlogin remove <oyun-ici-isim/discord-id> §7- Kullanıcıyı siler. İzin: " + configManager.getRemovePermission());
    }

    private String generateRandomPassword(int length) {
        String charSet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(charSet.length());
            password.append(charSet.charAt(randomIndex));
        }

        return password.toString();
    }
}
