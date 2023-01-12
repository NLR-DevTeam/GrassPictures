package cn.whitrayhb.grasspics;

import net.mamoe.mirai.console.command.CommandSender;
import net.mamoe.mirai.console.command.ConsoleCommandSender;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class Cooler {
    private static final ConcurrentHashSet<Long> set = new ConcurrentHashSet<Long>();
    public static void lock(CommandSender sender,int second){
        if(sender instanceof ConsoleCommandSender){
            sender.sendMessage("请勿在控制台中使用该命令");
            return;
        }else {
            set.add(Objects.requireNonNull(sender.getUser()).getId());
        }
        Thread unlockThread = new Thread(() ->{
            try {
                Thread.sleep(second* 1000L);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            unlock(sender);
        });
        unlockThread.start();
    }
    public static void unlock(CommandSender sender){
        set.remove(Objects.requireNonNull(sender.getUser()).getId());
    }
    public static boolean isLocked(CommandSender sender){
        if(sender instanceof ConsoleCommandSender) return false;
        return set.contains(Objects.requireNonNull(sender.getUser()).getId());
    }
}

class ConcurrentHashSet<T>{
    private final ConcurrentHashMap<T, Integer> map;
    ConcurrentHashSet(){
        map = new ConcurrentHashMap<T,Integer>();
    }
    public void add(T value){
        map.put(value,1);
    }
    public void remove(T value){
        map.remove(value,1);
    }
    public Boolean contains(T value){
        return map.containsKey(value);
    }
    public String mapReturn(){
        return String.valueOf(map);
    }
}

