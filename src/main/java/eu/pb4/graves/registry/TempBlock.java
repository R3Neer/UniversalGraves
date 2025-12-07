package eu.pb4.graves.registry;

import eu.pb4.polymer.core.api.block.PolymerBlock;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import xyz.nucleoid.packettweaker.PacketContext;

public class TempBlock extends Block implements PolymerBlock {
    public TempBlock(Properties settings) {
        super(settings);
    }

    @Override
    public boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        return false;
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state, PacketContext context) {
        return Blocks.AIR.defaultBlockState();
    }
}
