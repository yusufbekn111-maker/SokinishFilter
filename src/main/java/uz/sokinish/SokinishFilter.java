package uz.sokinish;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
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
        getLogger().info("Filtr yoqildi, so'zlar soni: " + patterns.size());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncChatEvent e) {
        Component msg = e.message();
        for (Pattern p : patterns) {
            msg = msg.replaceText(b -> b.match(p)
                    .replacement((r, c) -> Component.text("*".repeat(r.group().length()))));
        }
        e.message(msg);
    }
}
