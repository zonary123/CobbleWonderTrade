package com.kingpixel.wondertrade.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kingpixel.wondertrade.CobbleWonderTrade;
import com.kingpixel.wondertrade.Model.ItemModel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;

public abstract class Utils {
  public static CompletableFuture<Boolean> writeFileAsync(String filePath, String filename, String data) {
    CompletableFuture<Boolean> future = new CompletableFuture<>();

    Path path = Paths.get(new File("").getAbsolutePath() + filePath, filename);
    File file = path.toFile();

    if (!Files.exists(path.getParent())) {
      file.getParentFile().mkdirs();
    }

    try (AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open(
      path,
      StandardOpenOption.WRITE,
      StandardOpenOption.CREATE,
      StandardOpenOption.TRUNCATE_EXISTING)) {
      ByteBuffer buffer = ByteBuffer.wrap(data.getBytes(StandardCharsets.UTF_8));

      fileChannel.write(buffer, 0, buffer, new CompletionHandler<Integer, ByteBuffer>() {
        @Override
        public void completed(Integer result, ByteBuffer attachment) {
          attachment.clear();
          try {
            fileChannel.close();
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
          future.complete(true);
        }

        @Override
        public void failed(Throwable exc, ByteBuffer attachment) {
          future.complete(writeFileSync(file, data));
        }
      });
    } catch (IOException | SecurityException e) {
      CobbleWonderTrade.LOGGER.fatal("Unable to write file asynchronously, attempting sync write.");
      future.complete(future.complete(false));
    }

    return future;
  }

  public static boolean writeFileSync(File file, String data) {
    try {
      FileWriter writer = new FileWriter(file);
      writer.write(data);
      writer.close();
      return true;
    } catch (Exception e) {
      CobbleWonderTrade.LOGGER.fatal("Unable to write to file for " + CobbleWonderTrade.MOD_ID + ".\nStack Trace: ");
      e.printStackTrace();
      return false;
    }
  }

  public static CompletableFuture<Boolean> readFileAsync(String filePath, String filename,
                                                         Consumer<String> callback) {
    CompletableFuture<Boolean> future = new CompletableFuture<>();
    ExecutorService executor = Executors.newSingleThreadExecutor();

    Path path = Paths.get(new File("").getAbsolutePath() + filePath, filename);
    File file = path.toFile();

    if (!file.exists()) {
      future.complete(false);
      executor.shutdown();
      return future;
    }

    try (AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open(path, StandardOpenOption.READ)) {
      ByteBuffer buffer = ByteBuffer.allocate((int) fileChannel.size());

      Future<Integer> readResult = fileChannel.read(buffer, 0);
      readResult.get();
      buffer.flip();

      byte[] bytes = new byte[buffer.remaining()];
      buffer.get(bytes);
      String fileContent = new String(bytes, StandardCharsets.UTF_8);

      callback.accept(fileContent);

      fileChannel.close();
      executor.shutdown();
      future.complete(true);
    } catch (Exception e) {
      future.complete(readFileSync(file, callback));
      executor.shutdown();
    }

    return future;
  }

  public static boolean readFileSync(File file, Consumer<String> callback) {
    try {
      Scanner reader = new Scanner(file);

      String data = "";

      while (reader.hasNextLine()) {
        data += reader.nextLine();
      }
      reader.close();
      callback.accept(data);
      return true;
    } catch (Exception e) {
      CobbleWonderTrade.LOGGER
        .fatal("Unable to read file " + file.getName() + " for " + CobbleWonderTrade.MOD_ID + ".\nStack Trace: ");
      e.printStackTrace();
      return false;
    }
  }

  public static Gson newGson() {
    return new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
  }

  public static void broadcastMessage(String message) {
    MinecraftServer server = CobbleWonderTrade.server;
    ArrayList<ServerPlayer> players = new ArrayList<>(server.getPlayerList().getPlayers());
    for (ServerPlayer pl : players) {
      pl.sendSystemMessage(AdventureTranslator.toNative(message));
    }
  }

  public static ItemStack parseItemId(String id) {
    CompoundTag tag = new CompoundTag();
    tag.putString("id", id);
    tag.putInt("Count", 1);
    return ItemStack.of(tag);
  }

  public static ItemStack parseItemModel(ItemModel itemModel) {
    CompoundTag tag = new CompoundTag();
    tag.putString("id", itemModel.getItem());
    tag.putInt("Count", 1);
    ItemStack itemStack = ItemStack.of(tag);
    itemStack.getOrCreateTag().putInt("CustomModelData", itemModel.getCustomModelData());
    return itemStack;
  }

  public static ItemStack fill() {
    return parseItemId(CobbleWonderTrade.language.getFill());
  }
}