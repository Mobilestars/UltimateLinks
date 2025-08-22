package de.scholle.betterlinks;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.util.Map;

public class Betterlinks extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();
        registerCommandsFromConfig();
    }

    private void registerCommandsFromConfig() {
        FileConfiguration config = getConfig();

        try {
            Field commandMapField = SimplePluginManager.class.getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            CommandMap commandMap = (CommandMap) commandMapField.get(Bukkit.getPluginManager());

            for (Map.Entry<String, Object> entry : config.getValues(false).entrySet()) {
                String commandName = entry.getKey();
                String link = entry.getValue().toString();

                Command command = new BukkitCommand(commandName) {
                    @Override
                    public boolean execute(org.bukkit.command.CommandSender sender, String label, String[] args) {
                        sender.sendMessage("§aLink: §f" + link);
                        return true;
                    }
                };

                command.setDescription("Link command for " + commandName);
                command.setUsage("/" + commandName);

                commandMap.register(getDescription().getName(), command);
                getLogger().info("Registered command /" + commandName + " -> " + link);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
