package cn.lanink.chattranslate;

import cn.lanink.gamecore.translateapi.TranslateAPI;
import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerChatEvent;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.scheduler.AsyncTask;

import java.util.HashMap;
import java.util.Set;

/**
 * @author LT_Name
 */
public class ChatTranslate extends PluginBase implements Listener {

    private String autoChatTranslateFormat;

    @Override
    public void onLoad() {
        this.saveDefaultConfig();
    }

    @Override
    public void onEnable() {
        try {
            Class.forName("cn.lanink.gamecore.translateapi.TranslateAPI");
        }catch (Exception e) {
            this.getLogger().error("请安装 GameCore TranslateAPI 模块！");
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }

        this.autoChatTranslateFormat = this.getConfig().getString("autoChatTranslateFormat", "&7[&6AutoChatTranslate&7] &f{player} &7-> &f{message}");

        this.getServer().getPluginManager().registerEvents(this, this);

        this.getLogger().info("插件加载完成！");
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerChat(PlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();
        if (message == null || "".equals(message.trim())) {
            return;
        }
        Set<CommandSender> recipients = event.getRecipients();
        if (recipients.isEmpty()) {
            return;
        }
        String sourceLanguage = player.getLoginChainData().getLanguageCode();
        this.getServer().getScheduler().scheduleAsyncTask(this, new AsyncTask() {
            @Override
            public void onRun() {
                HashMap<String, String> cache = new HashMap<>();
                for (CommandSender sender : recipients) {
                    if (sender instanceof Player) {
                        String targetLanguage = ((Player) sender).getLoginChainData().getLanguageCode();
                        if (targetLanguage.equalsIgnoreCase(sourceLanguage)) {
                            continue;
                        }
                        sender.sendMessage(cache.computeIfAbsent(targetLanguage,
                                s -> autoChatTranslateFormat
                                        .replace("{player}", player.getName())
                                        .replace("{message}", TranslateAPI.getInstance().translate("auto", targetLanguage, message))
                        ));
                    }
                }
            }
        });
    }

}
