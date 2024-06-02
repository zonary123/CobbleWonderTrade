package com.kingpixel.wondertrade.utils;

import com.cobblemon.mod.common.api.spawning.BestSpawner;
import com.cobblemon.mod.common.api.spawning.CobblemonSpawnPools;
import com.cobblemon.mod.common.api.spawning.SpawnBucket;
import com.cobblemon.mod.common.api.spawning.detail.SpawnDetail;
import com.cobblemon.mod.common.api.spawning.multiplier.WeightMultiplier;
import com.cobblemon.mod.common.pokemon.Pokemon;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Carlos Varas Alonso - 18/05/2024 18:48
 */
public class SpawnRates {

  // Stores all rarities to easily reference for a price.
  HashMap<String, Float> rarity;

  public SpawnRates() {
    rarity = new HashMap<>();
  }

  public void init() {
    ArrayList<SpawnDetail> spawnDetails = new ArrayList<>(CobblemonSpawnPools.WORLD_SPAWN_POOL.getDetails());
    // Checks for highest value for each key and adds the key with the highest weight.

    // Holds all of the buckets as a key, with another hashmap that will hold the Pokemon
    // and their weights for that bucket as the value.
    HashMap<SpawnBucket, HashMap<String, Float>> buckets = new HashMap<>();
    Set<String> pokemon = new HashSet<>();

    // Gets all buckets and adds them to the HashMap.
    for (SpawnBucket bucket : BestSpawner.INSTANCE.getConfig().getBuckets()) {
      buckets.put(bucket, new HashMap<>());
    }

    // For each SpawnDetail
    for (SpawnDetail detail : spawnDetails) {

      // Finds the highest weight multiplier.
      float weightMultiplier = 0;
      for (WeightMultiplier multiplier : detail.getWeightMultipliers()) {
        if (multiplier.getMultiplier() > weightMultiplier) {
          weightMultiplier = multiplier.getMultiplier();
        }
      }

      // Makes sure there was a weight multiplier. Chooses highest weight.
      float highestWeight = Math.max(detail.getWeight() * weightMultiplier, detail.getWeight());

      // If the bucket of the detail doesn't contain the Pokemon, or the detail value is higher than
      // the currently saved value, add the details combined weight.
      if (!buckets.get(detail.getBucket()).containsKey(detail.getName().getString()) ||
        highestWeight > buckets.get(detail.getBucket()).get(detail.getName().getString())) {

        // Adds the weight to the bucket.
        buckets.get(detail.getBucket()).put(detail.getName().getString(), highestWeight);

      }

      // Stores the Pokemon name so we can compare the different weights after.
      pokemon.add(detail.getName().getString());
    }

    // Calculates the total weight of all buckets.
    HashMap<SpawnBucket, Float> totalWeights = new HashMap<>();
    for (SpawnBucket bucket : buckets.keySet()) {

      // Finds the total weight of all Pokemon in the bucket.
      float bucketTotalWeight = 0;
      for (float weight : new ArrayList<>(buckets.get(bucket).values())) {
        bucketTotalWeight += weight;
      }
      totalWeights.put(bucket, bucketTotalWeight);
    }

    for (String poke : pokemon) {
      // Iterate over each bucket and find the highest weight of them all.
      BigDecimal highestWeight = new BigDecimal(0);

      // Checks each bucket, calculates the weight and compares it to the current one.
      for (SpawnBucket bucket : buckets.keySet()) {

        // Calculates the weight and compares to previous ones to find the highest.
        if (buckets.get(bucket).containsKey(poke)) {
          BigDecimal rarityInBucket = new BigDecimal(buckets.get(bucket).get(poke) / totalWeights.get(bucket));

          BigDecimal totalWeight = rarityInBucket.multiply(new BigDecimal(bucket.getWeight()));

          if (totalWeight.compareTo(highestWeight) > 0) {
            highestWeight = totalWeight;
          }
        }
      }
      rarity.put(poke, highestWeight.multiply(new BigDecimal(100)).floatValue());
    }
  }

  /**
   * Gets the rarity hashmap.
   *
   * @return rarity of given pokemon as a float.
   */
  public float getRarity(Pokemon pokemon) {
    return rarity.get(pokemon.getDisplayName().getString()) == null ? -1 :
      rarity.get(pokemon.getDisplayName().getString());
  }
}