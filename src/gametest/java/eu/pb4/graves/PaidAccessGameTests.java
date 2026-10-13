package eu.pb4.graves;

import eu.pb4.graves.config.ConfigManager;
import eu.pb4.graves.grave.Grave;
import eu.pb4.graves.grave.GraveType;
import eu.pb4.graves.other.DynamicLevelUnlockCost;
import eu.pb4.graves.other.GenericCost;
import eu.pb4.graves.other.GraveUnlockCost;
import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.level.GameType;

public final class PaidAccessGameTests {
    private static ServerPlayer player(GameTestHelper h) {
        // Vanilla's mock overrides gameMode() to creative, even after setGameMode().
        var fixture = h.makeMockServerPlayerInLevel();
        var player = new ServerPlayer(h.getLevel().getServer(), h.getLevel(),
                new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(), "grave-survival"),
                net.minecraft.server.level.ClientInformation.createDefault());
        player.connection = fixture.connection;
        player.connection.player = player;
        player.setGameMode(GameType.SURVIVAL);
        h.assertFalse(player.isCreative(), "payment fixture must actually use survival");
        return player;
    }

    private static Grave grave(GameTestHelper h, ServerPlayer owner) {
        return grave(h, owner, List.of());
    }

    private static Grave grave(GameTestHelper h, ServerPlayer owner,
                              List<eu.pb4.graves.grave.PositionedItemStack> items) {
        return new Grave(123L, owner.getGameProfile(), (byte) 0x7f, HumanoidArm.RIGHT,
                BlockPos.ZERO, h.getLevel().dimension().identifier(), GraveType.VIRTUAL,
                System.currentTimeMillis() / 1000, h.getLevel().getGameTime(), 0,
                Grave.DEFAULT_DEATH_CAUSE, List.of(), items, true, 0);
    }

    private static void withCost(GraveUnlockCost cost, boolean individual, Runnable test) {
        var config = ConfigManager.getConfig();
        var oldCost = config.interactions.cost;
        boolean oldIndividual = config.interactions.allowNonOwnerPaidUnlock;
        int oldProtection = config.protection.protectionTime;
        boolean oldRealTime = config.protection.useRealTime;
        try {
            config.interactions.cost = cost;
            config.interactions.allowNonOwnerPaidUnlock = individual;
            config.protection.protectionTime = -1;
            config.protection.useRealTime = false;
            test.run();
        } finally {
            config.interactions.cost = oldCost;
            config.interactions.allowNonOwnerPaidUnlock = oldIndividual;
            config.protection.protectionTime = oldProtection;
            config.protection.useRealTime = oldRealTime;
        }
    }

    @GameTest
    public void protectedGraveAllowsIndividualPaymentAndPersistsIt(GameTestHelper h) {
        withCost(new DynamicLevelUnlockCost(3, 4, DynamicLevelUnlockCost.EnchantmentCostMode.LEVELS,
                2, 1, 3), true, () -> {
            var owner = player(h);
            var visitor = player(h);
            var other = player(h);
            var grave = grave(h, owner);
            h.assertTrue(grave.isProtected(), "fixture must be protected");
            h.assertTrue(grave.canOpen(visitor), "protected grave allows reaching payment UI");
            h.assertFalse(grave.canTakeFrom(visitor), "opening UI must not grant unpaid access");
            h.assertTrue(grave.getUnlockCost(owner).count() == 2, "owner price");
            h.assertTrue(grave.getUnlockCost(visitor).count() == 6, "non-owner multiplier");
            visitor.experienceLevel = 5;
            h.assertFalse(grave.payForUnlock(visitor), "insufficient levels must fail");
            h.assertFalse(grave.canTakeFrom(visitor), "failed payment must not grant access");
            visitor.experienceLevel = 10;
            h.assertTrue(grave.payForUnlock(visitor), "visitor payment must succeed");
            h.assertTrue(visitor.experienceLevel == 4, "exact quoted cost must be charged");
            h.assertTrue(grave.canTakeFrom(visitor), "paid visitor can withdraw");
            h.assertFalse(grave.canTakeFrom(other), "payment must not unlock other players");
            h.assertFalse(grave.canTakeFrom(owner), "owner must still make own payment");
            h.assertFalse(grave.payForUnlock(visitor), "repeat payment must be rejected");
            h.assertTrue(visitor.experienceLevel == 4, "repeat payment must not charge again");
            var restored = new Grave();
            restored.readNbt(grave.writeNbt(new CompoundTag(), h.getLevel().registryAccess()),
                    h.getLevel().registryAccess(), h.getLevel().getServer().getFixerUpper(), 0, 0);
            h.assertTrue(restored.canTakeFrom(visitor), "paid UUID survives NBT round trip");
            h.assertFalse(restored.canTakeFrom(other), "unpaid player remains blocked after reload");
            restored.disableProtection();
            h.assertFalse(restored.canTakeFrom(other), "protection removal must not bypass payment");
        });
        h.succeed();
    }

    @GameTest
    public void dynamicQuoteUsesRemainingItems(GameTestHelper h) {
        withCost(new DynamicLevelUnlockCost(1, 4, DynamicLevelUnlockCost.EnchantmentCostMode.LEVELS,
                0, 1, 3), true, () -> {
            var owner = player(h);
            var stack = new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.COBBLESTONE, 64);
            var grave = grave(h, owner, List.of(new eu.pb4.graves.grave.PositionedItemStack(
                    stack, 0, null, null, java.util.Set.of())));
            h.assertTrue(grave.getUnlockCost(owner).count() == 1, "stored full stack contributes cost");
            stack.setCount(0);
            h.assertTrue(grave.getUnlockCost(owner).count() == 0, "new quote reflects removed contents");
        });
        h.succeed();
    }

    @GameTest
    public void configurationReloadPreservesPaidAccessSettings(GameTestHelper h) {
        var previous = ConfigManager.getConfig();
        try {
            var config = new eu.pb4.graves.config.Config();
            config.interactions.cost = new DynamicLevelUnlockCost(3, 4,
                    DynamicLevelUnlockCost.EnchantmentCostMode.LEVELS, 2, 1, 3);
            config.interactions.allowNonOwnerPaidUnlock = true;
            ConfigManager.overrideConfig(config, h.getLevel().registryAccess());
            h.assertTrue(ConfigManager.loadConfig(h.getLevel().registryAccess()), "configuration reload succeeds");
            h.assertTrue(ConfigManager.getConfig().interactions.allowNonOwnerPaidUnlock, "individual access survives reload");
            h.assertTrue(ConfigManager.getConfig().interactions.cost instanceof DynamicLevelUnlockCost,
                    "dynamic cost survives reload");
            h.assertTrue(ConfigManager.getConfig().version == ConfigManager.VERSION, "configuration version is current");
        } finally {
            ConfigManager.overrideConfig(previous, h.getLevel().registryAccess());
            ConfigManager.loadConfig(h.getLevel().registryAccess());
        }
        h.succeed();
    }

    @GameTest
    public void staticCostSupportsIndividualAccess(GameTestHelper h) {
        withCost(new GraveUnlockCost.Static(new GenericCost<>(GenericCost.Type.LEVEL, null, 3)), true, () -> {
            var owner = player(h);
            var visitor = player(h);
            var grave = grave(h, owner);
            visitor.experienceLevel = 5;
            h.assertTrue(grave.payForUnlock(visitor), "static cost supports protected visitor payment");
            h.assertTrue(visitor.experienceLevel == 2, "static price charged exactly");
            h.assertTrue(grave.canTakeFrom(visitor), "static payment grants individual access");
            h.assertFalse(grave.canTakeFrom(owner), "static payment is not global in individual mode");
        });
        h.succeed();
    }

    @GameTest
    public void legacyGlobalPaymentAndProtectedAccessRemainSupported(GameTestHelper h) {
        withCost(new GraveUnlockCost.Static(new GenericCost<>(GenericCost.Type.LEVEL, null, 3)), false, () -> {
            var owner = player(h);
            var visitor = player(h);
            var grave = grave(h, owner);
            h.assertFalse(grave.canOpen(visitor), "legacy protection blocks outsiders");
            owner.experienceLevel = 5;
            h.assertTrue(grave.payForUnlock(owner), "owner can pay legacy global cost");
            h.assertTrue(grave.canTakeFrom(owner), "owner can withdraw");
            h.assertFalse(grave.canTakeFrom(visitor), "global payment does not remove protection");
            grave.disableProtection();
            h.assertTrue(grave.canTakeFrom(visitor), "legacy global access after protection removal");
        });
        h.succeed();
    }
}
