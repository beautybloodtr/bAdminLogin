package net.mineking.beautybloodtr.bAdminLogin.managers;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

import java.awt.*;

public class JDAManager extends Thread {

    private JDA jda;
    private ConfigManager configManager;

    public JDAManager(ConfigManager configManager) {
        this.configManager = configManager;
    }

    @Override
    public void run() {
        String botToken = configManager.getBotToken();

        if (botToken == null || botToken.isEmpty()) {
            System.out.println("Bot tokeni bulunamadı! Lütfen config.yml dosyasına bot tokenini ekleyin.");
            return;
        }

        try {
            JDABuilder builder = JDABuilder.createDefault(configManager.getBotToken());

            builder.setMemberCachePolicy(MemberCachePolicy.ALL);
            builder.enableIntents(GatewayIntent.MESSAGE_CONTENT);
            builder.enableIntents(GatewayIntent.GUILD_MEMBERS);

            if (configManager.isActivityEnabled()) {
                String activityType = configManager.getActivityType();
                String activityStatus = configManager.getActivityStatus();
                builder.setActivity(Activity.of(Activity.ActivityType.valueOf(activityType), activityStatus));
            }

            jda = builder.build();
            jda.awaitReady();

            String id = jda.getSelfUser().getId();
            String name = jda.getSelfUser().getName();
            System.out.println("Connected to Discord Bot successfully.");
            System.out.println(name + "(" + id + ") - bAdminLogin");

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void sendDiscordNotification(String discordId, String message) {
        try {
            User user = jda.retrieveUserById(discordId).complete();
            if (user != null) {
                EmbedBuilder embed = new EmbedBuilder();
                embed.setTitle("bAdminLogin");
                embed.setDescription(message);
                embed.setColor(Color.CYAN);
                embed.setThumbnail("https://i.imgur.com/rvBSRIH.png");
                user.openPrivateChannel().queue(channel -> {
                    channel.sendMessageEmbeds(embed.build()).queue();
                });
            } else {
                System.out.println("Kullanıcı bulunamadı: " + discordId);

            }
        } catch (Exception e) {
            System.out.println("Discord bildirim gönderilemedi: " + discordId);
        }
    }

}

