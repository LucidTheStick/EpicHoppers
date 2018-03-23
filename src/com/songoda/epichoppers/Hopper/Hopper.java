package com.songoda.epichoppers.Hopper;

import com.songoda.arconix.Arconix;
import com.songoda.epichoppers.Lang;
import com.songoda.epichoppers.EpicHoppers;
import com.songoda.epichoppers.Utils.Debugger;
import com.songoda.epichoppers.Utils.Methods;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by songoda on 3/14/2017.
 */
public class Hopper {

    Block block;
    String locationStr;
    Player p;

    private EpicHoppers plugin = EpicHoppers.pl();

    public Hopper(Player pl) {
        try {
            block = plugin.lastBlock.get(pl);
            locationStr = Arconix.pl().serialize().serializeLocation(block);
            p = pl;
        } catch (Exception e) {
            Debugger.runReport(e);
        }

    }

    public boolean isMaxed(int level) {
        if (plugin.getConfig().contains("settings.levels.Level-" + (level + 1))) {
            return true;
        }
        return false;
    }

    public void view() {
        try {
            int level = plugin.dataFile.getConfig().getInt("data.sync." + Arconix.pl().serialize().serializeLocation(plugin.lastBlock.get(p)) + ".level");
            if (level == 0) {
                level = 1;
            }
            Inventory i = Bukkit.createInventory(null, 27, Methods.formatName(level, false));

            int xpCost = 1;
            int ecoCost = 1;

            int range = plugin.getConfig().getInt("settings.levels.Level-" + level + ".Range");
            int amount = plugin.getConfig().getInt("settings.levels.Level-" + level + ".Amount");
            int suction = plugin.getConfig().getInt("settings.levels.Level-" + level + ".Suction");
            int blockBreak = plugin.getConfig().getInt("settings.levels.Level-" + level + ".BlockBreak");

            int nextBlockBreak = 0;
            int nextSuction = 0;
            int nextRange = 0;
            int nextAmount = 0;

            boolean maxed = !isMaxed(level);

            if (plugin.getConfig().contains("settings.levels.Level-" + (level + 1))) {
                xpCost = plugin.getConfig().getInt("settings.levels.Level-" + (level + 1) + ".Cost-xp");
                ecoCost = plugin.getConfig().getInt("settings.levels.Level-" + (level + 1) + ".Cost-eco");

                nextSuction = plugin.getConfig().getInt("settings.levels.Level-" + (level + 1) + ".Suction");
                nextRange = plugin.getConfig().getInt("settings.levels.Level-" + (level + 1) + ".Range");
                nextAmount = plugin.getConfig().getInt("settings.levels.Level-" + (level + 1) + ".Amount");
                nextBlockBreak = plugin.getConfig().getInt("settings.levels.Level-" + (level + 1) + ".BlockBreak");
            }


            ItemStack perl = new ItemStack(Material.ENDER_PEARL, 1);
            ItemMeta perlmeta = perl.getItemMeta();
            perlmeta.setDisplayName(Arconix.pl().format().formatText(Lang.PEARL_TITLE.getConfigValue(null)));
            ArrayList<String> loreperl = new ArrayList<>();
            String[] parts = Lang.PEARLLORE.getConfigValue(null).split("\\|");
            for (String line : parts) {
                loreperl.add(Arconix.pl().format().formatText(line));
            }
            perlmeta.setLore(loreperl);
            perl.setItemMeta(perlmeta);

            ItemStack filter = new ItemStack(Material.REDSTONE_COMPARATOR, 1);
            ItemMeta filtermeta = filter.getItemMeta();
            filtermeta.setDisplayName(Arconix.pl().format().formatText(Lang.FILTER_TITLE.getConfigValue(null)));
            ArrayList<String> lorefilter = new ArrayList<>();
            parts = Lang.FILTERLORE.getConfigValue(null).split("\\|");
            for (String line : parts) {
                lorefilter.add(Arconix.pl().format().formatText(line));
            }
            filtermeta.setLore(lorefilter);
            filter.setItemMeta(filtermeta);


            ItemStack item = new ItemStack(Material.HOPPER, 1);
            ItemMeta itemmeta = item.getItemMeta();
            itemmeta.setDisplayName(Arconix.pl().format().formatText(Lang.LEVEL.getConfigValue(level)));
            ArrayList<String> lore = new ArrayList<>();
            lore.add(Lang.NEXT_RANGE.getConfigValue(range));
            lore.add(Lang.NEXT_AMOUNT.getConfigValue(amount));
            if (suction != 0) {
                lore.add(Lang.NEXT_SUCTION.getConfigValue(suction));
            }
            if (blockBreak != 0) {
                lore.add(Lang.NEXT_BLOCKBREAK.getConfigValue(blockBreak));
            }
            lore.add("");
            if (maxed) {
                lore.add(Lang.MAXED.getConfigValue(null));
            } else {
                lore.add(Lang.NEXT_LEVEL.getConfigValue(level + 1));
                lore.add(Lang.NEXT_RANGE.getConfigValue(nextRange));
                lore.add(Lang.NEXT_AMOUNT.getConfigValue(nextAmount));
                if (nextSuction != 0) {
                    lore.add(Lang.NEXT_SUCTION.getConfigValue(nextSuction));
                }
                if (nextBlockBreak != 0) {
                    lore.add(Lang.NEXT_BLOCKBREAK.getConfigValue(nextBlockBreak));
                }
            }
            itemmeta.setLore(lore);
            item.setItemMeta(itemmeta);

            ItemStack hook = new ItemStack(Material.TRIPWIRE_HOOK, 1);
            ItemMeta hookmeta = hook.getItemMeta();
            hookmeta.setDisplayName(Arconix.pl().format().formatText(Lang.SYNC_HOPPER.getConfigValue(null)));
            ArrayList<String> lorehook = new ArrayList<>();
            parts = Lang.SYNCLORE.getConfigValue(null).split("\\|");
            for (String line : parts) {
                lorehook.add(Arconix.pl().format().formatText(line));
            }
            hookmeta.setLore(lorehook);
            hook.setItemMeta(hookmeta);

            ItemStack itemXP = new ItemStack(Material.valueOf(plugin.getConfig().getString("settings.XP-Icon")), 1);
            ItemMeta itemmetaXP = itemXP.getItemMeta();
            itemmetaXP.setDisplayName(Lang.XPTITLE.getConfigValue(null));
            ArrayList<String> loreXP = new ArrayList<>();
            if (!maxed) {
                loreXP.add(Lang.XPLORE.getConfigValue(xpCost + ""));
            } else {
                loreXP.add(Lang.MAXED.getConfigValue(null));
            }
            itemmetaXP.setLore(loreXP);
            itemXP.setItemMeta(itemmetaXP);

            ItemStack itemECO = new ItemStack(Material.valueOf(plugin.getConfig().getString("settings.ECO-Icon")), 1);
            ItemMeta itemmetaECO = itemECO.getItemMeta();
            itemmetaECO.setDisplayName(Lang.ECOTITLE.getConfigValue(null));
            ArrayList<String> loreECO = new ArrayList<>();
            if (!maxed) {
                loreECO.add(Lang.ECOLORE.getConfigValue(Arconix.pl().format().formatEconomy(ecoCost)));
            } else {
                loreECO.add(Lang.MAXED.getConfigValue(null));
            }
            itemmetaECO.setLore(loreECO);
            itemECO.setItemMeta(itemmetaECO);

            int nu = 0;
            while (nu != 27) {
                i.setItem(nu, Methods.getGlass());
                nu++;
            }

            boolean canFilter = plugin.getConfig().getBoolean("settings.Filter-hoppers") && p.hasPermission("EpicHoppers.Filter");
            boolean canTeleport = plugin.getConfig().getBoolean("settings.Teleport-hoppers") && p.hasPermission("EpicHoppers.Teleport");
            if (!canFilter && canTeleport)
                i.setItem(4, perl);
            else if (!canTeleport && canFilter)
                i.setItem(4, filter);
            else if (canFilter && canTeleport){
                i.setItem(3, perl);
                i.setItem(5, filter);
            }

            if (plugin.getConfig().getBoolean("settings.Upgrade-with-xp") && p.hasPermission("EpicHoppers.Upgrade.XP")) {
                i.setItem(11, itemXP);
            }

            i.setItem(13, item);
            i.setItem(22, hook);

            if (plugin.getConfig().getBoolean("settings.Upgrade-with-eco") && p.hasPermission("EpicHoppers.Upgrade.ECO")) {
                i.setItem(15, itemECO);
            }

            i.setItem(0, Methods.getBackgroundGlass(true));
            i.setItem(1, Methods.getBackgroundGlass(true));
            i.setItem(2, Methods.getBackgroundGlass(false));
            i.setItem(6, Methods.getBackgroundGlass(false));
            i.setItem(7, Methods.getBackgroundGlass(true));
            i.setItem(8, Methods.getBackgroundGlass(true));
            i.setItem(9, Methods.getBackgroundGlass(true));
            i.setItem(10, Methods.getBackgroundGlass(false));
            i.setItem(16, Methods.getBackgroundGlass(false));
            i.setItem(17, Methods.getBackgroundGlass(true));
            i.setItem(18, Methods.getBackgroundGlass(true));
            i.setItem(19, Methods.getBackgroundGlass(true));
            i.setItem(20, Methods.getBackgroundGlass(false));
            i.setItem(24, Methods.getBackgroundGlass(false));
            i.setItem(25, Methods.getBackgroundGlass(true));
            i.setItem(26, Methods.getBackgroundGlass(true));

            p.openInventory(i);
            plugin.inShow.put(p, locationStr);
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    public void filter() {
        try {
            int level = plugin.dataFile.getConfig().getInt("data.sync." + Arconix.pl().serialize().serializeLocation(plugin.lastBlock.get(p)) + ".level");
            if (level == 0) {
                level = 1;
            }
            Inventory i = Bukkit.createInventory(null, 54, Arconix.pl().format().formatText(Methods.formatName(level, false) + " &8-&f Filter"));

            i.setItem(2, Methods.getBackgroundGlass(true));
            i.setItem(3, Methods.getBackgroundGlass(true));
            i.setItem(4, Methods.getBackgroundGlass(true));
            i.setItem(5, Methods.getBackgroundGlass(false));
            i.setItem(6, Methods.getBackgroundGlass(false));

            i.setItem(11, Methods.getBackgroundGlass(true));
            i.setItem(15, Methods.getBackgroundGlass(false));

            i.setItem(20, Methods.getBackgroundGlass(true));
            i.setItem(24, Methods.getBackgroundGlass(true));

            i.setItem(29, Methods.getBackgroundGlass(true));
            i.setItem(33, Methods.getBackgroundGlass(true));

            i.setItem(38, Methods.getBackgroundGlass(false));
            i.setItem(42, Methods.getBackgroundGlass(true));

            i.setItem(47, Methods.getBackgroundGlass(false));
            i.setItem(48, Methods.getBackgroundGlass(false));
            i.setItem(49, Methods.getBackgroundGlass(true));
            i.setItem(50, Methods.getBackgroundGlass(true));
            i.setItem(51, Methods.getBackgroundGlass(true));

            i.setItem(12, Methods.getGlass());
            i.setItem(14, Methods.getGlass());
            i.setItem(21, Methods.getGlass());
            i.setItem(22, Methods.getGlass());
            i.setItem(23, Methods.getGlass());
            i.setItem(30, Methods.getGlass());
            i.setItem(31, Methods.getGlass());
            i.setItem(32, Methods.getGlass());
            i.setItem(39, Methods.getGlass());
            i.setItem(41, Methods.getGlass());

            ItemStack it = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 0);
            ItemMeta itm = it.getItemMeta();
            itm.setDisplayName(Lang.WHITELIST.getConfigValue());
            it.setItemMeta(itm);
            int[] awhite = {0, 1, 9, 10, 18, 19};
            for (int nu : awhite) {
                i.setItem(nu, it);
            }
            if (plugin.dataFile.getConfig().contains("data.sync." + Arconix.pl().serialize().serializeLocation(block) + ".whitelist")) {
                List<ItemStack> owhite = (List<ItemStack>) plugin.dataFile.getConfig().getList("data.sync." + Arconix.pl().serialize().serializeLocation(block) + ".whitelist");

                int num = 0;
                for (ItemStack o : owhite) {
                    if (o != null) {
                        i.setItem(awhite[num], o);
                        num++;
                    }
                }
            }


            it = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 15);
            itm = it.getItemMeta();
            itm.setDisplayName(Lang.BLACKLIST.getConfigValue());
            it.setItemMeta(itm);
            int[] ablack = {27, 28, 36, 37, 45, 46};
            for (int nu : ablack) {
                i.setItem(nu, it);
            }
            if (plugin.dataFile.getConfig().contains("data.sync." + Arconix.pl().serialize().serializeLocation(block) + ".blacklist")) {
                List<ItemStack> oblack = (List<ItemStack>) plugin.dataFile.getConfig().getList("data.sync." + Arconix.pl().serialize().serializeLocation(block) + ".blacklist");
                int num = 0;
                for (ItemStack o : oblack) {
                    if (o != null) {
                        i.setItem(ablack[num], o);
                        num++;
                    }
                }
            }

            it = new ItemStack(Material.BARRIER);
            itm = it.getItemMeta();
            itm.setDisplayName(Lang.VOID.getConfigValue());
            it.setItemMeta(itm);
            int[] avoid = {7, 8, 16, 17, 25, 26, 34, 35, 43, 44, 52, 53};
            for (int nu : avoid) {
                i.setItem(nu, it);
            }
            if (plugin.dataFile.getConfig().contains("data.sync." + Arconix.pl().serialize().serializeLocation(block) + ".void")) {
                List<ItemStack> ovoid = (List<ItemStack>) plugin.dataFile.getConfig().getList("data.sync." + Arconix.pl().serialize().serializeLocation(block) + ".void");

                int num = 0;
                for (ItemStack o : ovoid) {
                    if (o != null) {
                        i.setItem(avoid[num], o);
                        num++;
                    }
                }
            }

            ItemStack itemInfo = new ItemStack(Material.PAPER, 1);
            ItemMeta itemmetaInfo = itemInfo.getItemMeta();
            itemmetaInfo.setDisplayName(Lang.INFOTITLE.getConfigValue(null));
            ArrayList<String> loreInfo = new ArrayList<>();
            String[] parts = Lang.INFOLORE.getConfigValue(null).split("\\|");
            for (String line : parts) {
                loreInfo.add(Arconix.pl().format().formatText(line));
            }
            itemmetaInfo.setLore(loreInfo);
            itemInfo.setItemMeta(itemmetaInfo);

            i.setItem(13, itemInfo);


            ItemStack hook = new ItemStack(Material.TRIPWIRE_HOOK, 1);
            ItemMeta hookmeta = hook.getItemMeta();
            hookmeta.setDisplayName(Arconix.pl().format().formatText(Lang.BSYNC_HOPPER.getConfigValue(null)));
            ArrayList<String> lorehook = new ArrayList<>();
            parts = Lang.SYNCLORE.getConfigValue(null).split("\\|");
            for (String line : parts) {
                lorehook.add(Arconix.pl().format().formatText(line));
            }
            hookmeta.setLore(lorehook);
            hook.setItemMeta(hookmeta);
            i.setItem(40, hook);

            p.openInventory(i);
            plugin.inFilter.put(p, locationStr);
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    public void compile(Player p) {
        try {
            ItemStack[] items2 = p.getOpenInventory().getTopInventory().getContents();

            List<ItemStack> owhite = new ArrayList<>();
            List<ItemStack> oblack = new ArrayList<>();
            List<ItemStack> ovoid = new ArrayList<>();

            int[] awhite = {0, 1, 9, 10, 18, 19};
            int[] ablack = {27, 28, 36, 37, 45, 46};
            int[] avoid = {7, 8, 16, 17, 25, 26, 34, 35, 43, 44, 52, 53};

            int num = 0;
            for (ItemStack item : items2) {
                for (int aa : awhite) {
                    if (aa == num) {
                        if (items2[num] != null && !items2[num].getType().equals(Material.STAINED_GLASS_PANE))
                            owhite.add(items2[num]);
                    }
                }
                for (int aa : ablack) {
                    if (aa == num) {
                        if (items2[num] != null && !items2[num].getType().equals(Material.STAINED_GLASS_PANE))
                            oblack.add(items2[num]);
                    }
                }
                for (int aa : avoid) {
                    if (aa == num) {
                        if (items2[num] != null && !items2[num].getType().equals(Material.BARRIER))
                            ovoid.add(items2[num]);
                    }
                }
                num++;
            }
            plugin.dataFile.getConfig().set("data.sync." + Arconix.pl().serialize().serializeLocation(block) + ".whitelist", owhite);
            plugin.dataFile.getConfig().set("data.sync." + Arconix.pl().serialize().serializeLocation(block) + ".blacklist", oblack);
            plugin.dataFile.getConfig().set("data.sync." + Arconix.pl().serialize().serializeLocation(block) + ".void", ovoid);
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    public void upgrade(String type) {
        try {
            int level = plugin.dataFile.getConfig().getInt("data.sync." + Arconix.pl().serialize().serializeLocation(block) + ".level");
            if (level == 0) {
                level = 1;
            }
            if (plugin.getConfig().contains("settings.levels.Level-" + (level + 1))) {

                int cost;
                if (type == "XP") {
                    cost = plugin.getConfig().getInt("settings.levels.Level-" + (level + 1) + ".Cost-xp");
                } else {
                    cost = plugin.getConfig().getInt("settings.levels.Level-" + (level + 1) + ".Cost-eco");
                }

                if (type == "ECO") {
                    if (plugin.getServer().getPluginManager().getPlugin("Vault") != null) {
                        RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
                        net.milkbowl.vault.economy.Economy econ = rsp.getProvider();
                        if (econ.has(p, cost)) {
                            econ.withdrawPlayer(p, cost);
                            upgradeFinal(level + 1);
                        } else {
                            p.sendMessage(Lang.CANT_AFFORD.getConfigValue(null));
                        }
                    } else {
                        p.sendMessage("Vault is not installed.");
                    }
                } else if (type == "XP") {
                    if (p.getLevel() >= cost || p.getGameMode() == GameMode.CREATIVE) {
                        if (p.getGameMode() != GameMode.CREATIVE) {
                            p.setLevel(p.getLevel() - cost);
                        }
                        upgradeFinal(level + 1);
                    } else {
                        p.sendMessage(Lang.CANT_AFFORD.getConfigValue(null));
                    }
                }
            }
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    public void upgradeFinal(int level) {
        try {
            plugin.dataFile.getConfig().set("data.sync." + Arconix.pl().serialize().serializeLocation(plugin.lastBlock.get(p)) + ".level", level);
            if (isMaxed(level)) {
                p.sendMessage(Lang.UPGRADE_MESSAGE.getConfigValue(level));
            } else {
                p.sendMessage(Lang.YOU_MAXED.getConfigValue(Integer.toString(level)));
            }
            if (plugin.getConfig().getBoolean("settings.On-upgrade-particles")) {
                Location loc = plugin.lastBlock.get(p).getLocation();
                loc.setX(loc.getX() + .5);
                loc.setY(loc.getY() + .5);
                loc.setZ(loc.getZ() + .5);
                if (!plugin.v1_8 && !plugin.v1_7) {
                    p.getWorld().spawnParticle(org.bukkit.Particle.valueOf(plugin.getConfig().getString("settings.Upgrade-particle-type")), loc, 200, .5, .5, .5);
                } else {
                    p.getWorld().spigot().playEffect(loc, org.bukkit.Effect.valueOf(plugin.getConfig().getString("settings.Upgrade-particle-type")), 1, 0, (float) 1, (float) 1, (float) 1, 1, 200, 10);
                }
                if (plugin.getInstance().getConfig().getBoolean("settings.Sounds")) {
                    if (isMaxed(level)) {
                        if (!plugin.getInstance().v1_8 && !plugin.getInstance().v1_7) {
                            p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.6F, 15.0F);
                        } else {
                            p.playSound(p.getLocation(), Sound.valueOf("LEVEL_UP"), 2F, 15.0F);
                        }
                    } else {
                        if (!plugin.getInstance().v1_10 && !plugin.getInstance().v1_9 && !plugin.getInstance().v1_8 && !plugin.getInstance().v1_7) {
                            p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 2F, 25.0F);
                            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_CHIME, 2F, 25.0F);
                            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin.getInstance(), () -> p.playSound(p.getLocation(), Sound.BLOCK_NOTE_CHIME, 1.2F, 35.0F), 5L);
                            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin.getInstance(), () -> p.playSound(p.getLocation(), Sound.BLOCK_NOTE_CHIME, 1.8F, 35.0F), 10L);
                        } else {
                            p.playSound(p.getLocation(), Sound.valueOf("LEVEL_UP"), 2F, 25.0F);
                        }
                    }
                }
            }
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }


    public void timeout() {
        try {
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                if (plugin.sync.containsKey(p)) {
                    p.sendMessage(Lang.SYNC_TIMEOUT.getConfigValue(null));
                    plugin.sync.remove(p);
                    plugin.bsync.remove(p);
                }
            }, plugin.getConfig().getLong("settings.Sync-Timeout"));
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    public void sync(Block block2, boolean black) {
        try {
            int level = 1;
            if (plugin.dataFile.getConfig().contains("data.sync." + Arconix.pl().serialize().serializeLocation(block) + ".level")) {
                level = plugin.dataFile.getConfig().getInt("data.sync." + Arconix.pl().serialize().serializeLocation(block) + ".level");
            }

            int range = plugin.getConfig().getInt("settings.levels.Level-" + level + ".Range");
            if (block.getLocation().getWorld().equals(block2.getLocation().getWorld()) && !p.hasPermission("EpicHoppers.Override") && !p.hasPermission("EpicHoppers.Admin")) {
                if (block.getLocation().distance(block2.getLocation()) > range && !p.hasPermission("EpicHoppers.Override") && !p.hasPermission("EpicHoppers.Admin")) {
                    p.sendMessage(Lang.SYNC_OUT_OF_RANGE.getConfigValue(null));
                    return;
                }
            }
            p.sendMessage(Lang.SYNC_SUCCESS.getConfigValue(null));
            plugin.dataFile.getConfig().set("data.sync." + Arconix.pl().serialize().serializeLocation(block) + ".level", level);

            if (!black)
                plugin.dataFile.getConfig().set("data.sync." + Arconix.pl().serialize().serializeLocation(block) + ".block", Arconix.pl().serialize().serializeLocation(block2));
            else
                plugin.dataFile.getConfig().set("data.sync." + Arconix.pl().serialize().serializeLocation(block) + ".black", Arconix.pl().serialize().serializeLocation(block2));
            plugin.dataFile.getConfig().set("data.sync." + Arconix.pl().serialize().serializeLocation(block) + ".player", p.getUniqueId().toString());
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }
}
