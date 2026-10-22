package uz.sokinish;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class SokinishFilter extends JavaPlugin implements Listener {

    private final List<Pattern> patterns = new ArrayList<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        for (String word : getConfig().getStringList("words")) {
            patterns.add(Pattern.compile(Pattern.quote(word),
                    Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE));
        }
        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent e) {
        String msg = e.getMessage();
        for (Pattern p : patterns) {
            msg = p.matcher(msg).replaceAll(m -> "*".repeat(m.group().length()));
        }
        e.setMessage(msg);
    }
}
