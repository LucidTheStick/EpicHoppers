package com.songoda.epichoppers.api.events;

import com.songoda.epichoppers.hopper.Hopper;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

public class HopperBreakEvent extends HopperEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    public HopperBreakEvent(Player player, Hopper hopper) {
        super(player, hopper);
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}