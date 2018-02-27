package mcmultipart.api.item;

import mcmultipart.api.container.IPartInfo;
import mcmultipart.api.multipart.IMultipart;
import mcmultipart.api.multipart.IMultipartTile;
import mcmultipart.api.multipart.MultipartHelper;
import mcmultipart.api.slot.IPartSlot;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemBlockMultipart extends ItemBlock {

    protected final IMultipart multipartBlock;

    public ItemBlockMultipart(Block block, IMultipart multipartBlock) {
        super(block);
        this.multipartBlock = multipartBlock;
    }

    public <T extends Block & IMultipart> ItemBlockMultipart(T block) {
        this(block, block);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX,
                                      float hitY, float hitZ) {
        return place(player, world, pos, hand, facing, hitX, hitY, hitZ, this, this.block::getStateForPlacement, multipartBlock,
                this::placeBlockAtTested, ItemBlockMultipart::placePartAt);
    }

    public static EnumActionResult place(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX,
                                         float hitY, float hitZ, Item item, IBlockPlacementInfo stateProvider, IMultipart multipartBlock,
                                         IBlockPlacementLogic blockLogic, IPartPlacementLogic partLogic) {
        ItemStack stack = player.getHeldItem(hand);

        if (!stack.isEmpty()) {
            int meta = item.getMetadata(stack.getMetadata());
            float d = Math.abs(hitX * facing.getFrontOffsetX() + hitY * facing.getFrontOffsetY() + hitZ * facing.getFrontOffsetZ());
            if (d == 0 || d == 1 || !placeAt(stack, player, hand, world, pos, facing, hitX, hitY, hitZ, stateProvider, meta, multipartBlock,
                    blockLogic, partLogic)) {
                pos = pos.offset(facing);
                if (!placeAt(stack, player, hand, world, pos, facing, hitX, hitY, hitZ, stateProvider, meta, multipartBlock, blockLogic,
                        partLogic)) {
                    return EnumActionResult.FAIL;
                }
            }
            SoundType soundtype = world.getBlockState(pos).getBlock().getSoundType(world.getBlockState(pos), world, pos, player);
            world.playSound(player, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F,
                    soundtype.getPitch() * 0.8F);
            if (!player.capabilities.isCreativeMode) {
                stack.shrink(1);
            }

            return EnumActionResult.SUCCESS;
        }
        return EnumActionResult.FAIL;
    }

    public static boolean placeAt(ItemStack stack, EntityPlayer player, EnumHand hand, World world, BlockPos pos, EnumFacing facing,
                                  float hitX, float hitY, float hitZ, IBlockPlacementInfo stateProvider, int meta, IMultipart multipartBlock,
                                  IBlockPlacementLogic blockLogic, IPartPlacementLogic partLogic) {
        IBlockState state = stateProvider.getStateForPlacement(world, pos, facing, hitX, hitY, hitZ, meta, player, hand);
        AxisAlignedBB bb = state.getCollisionBoundingBox(world, pos);
        if ((bb == null || world.checkNoEntityCollision(bb.offset(pos)))
                && blockLogic.place(stack, player, world, pos, facing, hitX, hitY, hitZ, state)) {
            return true;
        }
        bb = multipartBlock.getCollisionBoundingBox(world, pos, state);
        return (bb == null || world.checkNoEntityCollision(bb.offset(pos)))
                && partLogic.placePart(stack, player, hand, world, pos, facing, hitX, hitY, hitZ, multipartBlock, state);
    }

    public boolean placeBlockAtTested(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing facing, float hitX,
                                      float hitY, float hitZ, IBlockState newState) {
        return player.canPlayerEdit(pos, facing, stack) && world.getBlockState(pos).getBlock().isReplaceable(world, pos)
                && block.canPlaceBlockAt(world, pos) && block.canPlaceBlockOnSide(world, pos, facing)
                && super.placeBlockAt(stack, player, world, pos, facing, hitX, hitY, hitZ, newState);
    }

    public static boolean placePartAt(ItemStack stack, EntityPlayer player, EnumHand hand, World world, BlockPos pos, EnumFacing facing,
                                      float hitX, float hitY, float hitZ, IMultipart multipartBlock, IBlockState state) {
        IPartSlot slot = multipartBlock.getSlotForPlacement(world, pos, state, facing, hitX, hitY, hitZ, player);
        if (!multipartBlock.canPlacePartAt(world, pos) || !multipartBlock.canPlacePartOnSide(world, pos, facing, slot))
            return false;

        if (MultipartHelper.addPart(world, pos, slot, state, false)) {
            if (!world.isRemote) {
                IPartInfo info = MultipartHelper.getContainer(world, pos).flatMap(c -> c.get(slot)).orElse(null);
                if (info != null) {
                    setMultipartTileNBT(player, stack, info);
                    multipartBlock.onPartPlacedBy(info, player, stack);
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean canPlaceBlockOnSide(World worldIn, BlockPos pos, EnumFacing side, EntityPlayer player, ItemStack stack) {
        return true;
    }

    public static boolean setMultipartTileNBT(EntityPlayer player, ItemStack stack, IPartInfo info) {
        World world = info.getActualWorld();
        BlockPos pos = info.getPartPos();

        MinecraftServer server = world.getMinecraftServer();

        if (server == null) {
            return false;
        } else {
            NBTTagCompound tag = stack.getSubCompound("BlockEntityTag");

            if (tag != null) {
                IMultipartTile tile = info.getTile();

                if (tile != null) {
                    if (!world.isRemote && tile.onlyOpsCanSetPartNbt() && (player == null || !player.canUseCommandBlock())) {
                        return false;
                    }

                    NBTTagCompound tag1 = tile.writePartToNBT(new NBTTagCompound());
                    NBTTagCompound tag2 = tag1.copy();
                    tag1.merge(tag);
                    tag1.setInteger("x", pos.getX());
                    tag1.setInteger("y", pos.getY());
                    tag1.setInteger("z", pos.getZ());

                    if (!tag1.equals(tag2)) {
                        tile.readPartFromNBT(tag1);
                        tile.markPartDirty();
                        return true;
                    }
                }
            }

            return false;
        }
    }

    public static interface IPartPlacementLogic {

        public boolean placePart(ItemStack stack, EntityPlayer player, EnumHand hand, World world, BlockPos pos, EnumFacing facing,
                                 float hitX, float hitY, float hitZ, IMultipart multipartBlock, IBlockState state);

    }

    public static interface IBlockPlacementLogic {

        public boolean place(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing facing, float hitX, float hitY,
                             float hitZ, IBlockState newState);

    }

    public static interface IBlockPlacementInfo {

        public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta,
                                                EntityLivingBase placer, EnumHand hand);

    }

    public static interface IExtendedBlockPlacementInfo {

        public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta,
                                                EntityLivingBase placer, EnumHand hand, IBlockState state);

    }

}
