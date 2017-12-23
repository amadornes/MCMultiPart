/*
 * Copyright (c) 2017 Marco Rebhan (the_real_farfetchd)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT
 * OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package mcmultipart.util;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.lang.reflect.Field;

public class WorldExt {
    private static Field Chunk$storageArrays = ReflectionHelper.findField(Chunk.class, "storageArrays", "field_76652_q");

    /**
     * Workaround that is World#setBlockState but without calling Block#breakBlock on the old block
     *
     * @param self  The world instance
     * @param pos   The position where the block state should be set
     * @param state The new block state
     * @param flags Bit set:
     *              <p>
     *              Flag 1 will cause a block update. Flag 2 will send the change to clients. Flag 4 will prevent the block from
     *              being re-rendered, if this is a client world. Flag 8 will force any re-renders to run on the main thread instead
     *              of the worker pool, if this is a client world and flag 4 is clear. Flag 16 will prevent observers from seeing
     *              this change. Flags can be OR-ed
     */
    public static void setBlockStateHack(World self, BlockPos pos, IBlockState state, int flags) {
        try {
            Chunk chunk = self.getChunkFromBlockCoords(pos);

            int x = pos.getX() & 15;
            int y = pos.getY() & 15;
            int z = pos.getZ() & 15;
            ExtendedBlockStorage[] storageArrays = (ExtendedBlockStorage[]) Chunk$storageArrays.get(chunk);
            ExtendedBlockStorage storage = storageArrays[pos.getY() >> 4];
            if (storage != null) storage.set(x, y, z, state);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        self.setBlockState(pos, state, flags);
    }

    private WorldExt() {}
}
