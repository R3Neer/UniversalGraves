package eu.pb4.graves.compat;

import dev.emi.trinkets.api.TrinketEnums;
import dev.emi.trinkets.api.TrinketInventory;
import dev.emi.trinkets.api.TrinketsApi;
import dev.emi.trinkets.api.event.TrinketDropCallback;
import eu.pb4.graves.GravesApi;
import eu.pb4.graves.grave.GraveInventoryMask;
import eu.pb4.graves.other.VanillaInventoryMask;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public record TrinketsCompat(boolean preventAddition) implements GraveInventoryMask{
    private static final String GROUP_TAG = "Group";
    private static final String SLOT_TAG = "Slot";

    public static void register(boolean preventAddition) {
        GravesApi.registerInventoryMask(Identifier.fromNamespaceAndPath("universal_graves", "trinkets"), new TrinketsCompat(preventAddition));
    }

    @Override
    public void addToGrave(ServerPlayer player, ItemConsumer consumer) {
        if (preventAddition) {
            return;
        }

        TrinketsApi.getTrinketComponent(player).ifPresent(trinkets -> trinkets.forEach((ref, stack) -> {
            if (stack.isEmpty() || !GravesApi.canAddItem(player, stack)) {
                return;
            }

            var dropRule = TrinketsApi.getTrinket(stack.getItem()).getDropRule(stack, ref, player);

            dropRule = TrinketDropCallback.EVENT.invoker().drop(dropRule, stack, ref, player);

            TrinketInventory inventory = ref.inventory();

            if (dropRule == TrinketEnums.DropRule.DEFAULT) {
                dropRule = inventory.getSlotType().getDropRule();
            }

            if (dropRule == TrinketEnums.DropRule.DEFAULT) {
                if (EnchantmentHelper.has(stack, EnchantmentEffectComponents.PREVENT_EQUIPMENT_DROP)) {
                    dropRule = TrinketEnums.DropRule.DESTROY;
                } else {
                    dropRule = TrinketEnums.DropRule.DROP;
                }
            }

            if (dropRule == TrinketEnums.DropRule.DROP) {
                var nbt = new CompoundTag();
                nbt.putString(GROUP_TAG, ref.inventory().getSlotType().getGroup());
                nbt.putString(SLOT_TAG, ref.inventory().getSlotType().getName());

                consumer.addItem(stack.copy(), ref.index(), nbt);
                inventory.setItem(ref.index(), ItemStack.EMPTY);
            }
        }));
    }

    @Override
    public boolean moveToPlayerExactly(ServerPlayer player, ItemStack stack, int slot, Tag extraData) {
        var groupId = ((CompoundTag) extraData).getStringOr(GROUP_TAG, "");
        var slotId = ((CompoundTag) extraData).getStringOr(SLOT_TAG, "");

        var inventory = getInventory(player, groupId, slotId);

        if (inventory != null) {
            if (inventory.getItem(slot).isEmpty()) {
                inventory.setItem(slot, stack.copyAndClear());
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean moveToPlayerClosest(ServerPlayer player, ItemStack stack, int slot, Tag data) {
        var groupId = ((CompoundTag) data).getStringOr(GROUP_TAG, "");
        var slotId = ((CompoundTag) data).getStringOr(SLOT_TAG, "");

        var inventory = getInventory(player, groupId, slotId);

        if (inventory != null) {
            int size = inventory.getContainerSize();

            for (int i = 0; i < size; i++) {
                if (inventory.getItem(i).isEmpty()) {
                    inventory.setItem(i, stack.copyAndClear());
                    return true;
                }
            }
        }
        return false;
    }

    private TrinketInventory getInventory(ServerPlayer player, String groupId, String slotId) {
        var optional = TrinketsApi.getTrinketComponent(player);
        if (optional.isPresent()) {
            var group = optional.get().getInventory().get(groupId);

            if (group != null) {
                return group.get(slotId);
            }
        }
        return null;
    }
}
