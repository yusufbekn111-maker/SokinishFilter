package uz.sokinish;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

@SuppressWarnings("deprecation")
public class SokinishFilter extends JavaPlugin implements Listener {

    private final List<Pattern> patterns = new ArrayList<>();

    private static class Invite {
        UUID from;
        String fromName;
        String kit;
        int rounds;
        long time;
    }

    private static class Saved {
        ItemStack[] inv;
        ItemStack[] armor;
        Location loc;
        GameMode mode;
        double health;
        int food;
    }

    private static class Match {
        UUID a, b;
        String nameA, nameB, kit;
        int rounds, need, winsA, winsB, round;
        boolean active;
        Map<UUID, Saved> saved = new HashMap<>();
    }

    private static class Edit {
        String name;
        Saved saved;
    }

    private final Map<UUID, Invite> invites = new HashMap<>();
    private final Map<UUID, Edit> editing = new HashMap<>();
    private Match current;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getConfig().options().copyDefaults(true);
        saveConfig();
        for (String word : getConfig().getStringList("words")) {
            patterns.add(Pattern.compile(Pattern.quote(word),
                    Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE));
        }
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("Filtr yoqildi, so'zlar soni: " + patterns.size());
    }

    @Override
    public void onDisable() {
        if (current != null) finish(current, null);
        for (Map.Entry<UUID, Edit> en : editing.entrySet()) {
            Player pl = Bukkit.getPlayer(en.getKey());
            if (pl != null) restore(pl, en.getValue().saved);
        }
        editing.clear();
    }

    // ---------- Chat filtri ----------
    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncChatEvent e) {
        Component msg = e.message();
        for (Pattern p : patterns) {
            msg = msg.replaceText(b -> b.match(p)
                    .replacement((r, c) -> Component.text("*".repeat(r.group().length()))));
        }
        e.message(msg);
    }

    // ---------- Buyruqlar ----------
    @Override
    public boolean onCommand(CommandSender s, Command cmd, String label, String[] a) {
        if (!(s instanceof Player p)) {
            s.sendMessage("Bu buyruq faqat o'yinda ishlaydi.");
            return true;
        }
        if (cmd.getName().equalsIgnoreCase("duelset")) {
            if (!p.hasPermission("sokinish.admin")) {
                tell(p, "Ruxs
