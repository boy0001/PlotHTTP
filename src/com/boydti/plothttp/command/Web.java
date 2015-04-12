package com.boydti.plothttp.command;

import java.io.File;
import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;

import com.boydti.plothttp.Main;
import com.boydti.plothttp.object.WebResource;
import com.intellectualcrafters.jnbt.CompoundTag;
import com.intellectualcrafters.plot.commands.SubCommand;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.Permissions;
import com.intellectualcrafters.plot.util.SchematicHandler;
import com.intellectualcrafters.plot.util.TaskManager;
import com.intellectualcrafters.plot.util.bukkit.UUIDHandler;

public class Web extends SubCommand {

    public Web() {
        super("web", "plots.web", "Web related commands", "", "web", CommandCategory.DEBUG, false);
    }
    
    public void noargs(PlotPlayer player) {
        ArrayList<String> args = new ArrayList<String>();
        if (Permissions.hasPermission(player, "plots.web.reload")) {
            args.add("reload");
        }
        if (Permissions.hasPermission(player, "plots.web.download")) {
            args.add("download");
        }
        if (Permissions.hasPermission(player, "plots.web.upload")) {
            args.add("upload");
        }
        if (args.size() == 0) {
            MainUtil.sendMessage(player, C.NO_PERMISSION, "plots.web.<reload|download|upload>");
            return;
        }
        MainUtil.sendMessage(player, C.COMMAND_SYNTAX, "/plot web <" + StringUtils.join(args, "|") + ">");
    }

    @Override
    public boolean execute(final PlotPlayer player, String... args) {
        if (args.length == 0) {
            noargs(player);
            return false;
        }
        switch (args[0]) {
            case "reload": {
                if (!Permissions.hasPermission(player, "plots.web.reload")) {
                    MainUtil.sendMessage(player, C.NO_PERMISSION, "plots.web.reload");
                    return false;
                }
                Main.plugin.onDisable();
                Main.plugin.onEnable();
                MainUtil.sendMessage(player, "&aReloaded success!");
                return true;
            }
            case "download": {
                if (!Permissions.hasPermission(player, "plots.web.download")) {
                    MainUtil.sendMessage(player, C.NO_PERMISSION, "plots.web.reload");
                    return false;
                }
                final Plot plot = MainUtil.getPlot(player.getLocation());
                if (plot == null || !plot.isAdded(player.getUUID())) {
                    MainUtil.sendMessage(player, C.NO_PLOT_PERMS);
                    return true;
                }
                final String id = WebResource.nextId();
                final String port;
                if (Main.port != 80) {
                    port = ":" + Main.port;
                }
                else {
                    port = "";
                }
                MainUtil.sendMessage(player, "&6Please wait while we process your plot...");
                final CompoundTag sch = SchematicHandler.manager.getCompoundTag(plot.world, plot.id);
                final String o = UUIDHandler.getName(plot.owner);
                final String owner = o == null ? "unknown" : o;
                if (sch == null) {
                    MainUtil.sendMessage(player, "&7Could not export &c" + plot.id);
                    return false;
                } else {
                    TaskManager.runTaskAsync(new Runnable() {
                        @Override
                        public void run() {
                            MainUtil.sendMessage(player, "&6Generating link...");
                            String filename = plot.id.x + ";" + plot.id.y + "," + plot.world + "," + owner + ".schematic";
                            final boolean result = SchematicHandler.manager.save(sch, Main.plugin.getDataFolder() + File.separator + "downloads" + File.separator + filename);
                            if (!result) {
                                MainUtil.sendMessage(player, "&7Could not export &c" + plot.id);
                            } else {
                                MainUtil.sendMessage(null, "&7 - &a  success: " + plot.id);
                                WebResource.downloads.put(id, filename);
                                MainUtil.sendMessage(player, "Download the file:\n" + Main.ip + port + "/web?id=" + id);
                            }
                        }
                    });
                }
                return true;
            }
            case "upload": {
                if (!Permissions.hasPermission(player, "plots.web.upload")) {
                    MainUtil.sendMessage(player, C.NO_PERMISSION, "plots.web.reload");
                    return false;
                }
                
                return false;
            }
            default: {
                noargs(player);
                return false;
            }
        }
    }
    
}