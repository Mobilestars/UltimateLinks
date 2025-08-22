package de.scholle.ultimatelinks;

import com.google.gson.Gson;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Field;
import java.util.*;

public class UltimateLinks extends JavaPlugin implements TabCompleter {

    private boolean prefixEnabled;
    private String defaultPrefix;
    private final Map<String, Command> registeredCommands = new HashMap<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        saveResource("lang/en_us.json", false);
        saveResource("lang/de_de.json", false);

        loadSettings();
        registerCommandsFromConfig();

        PluginCommand cmd = getCommand("ultimatelinks");
        if (cmd != null) {
            cmd.setExecutor(this::onUltimateLinksCommand);
            cmd.setTabCompleter(this);
        }
    }

    private void loadSettings() {
        FileConfiguration config = getConfig();
        prefixEnabled = config.getBoolean("prefix.enabled", true);
        defaultPrefix = translateColorCodes(config.getString("prefix.default", "&aLink: &f"));
    }

    private void registerCommandsFromConfig() {
        FileConfiguration config = getConfig();

        try {
            Field commandMapField = SimplePluginManager.class.getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            CommandMap commandMap = (CommandMap) commandMapField.get(Bukkit.getPluginManager());

            registeredCommands.forEach((name, cmd) -> commandMap.getCommand(name).unregister(commandMap));
            registeredCommands.clear();

            for (Map.Entry<String, Object> entry : config.getValues(false).entrySet()) {
                String key = entry.getKey();

                if (key.equalsIgnoreCase("prefix") || key.endsWith("-prefix")) continue;

                String commandName = key;
                String link = entry.getValue().toString();
                String commandPrefix = prefixEnabled ? translateColorCodes(config.getString(commandName + "-prefix", defaultPrefix)) : "";

                Command command = new BukkitCommand(commandName) {
                    @Override
                    public boolean execute(CommandSender sender, String label, String[] args) {
                        if (!sender.hasPermission("ultimatelinks.use")) {
                            sender.sendMessage(msg(sender, "no-permission"));
                            return true;
                        }
                        sender.sendMessage(commandPrefix + link);
                        return true;
                    }
                };

                command.setDescription("Link command for " + commandName);
                command.setUsage("/" + commandName);

                commandMap.register(getDescription().getName(), command);
                registeredCommands.put(commandName, command);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean onUltimateLinksCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("ultimatelinks.op")) {
            sender.sendMessage(msg(sender, "no-permission"));
            return true;
        }

        if (args.length == 0) {
            for (String line : msgList(sender, "help")) sender.sendMessage(line);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload" -> {
                reloadConfig();
                loadSettings();
                registerCommandsFromConfig();
                sender.sendMessage(msg(sender, "reloaded"));
                return true;
            }
            case "add" -> {
                if (args.length < 3) {
                    sender.sendMessage(msg(sender, "usage-add"));
                    return true;
                }
                String name = args[1].toLowerCase();
                String link = args[2];
                String prefix = (args.length >= 4) ? translateColorCodes(String.join(" ", Arrays.copyOfRange(args, 3, args.length))) : defaultPrefix;

                getConfig().set(name, link);
                if (prefixEnabled) getConfig().set(name + "-prefix", prefix);
                saveConfig();

                registerCommandsFromConfig();
                sender.sendMessage(msg(sender, "link-added", Map.of("name", name, "link", link)));
                return true;
            }
            case "remove" -> {
                if (args.length < 2) {
                    sender.sendMessage(msg(sender, "usage-remove"));
                    return true;
                }
                String name = args[1].toLowerCase();
                if (getConfig().contains(name)) {
                    getConfig().set(name, null);
                    getConfig().set(name + "-prefix", null);
                    saveConfig();
                    registerCommandsFromConfig();
                    sender.sendMessage(msg(sender, "link-removed", Map.of("name", name)));
                } else {
                    sender.sendMessage(msg(sender, "link-not-found", Map.of("name", name)));
                }
                return true;
            }
        }
        sender.sendMessage("§cUnknown subcommand.");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!command.getName().equalsIgnoreCase("ultimatelinks")) return null;

        List<String> result = new ArrayList<>();
        if (args.length == 1) {
            if (sender.hasPermission("ultimatelinks.op")) {
                result.addAll(Arrays.asList("reload", "add", "remove"));
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("remove")) {
            result.addAll(getConfig().getKeys(false));
        }
        return result;
    }

    // ----------------- LANG SYSTEM -----------------
    private Map<String, Object> loadLangFile(String locale) {
        File file = new File(getDataFolder(), "lang/" + locale.toLowerCase() + ".json");
        if (!file.exists()) {
            file = new File(getDataFolder(), "lang/en_us.json");
        }
        try {
            return new Gson().fromJson(new FileReader(file), Map.class);
        } catch (Exception e) {
            e.printStackTrace();
            return new HashMap<>();
        }
    }

    private String msg(CommandSender sender, String key) {
        return msg(sender, key, null);
    }

    private String msg(CommandSender sender, String key, Map<String, String> vars) {
        String locale = "en_us";
        if (sender instanceof Player p) locale = p.getLocale().toLowerCase();

        Map<String, Object> langData = loadLangFile(locale);
        String text = (String) langData.getOrDefault(key, "&cMissing message: " + key);

        if (vars != null) {
            for (Map.Entry<String, String> e : vars.entrySet()) {
                text = text.replace("{" + e.getKey() + "}", e.getValue());
            }
        }
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    private List<String> msgList(CommandSender sender, String key) {
        String locale = "en_us";
        if (sender instanceof Player p) locale = p.getLocale().toLowerCase();

        Map<String, Object> langData = loadLangFile(locale);
        List<String> list = (List<String>) langData.getOrDefault(key, Collections.emptyList());

        List<String> out = new ArrayList<>();
        for (Object o : list) out.add(ChatColor.translateAlternateColorCodes('&', o.toString()));
        return out;
    }

    private String translateColorCodes(String input) {
        return ChatColor.translateAlternateColorCodes('&', input);
    }
}
