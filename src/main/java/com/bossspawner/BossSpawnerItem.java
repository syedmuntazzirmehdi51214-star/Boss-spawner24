package com.bossspawner;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class BossSpawnerItem extends Item {
    private static List<EntityType<?>> cache;

    public BossSpawnerItem(Properties props) {
        super(props);
    }

    private static List<EntityType<?>> list() {
        if (cache == null) {
            cache = new ArrayList<>();
            for (EntityType<?> t : ForgeRegistries.ENTITIES.getValues()) {
                ResourceLocation id = t.getRegistryName();
                if (id == null) continue;
                String s = id.toString();
                boolean vanillaBoss = s.equals("minecraft:ender_dragon") || s.equals("minecraft:wither");
                if (!id.getNamespace().equals("minecraft") || vanillaBoss) {
                    cache.add(t);
                }
            }
            cache.sort(Comparator.comparing(t -> t.getRegistryName().toString()));
        }
        return cache;
    }

    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (world.isClientSide) return ActionResult.success(stack);

        List<EntityType<?>> types = list();
        if (types.isEmpty()) {
            player.displayClientMessage(new StringTextComponent("No modded entities found!"), true);
            return ActionResult.fail(stack);
        }

        int idx = stack.getOrCreateTag().getInt("idx") % types.size();

        if (player.isShiftKeyDown()) {
            idx = (idx + 1) % types.size();
            stack.getOrCreateTag().putInt("idx", idx);
            player.displayClientMessage(new StringTextComponent("Selected: "
                    + types.get(idx).getRegistryName() + " (" + (idx + 1) + "/" + types.size() + ")"), true);
            return ActionResult.success(stack);
        }

        EntityType<?> type = types.get(idx);
        BlockRayTraceResult hit = getPlayerPOVHitResult(world, player, RayTraceContext.FluidMode.NONE);
        BlockPos pos = hit.getType() == RayTraceResult.Type.MISS
                ? player.blockPosition().offset(player.getLookAngle().x * 3, 0, player.getLookAngle().z * 3)
                : hit.getBlockPos().relative(hit.getDirection());

        type.spawn((ServerWorld) world, null, player, pos, SpawnReason.SPAWN_EGG, true, false);
        player.getCooldowns().addCooldown(this, 60);
        player.displayClientMessage(new StringTextComponent("Spawned " + type.getRegistryName()), true);
        return ActionResult.success(stack);
    }
}
