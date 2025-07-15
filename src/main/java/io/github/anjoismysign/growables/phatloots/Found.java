package io.github.anjoismysign.growables.phatloots;

import com.codisimus.plugins.phatloots.PhatLoot;
import com.codisimus.plugins.phatloots.PhatLootsAPI;
import com.codisimus.plugins.phatloots.loot.LootBundle;
import io.github.anjoismysign.bloblib.api.BlobLibEconomyAPI;
import io.github.anjoismysign.bloblib.vault.multieconomy.ElasticEconomy;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Found implements PhatLootsHook {

    @Override
    public void lootAt(@NotNull Location location,
                       @NotNull String phatLoot,
                       @NotNull Player player) {
        @Nullable PhatLoot real = PhatLootsAPI.getPhatLoot(phatLoot);
        if (real == null)
            return;
        LootBundle lootBundle = real.rollForLoot();
        List<ItemStack> itemStacks = lootBundle.getItemList();
        int exp = lootBundle.getExp();
        World world = location.getWorld();
        itemStacks.forEach(itemStack -> {
            world.dropItemNaturally(location, itemStack);
        });
        if (exp <= 0){
            return;
        }
        ExperienceOrb orb = (ExperienceOrb) world.spawnEntity(location, EntityType.EXPERIENCE_ORB);
        orb.setExperience(exp);
        orb.setCount(exp);

        ElasticEconomy elasticEconomy = BlobLibEconomyAPI.getInstance().getElasticEconomy();
        if (elasticEconomy.isAbsent())
            return;
        double money = lootBundle.getMoney();
        elasticEconomy.getDefault().depositPlayer(player, money);
    }
}
