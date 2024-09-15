package net.mineking.beautybloodtr.bAdminLogin.managers;

import fr.xephi.authme.events.LoginEvent;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

public class LoginManager implements Listener {

    private final DatabaseManager databaseManager;
    private final JDAManager jdaManager;
    private final boolean isAuthMeEnabled;
    private final Map<Player, Boolean> loggedInPlayers = new HashMap<>();
    private final Map<Player, GameMode> previousGameModes = new HashMap<>();
    private final Map<Player, Integer> wrongAttempts = new HashMap<>();

    public LoginManager(DatabaseManager databaseManager, JDAManager jdaManager, boolean isAuthMeEnabled) {
        this.databaseManager = databaseManager;
        this.jdaManager = jdaManager;
        this.isAuthMeEnabled = isAuthMeEnabled;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (isAuthMeEnabled) {

            return;
        }

        startLoginProcess(player);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerAuth(LoginEvent event) {
        if (!isAuthMeEnabled) return;

        Player player = event.getPlayer();
        startLoginProcess(player);
    }

    private void startLoginProcess(Player player) {
        if (player.hasPermission("badminlogin.use") && databaseManager.playerExists(player.getName())) {
            applyLoginRestrictions(player);
            wrongAttempts.put(player, 0);

            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!isPlayerLoggedIn(player)) {
                        player.kickPlayer("§cGiriş yapmadığınız için sunucudan atıldınız.");
                    }
                }
            }.runTaskLater(Bukkit.getPluginManager().getPlugin("bAdminLogin"), 1200L);  // 60 saniye

            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!isPlayerLoggedIn(player)) {
                        player.sendMessage("§cLütfen giriş yapınız: §e/badminlogin <şifre>");
                    } else {
                        cancel();
                    }
                }
            }.runTaskTimer(Bukkit.getPluginManager().getPlugin("bAdminLogin"), 0L, 100L);
        } else {
            loggedInPlayers.put(player, true);
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!isPlayerLoggedIn(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        loggedInPlayers.remove(player);
        previousGameModes.remove(player);
        wrongAttempts.remove(player);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (!isPlayerLoggedIn(player)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (!isPlayerLoggedIn(player)) {
            player.sendMessage("§cGiriş yapmadan chat kullanamazsınız. Lütfen şifrenizi girin: §e/badminlogin <şifre>");
            event.setCancelled(true);
        }
    }

    public void applyLoginRestrictions(Player player) {
        previousGameModes.put(player, player.getGameMode());

        player.setWalkSpeed(0);
        player.setFlySpeed(0);
        player.setGameMode(GameMode.ADVENTURE);
        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, Integer.MAX_VALUE, 1, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1, false, false));
        loggedInPlayers.put(player, false);
    }

    public void removeLoginRestrictions(Player player) {
        player.setWalkSpeed(0.2f);
        player.setFlySpeed(0.1f);
        player.removePotionEffect(PotionEffectType.BLINDNESS);
        player.removePotionEffect(PotionEffectType.INVISIBILITY);

        GameMode previousGameMode = previousGameModes.get(player);
        if (previousGameMode != null) {
            player.setGameMode(previousGameMode);
        }

        loggedInPlayers.put(player, true);
    }

    public boolean isPlayerLoggedIn(Player player) {
        return loggedInPlayers.getOrDefault(player, false);
    }

    public void handleWrongPassword(Player player) {
        int attempts = wrongAttempts.getOrDefault(player, 0);
        attempts++;
        wrongAttempts.put(player, attempts);

        if (attempts >= 3) {

            String discordId = databaseManager.getDiscordId(player.getName());
            if (discordId != null) {
                jdaManager.sendDiscordNotification(discordId, "Birçok yanlış şifre denemesi yapıldı. Hesabınız engellendi.");
            }
            player.kickPlayer("§cÇok fazla yanlış şifre denemesi yaptınız, sunucudan atıldınız.");
        } else {
            player.sendMessage("§cYanlış şifre. Lütfen tekrar deneyin.");
        }
    }
}
