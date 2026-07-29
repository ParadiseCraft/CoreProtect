package net.coreprotect.command;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

import net.coreprotect.command.lookup.BlockLookupThread;
import net.coreprotect.command.lookup.ChestTransactionLookupThread;
import net.coreprotect.command.lookup.EntityInteractionLookupThread;
import net.coreprotect.command.lookup.StandardLookupThread;
import net.coreprotect.config.ConfigHandler;
import net.coreprotect.language.Phrase;
import net.coreprotect.model.action.EntityActionFilter;
import net.coreprotect.model.lookup.LookupOutputMode;
import net.coreprotect.model.lookup.LookupRollbackState;
import net.coreprotect.utility.Chat;
import net.coreprotect.utility.ChatMessage;
import net.coreprotect.utility.Color;
import net.coreprotect.utility.WorldUtils;

public class PageCommand {
    public static void runCommand(CommandSender player, Command command, boolean permission, String[] args) {
        if (!permission) {
            Chat.sendMessage(player, new ChatMessage(Phrase.build(Phrase.NO_PERMISSION)).build());
            return;
        }

        int resultc = args.length;
        if (resultc < 2 || !args[1].equals(args[1].replaceAll("[^0-9]", ""))) {
            Chat.sendMessage(player, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.MISSING_PARAMETERS, Color.WHITE, "/co page <page>"));
            return;
        }

