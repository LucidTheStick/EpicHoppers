package com.songoda.epichoppers.listeners;

import com.songoda.core.compatibility.CompatibleMaterial;
import com.songoda.core.compatibility.ServerVersion;
import com.songoda.epichoppers.EpicHoppers;
import com.songoda.epichoppers.hopper.Hopper;
import com.songoda.epichoppers.hopper.HopperBuilder;
import com.songoda.epichoppers.hopper.levels.Level;
import com.songoda.epichoppers.settings.Settings;
import com.songoda.epichoppers.utils.Methods;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;


/**
 * Created by songoda on 3/14/2017.
 */
public class BlockListeners implements Listener {

    private final EpicHoppers instance;
    private final Random random;

    public BlockListeners(EpicHoppers instance) {
        this.instance = instance;
        this.random = new Random();
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent e) {
        Player player = e.getPlayer();

        if (e.getBlock().getType() != Material.HOPPER)
            return;

        if (instance.isLiquidtanks() && net.arcaniax.liquidtanks.object.LiquidTankAPI.isLiquidTank(e.getBlock().getLocation()))
            return;

        int amt = count(e.getBlock().getChunk());

        int max = maxHoppers(player);

        if (max != -1 && amt > max) {
            player.sendMessage(instance.getLocale().getMessage("event.hopper.toomany").processPlaceholder("amount", max).getMessage());
            e.setCancelled(true);
            return;
        }

        ItemStack item = e.getItemInHand().clone();

        if (Settings.ALLOW_NORMAL_HOPPERS.getBoolean() && !instance.getLevelManager().isEpicHopper(item))
            return;

        Hopper hopper = instance.getHopperManager().addHopper(
                new HopperBuilder(e.getBlock())
                        .setLevel(instance.getLevelManager().getLevel(item))
                        .setPlacedBy(player)
                        .setLastPlayerOpened(player).build());
        EpicHoppers.getInstance().getDataManager().createHopper(hopper);
    }

    private int maxHoppers(Player player) {
        int limit = -1;
        for (PermissionAttachmentInfo permissionAttachmentInfo : player.getEffectivePermissions()) {
            if (!permissionAttachmentInfo.getPermission().toLowerCase().startsWith("epichoppers.limit")) continue;
            int num = Integer.parseInt(permissionAttachmentInfo.getPermission().split("\\.")[2]);
            if (num > limit)
                limit = num;
        }
        if (limit == -1) limit = instance.getConfig().getInt("Main.Max Hoppers Per Chunk");
        return limit;
    }

