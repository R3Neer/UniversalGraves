package eu.pb4.graves.other;

import eu.pb4.graves.grave.Grave;
import net.minecraft.server.level.ServerPlayer;

public interface GraveUnlockCost {
    GenericCost<?> quote(Grave grave, boolean owner);

    default GenericCost<?> quote(Grave grave, ServerPlayer player) {
        return this.quote(grave, grave.isOwner(player));
    }

    boolean requiresPayment();

    default boolean unlocksPerPlayer() {
        return false;
    }

    default boolean allowsNonOwnerPayment() {
        return false;
    }

    record Static(GenericCost<?> cost) implements GraveUnlockCost {
        @Override
        public GenericCost<?> quote(Grave grave, boolean owner) {
            return this.cost;
        }

        @Override
        public boolean requiresPayment() {
            return !this.cost.isFree();
        }
    }
}