        if (ConfigHandler.converterRunning) {
            Chat.sendMessage(player, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.UPGRADE_IN_PROGRESS));
            return;
        }
        if (ConfigHandler.purgeRunning) {
            Chat.sendMessage(player, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.PURGE_IN_PROGRESS));
            return;
        }

        Integer type = ConfigHandler.lookupType.get(player.getName());
        if (type == null || type == 0) {
            Chat.sendMessage(player, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.MISSING_PARAMETERS, "/co page <page>"));
            return;
        }

        if (ConfigHandler.lookupThrottle.get(player.getName()) != null) {
            Object[] lookupThrottle = ConfigHandler.lookupThrottle.get(player.getName());
            if ((boolean) lookupThrottle[0] || ((System.currentTimeMillis() - (long) lookupThrottle[1])) < 50) {
                Chat.sendMessage(player, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.DATABASE_BUSY));
                return;
            }
        }

        int parseRows = CommandParser.parseRows(args);

        if (type == 1) {
            boolean defaultRe = true;
            int p = 0;
            int re = 7;
            if (parseRows > 0) {
                re = parseRows;
            }
            if (resultc > 1) {
                String pages = args[1];
                if (pages.contains(":")) {
                    String[] data = pages.split(":");
                    pages = data[0];
                    String results = "";
                    if (data.length > 1) {
                        results = data[1];
                    }
                    results = results.replaceAll("[^0-9]", "");
                    if (results.length() > 0 && results.length() < 10) {
                        int r = Integer.parseInt(results);
                        if (r > 0) {
                            re = r;
                            defaultRe = false;
                        }
                    }
                }
                pages = pages.replaceAll("[^0-9]", "");
                if (pages.length() > 0 && pages.length() < 10) {
                    int pa = Integer.parseInt(pages);
                    if (pa > 0) {
                        p = pa;
                    }
                }
            }

            if (re > 1000) {
                re = 1000;
            }
            if (re > 100 && !(player instanceof ConsoleCommandSender)) {
                re = 100;
            }

            if (p <= 0) {
                p = 1;
            }
            String lcommand = ConfigHandler.lookupCommand.get(player.getName());
            String[] data = lcommand.split("\\.");
            int x = Integer.parseInt(data[0]);
            int y = Integer.parseInt(data[1]);
            int z = Integer.parseInt(data[2]);
            int wid = Integer.parseInt(data[3]);
            int x2 = Integer.parseInt(data[4]);
            int y2 = Integer.parseInt(data[5]);
            int z2 = Integer.parseInt(data[6]);
            if (defaultRe) {
                re = Integer.parseInt(data[7]);
            }

            String bc = x + "." + y + "." + z + "." + wid + "." + x2 + "." + y2 + "." + z2 + "." + re;
            ConfigHandler.lookupCommand.put(player.getName(), bc);

            String world = WorldUtils.getWorldName(wid);
            double dx = 0.5 * (x + x2);
            double dy = 0.5 * (y + y2);
            double dz = 0.5 * (z + z2);
            final Location location = new Location(Bukkit.getServer().getWorld(world), dx, dy, dz);

            Runnable runnable = new ChestTransactionLookupThread(player, command, location, p, re);
            Thread thread = new Thread(runnable);
            thread.start();
        }
        else if (type == 2 || type == 3 || type == 7 || type == 8 || type == 9) {
            boolean defaultRe = true;
            int page = 1;
            int re = 7;
            if (parseRows > 0) {
                re = parseRows;
            }
            if (resultc > 1) {
                String pages = args[1];
                if (pages.contains(":")) {
                    String[] data = pages.split(":");
                    pages = data[0];
                    String results = "";
                    if (data.length > 1) {
                        results = data[1];
                    }
                    results = results.replaceAll("[^0-9]", "");
                    if (results.length() > 0 && results.length() < 10) {
                        int r = Integer.parseInt(results);
                        if (r > 0) {
                            re = r;
                            defaultRe = false;
                        }
                    }
                }
                pages = pages.replaceAll("[^0-9]", "");
                if (pages.length() > 0 && pages.length() < 10) {
                    int p = Integer.parseInt(pages);
                    if (p > 0) {
                        page = p;
                    }
                }
            }

            if (re > 1000) {
                re = 1000;
            }
            if (re > 100 && !(player instanceof ConsoleCommandSender)) {
                re = 100;
            }

            if (type == 9) {
                Runnable runnable = new EntityInteractionLookupThread(player, command, page, re);
                Thread thread = new Thread(runnable);
                thread.start();
                return;
            }

            String lcommand = ConfigHandler.lookupCommand.get(player.getName());
            String[] data = lcommand.split("\\.");
            int x = Integer.parseInt(data[0]);
            int y = Integer.parseInt(data[1]);
            int z = Integer.parseInt(data[2]);
            int wid = Integer.parseInt(data[3]);
            int lookupType = Integer.parseInt(data[4]);
            if (defaultRe) {
                re = Integer.parseInt(data[5]);
            }

            String bc = x + "." + y + "." + z + "." + wid + "." + lookupType + "." + re;
            ConfigHandler.lookupCommand.put(player.getName(), bc);

            String world = WorldUtils.getWorldName(wid);
            final Block block = Bukkit.getServer().getWorld(world).getBlockAt(x, y, z);
            final BlockState blockState = block.getState();

            Runnable runnable = new BlockLookupThread(player, command, block, blockState, page, re, type);
            Thread thread = new Thread(runnable);
            thread.start();
        }
        else if (type == 4 || type == 5) {
            boolean defaultRe = true;
            int page = 1;
            int re = 7;
            if (parseRows > 0) {
                re = parseRows;
            }
            if (resultc > 1) {
                String pages = args[1];
                if (pages.contains(":")) {
                    String[] data = pages.split(":");
                    pages = data[0];
                    String results = "";
                    if (data.length > 1) {
                        results = data[1];
                    }
                    results = results.replaceAll("[^0-9]", "");
                    if (results.length() > 0 && results.length() < 10) {
                        int r = Integer.parseInt(results);
                        if (r > 0) {
                            re = r;
                            defaultRe = false;
                        }
                    }
                }
                pages = pages.replaceAll("[^0-9]", "");
                if (pages.length() > 0 && pages.length() < 10) {
                    int p = Integer.parseInt(pages);
                    if (p > 0) {
                        page = p;
                    }
                }
            }

            if (re > 1000) {
                re = 1000;
            }
            if (re > 100 && !(player instanceof ConsoleCommandSender)) {
                re = 100;
            }

            String lcommand = ConfigHandler.lookupCommand.get(player.getName());
            String[] data = lcommand.split("\\.");
            int x = Integer.parseInt(data[0]);
            int y = Integer.parseInt(data[1]);
            int z = Integer.parseInt(data[2]);
            int wid = Integer.parseInt(data[3]);
            long timeStart = Long.parseLong(data[4]);
            long timeEnd = Long.parseLong(data[5]);
            int argNoisy = Integer.parseInt(data[6]);
            int argExcluded = Integer.parseInt(data[7]);
            int argRestricted = Integer.parseInt(data[8]);
            int argWid = Integer.parseInt(data[9]);
            if (defaultRe) {
                re = Integer.parseInt(data[10]);
            }

            String bc = x + "." + y + "." + z + "." + wid + "." + timeStart + "." + timeEnd + "." + argNoisy + "." + argExcluded + "." + argRestricted + "." + argWid + "." + re;
            ConfigHandler.lookupCommand.put(player.getName(), bc);

            String world = WorldUtils.getWorldName(wid);
            Location location = null;
            if (world != null) {
                org.bukkit.World bukkitWorld = Bukkit.getServer().getWorld(world);
                if (bukkitWorld != null) {
                    location = new Location(bukkitWorld, x, y, z);
                }
            }

            List<String> rollbackusers = ConfigHandler.lookupUlist.get(player.getName());
            List<Object> argBlocks = ConfigHandler.lookupBlist.get(player.getName());
            Map<Object, Boolean> argExclude = ConfigHandler.lookupElist.get(player.getName());
            List<String> argExcludeUsers = ConfigHandler.lookupEUserlist.get(player.getName());
            List<Integer> argAction = ConfigHandler.lookupAlist.get(player.getName());
            EntityActionFilter argEntityActionFilter = ConfigHandler.lookupEntityActionFilter.getOrDefault(player.getName(), EntityActionFilter.DEFAULT);
            List<String> argFilters = ConfigHandler.lookupFlist.getOrDefault(player.getName(), Collections.emptyList());
            Integer[] argRadius = ConfigHandler.lookupRadius.get(player.getName());
            String ts = ConfigHandler.lookupTime.get(player.getName());
            LookupOutputMode outputMode = ConfigHandler.lookupOutputMode.getOrDefault(player.getName(), LookupOutputMode.DETAIL);
            LookupRollbackState rollbackState = ConfigHandler.lookupRollbackState.getOrDefault(player.getName(), LookupRollbackState.ANY);

            Runnable runnable = new StandardLookupThread(player, command, rollbackusers, argBlocks, argExclude, argExcludeUsers, argAction, argEntityActionFilter, argFilters, argRadius, location, x, y, z, wid, argWid, timeStart, timeEnd, argNoisy, argExcluded, argRestricted, page, re, type, ts, outputMode, rollbackState);
            Thread thread = new Thread(runnable);
            thread.start();
        }
        else {
            Chat.sendMessage(player, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.MISSING_PARAMETERS, "/co page <page>"));
        }
    }
}
