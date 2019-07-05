package me.coco0325;

import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;

public final class BungeeAntiSwear extends Plugin implements Listener {

    Configuration words;
    List<String> bad,replace;
    HashMap<String, String> replaceMap = new HashMap<String, String>();

    @Override
    public void onEnable() {
        if (!getDataFolder().exists())
            getDataFolder().mkdir();
        File file = new File(getDataFolder(), "words.yml");
        if (!file.exists()) {
            try (InputStream in = getResourceAsStream("words.yml")) {
                Files.copy(in, file.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try{
        words = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(getDataFolder(), "words.yml"));
        }catch (IOException e){
            e.printStackTrace();
        }
        bad = words.getStringList("BadWords");
        replace = words.getStringList("ReplaceWords");
        for(String r : replace){
            String[] rs = r.split(";");
            replaceMap.put(rs[0], rs[1]);
        }
        getProxy().getPluginManager().registerListener(this, this);
    }

    public static String replaceWithChar(String toReplace, char ch) {
        StringBuilder sb = new StringBuilder();
        for (char c : toReplace.toCharArray()) {
            if (c == ' ') sb.append(" ");
            else sb.append(ch);
        }

        return sb.toString().trim();
    }

    @Override
    public void onDisable() {
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onChat(ChatEvent event) {
        String message = event.getMessage();
        if(!event.isCommand() && !event.isProxyCommand() && !event.isCancelled()){
            for(String s : bad){
                String m = message.replaceAll(" ","").toLowerCase();
                if(m.contains(s)){

                    message = m.replaceAll(s, replaceWithChar(s, '`'));
                }
            }
            for(String s : replaceMap.keySet()){
                if(message.contains(s)){
                    message = message.replaceAll(s, replaceMap.get(s));
                }
            }
            message = message.replaceAll("`", "\\*");
            event.setMessage(message);
            return;
        } else {
            if(message.startsWith("/m ") || message.startsWith("/msg ")){
                String[] cmd = message.split(" ", 3);
                if(cmd.length < 3){ return; }
                String text = cmd[2];
                for(String s : bad){
                    String m = text.replaceAll(" ","").toLowerCase();
                    if(m.contains(s)){
                        text = m.replaceAll(s, replaceWithChar(s, '`'));
                    }
                }
                message = "/m "+cmd[1]+" "+text;
                message = message.replaceAll("`", "\\*");
                event.setMessage(message);
            }
        }
    }
}
