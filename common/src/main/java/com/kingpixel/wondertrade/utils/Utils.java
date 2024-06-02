package com.kingpixel.wondertrade.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kingpixel.wondertrade.CobbleWonderTrade;
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

/**
 * Abstract class that contains some utility methods.
 */
public abstract class Utils {
  /**
   * Method to write some data to file.
   *
   * @param filePath the directory to write the file to
   * @param filename the name of the file
   * @param data     the data to write to file
   *
   * @return CompletableFuture if writing to file was successful
   */
  public static CompletableFuture<Boolean> writeFileAsync(String filePath, String filename, String data) {
    CompletableFuture<Boolean> future = new CompletableFuture<>();

    Path path = Paths.get(new File("").getAbsolutePath() + filePath, filename);
    File file = path.toFile();

    // If the path doesn't exist, create it.
    if (!Files.exists(path.getParent())) {
      file.getParentFile().mkdirs();
    }

    // Write the data to file.
    try (AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open(
      path,
      StandardOpenOption.WRITE,
      StandardOpenOption.CREATE,
      StandardOpenOption.TRUNCATE_EXISTING
    )) {
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


  /**
   * Method to write a file sync.
   *
   * @param file the location to write.
   * @param data the data to write.
   *
   * @return true if the write was successful.
   */
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


  /**
   * Method to read a file asynchronously
   *
   * @param filePath the path of the directory to find the file at
   * @param filename the name of the file
   * @param callback a callback to deal with the data read
   *
   * @return true if the file was read successfully
   */
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
      ByteBuffer buffer = ByteBuffer.allocate((int) fileChannel.size()); // Allocate buffer for the entire file

      Future<Integer> readResult = fileChannel.read(buffer, 0);
      readResult.get(); // Wait for the read operation to complete
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

  /**
   * Method to read files sync.
   *
   * @param file     The file to read
   * @param callback what to do with the read data.
   *
   * @return true if the file could be read successfully.
   */
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
      CobbleWonderTrade.LOGGER.fatal("Unable to read file " + file.getName() + " for " + CobbleWonderTrade.MOD_ID + ".\nStack Trace: ");
      e.printStackTrace();
      return false;
    }
  }

  /**
   * Method to create a new gson builder.
   *
   * @return Gson instance.
   */
  public static Gson newGson() {
    return new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
  }

  public static void broadcastMessage(String message) {
    MinecraftServer server = CobbleWonderTrade.server;
    ArrayList<ServerPlayer> players = new ArrayList<>(server.getPlayerList().getPlayers());
    for (ServerPlayer pl : players) {
      pl.sendSystemMessage(TextUtil.parseHexCodes(message));
    }
  }

  public static ItemStack parseItemId(String id) {
    CompoundTag tag = new CompoundTag();
    tag.putString("id", id);
    tag.putInt("Count", 1);
    return ItemStack.of(tag);
  }
}