    private int count(Chunk c) {
        int count = 0;
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = 0; y < c.getWorld().getMaxHeight(); y++) {
                    if (c.getBlock(x, y, z).getType() == Material.HOPPER) count++;
                }
            }
        }
        return count;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();

        handleSyncTouch(event);

        if (event.getBlock().getType() != Material.HOPPER) return;

        if (instance.isLiquidtanks() && net.arcaniax.liquidtanks.object.LiquidTankAPI.isLiquidTank(block.getLocation()))
            return;

        if (Settings.ALLOW_NORMAL_HOPPERS.getBoolean() && !instance.getHopperManager().isHopper(block.getLocation()))
            return;

        Hopper hopper = instance.getHopperManager().getHopper(block);

        Level level = hopper.getLevel();

        if (level.getLevel() > 1) {
            event.setCancelled(true);
            ItemStack item = instance.newHopperItem(level);

            event.getBlock().setType(Material.AIR);
            event.getBlock().getLocation().getWorld().dropItemNaturally(event.getBlock().getLocation(), item);
        }

        hopper.getFilter().getWhiteList().stream()
                .filter(m -> m != null)
                .forEach(m -> event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), m));
        hopper.getFilter().getBlackList().stream()
                .filter(m -> m != null)
                .forEach(m -> event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), m));
        hopper.getFilter().getVoidList().stream().
                filter(m -> m != null)
                .forEach(m -> event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), m));

        instance.getHopperManager().removeHopper(block.getLocation());
        instance.getDataManager().deleteHopper(hopper);

        instance.getPlayerDataManager().getPlayerData(player).setSyncType(null);
    }

    private void handleSyncTouch(BlockBreakEvent event) {
        if (!Methods.isSync(event.getPlayer())) return;

        ItemStack tool = event.getPlayer().getInventory().getItemInHand();
        ItemMeta meta = tool.getItemMeta();
        Location location = null;

        for (String lore : meta.getLore()) {
            if (!lore.contains(Methods.formatText("&aSync Touch"))) continue;
            String[] loreSplit = lore.split("~");
            location = Methods.unserializeLocation(loreSplit[0].replace(ChatColor.COLOR_CHAR + "", "")
                    .replace("~", ""));
            break;
        }

        Material material = event.getBlock().getType();

        if (location == null
                || material == Material.CHEST
                || Settings.SYNC_TOUCH_BLACKLIST.getStringList().contains(event.getBlock().getType().name())

                || material.name().contains("SHULKER")
                || material == CompatibleMaterial.SPAWNER.getMaterial()) {
            return;
        }

        InventoryHolder ih = (InventoryHolder) location.getBlock().getState();
        Player player = event.getPlayer();
        Collection<ItemStack> drops = event.getBlock().getDrops();
        if (drops.isEmpty()) {
            drops = new ArrayList<>();
            ItemStack itemStack = getOreDrop(CompatibleMaterial.getMaterial(material), random);
            if (itemStack != null)
                drops.add(getOreDrop(CompatibleMaterial.getMaterial(material), random));
        }
        if (meta.hasEnchant(Enchantment.SILK_TOUCH)) {
            ih.getInventory().addItem(new ItemStack(event.getBlock().getType(), 1, event.getBlock().getData()));
        } else {
            if (meta.hasEnchant(Enchantment.LOOT_BONUS_BLOCKS)) {
                int level = meta.getEnchantLevel(Enchantment.LOOT_BONUS_BLOCKS);
                int dropAmount = calculateFortuneDrops(material, level, random);
                for (int i = 0; i < dropAmount; i++) {
                    for (ItemStack is : drops) ih.getInventory().addItem(is);
                }
            } else {
                for (ItemStack is : drops) ih.getInventory().addItem(is);
            }
        }

        if (ServerVersion.isServerVersionAtLeast(ServerVersion.V1_12)) {
            event.setDropItems(false);
            return;
        }

        event.isCancelled();
        player.getItemInHand().setDurability((short) (player.getItemInHand().getDurability() + 1));
        if (player.getItemInHand().getDurability() >= player.getItemInHand().getType().getMaxDurability()) {
            player.getItemInHand().setType(null);
        }
        if (event.getExpToDrop() > 0)
            player.getWorld().spawn(event.getBlock().getLocation(), ExperienceOrb.class).setExperience(event.getExpToDrop());
        event.getBlock().setType(Material.AIR);

    }

    private int calculateFortuneDrops(Material material, int level, Random random) {
        if (material != CompatibleMaterial.COAL_ORE.getMaterial()
                && material != CompatibleMaterial.DIAMOND_ORE.getMaterial()
                && material != CompatibleMaterial.EMERALD_ORE.getMaterial()
                && material != CompatibleMaterial.NETHER_QUARTZ_ORE.getMaterial()
                && material != CompatibleMaterial.LAPIS_ORE.getMaterial()) return 1;
        if (level <= 0) return 1;
        int drops = random.nextInt(level + 2) - 1;
        if (drops < 0) drops = 0;
        return applyLapisDrops(material, random) * (drops + 1);
    }

    private int applyLapisDrops(Material material, Random random) {
        return material == Material.LAPIS_ORE ? 4 + random.nextInt(5) : 1;
    }

    private ItemStack getOreDrop(CompatibleMaterial material, Random random) {
        ItemStack item = null;
        switch (material) {
            case COAL_ORE:
                item = CompatibleMaterial.COAL.getItem();
                break;
            case DIAMOND_ORE:
                item = CompatibleMaterial.DIAMOND.getItem();
                break;
            case EMERALD_ORE:
                item = CompatibleMaterial.EMERALD.getItem();
                break;
            case GOLD_ORE:
                item = CompatibleMaterial.GOLD_ORE.getItem();
                break;
            case IRON_ORE:
                item = CompatibleMaterial.IRON_ORE.getItem();
                break;
            case LAPIS_ORE:
                item = CompatibleMaterial.LAPIS_LAZULI.getItem();
                break;
            case NETHER_QUARTZ_ORE:
                item = CompatibleMaterial.QUARTZ.getItem();
                break;
            case REDSTONE_ORE:
                item = CompatibleMaterial.REDSTONE.getItem();
                break;
        }

        switch (material) {
            case LAPIS_ORE:
                item.setAmount(random.nextInt((9 - 4) + 1) + 4);
                break;
            case REDSTONE_ORE:
                item.setAmount(random.nextInt((5 - 4) + 1) + 4);
                break;
        }
        return item;
    }
}