package mcmultipart.client.multipart;

import java.util.HashMap;
import java.util.Map;

import mcmultipart.multipart.MultipartRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.statemap.DefaultStateMapper;
import net.minecraft.client.renderer.block.statemap.IStateMapper;
import net.minecraft.client.resources.model.ModelResourceLocation;

public class MultipartStateMapper extends DefaultStateMapper {

    public static MultipartStateMapper instance = new MultipartStateMapper();

    private boolean replaceNormal = true;

    @Override
    public Map<IBlockState, ModelResourceLocation> putStateModelLocations(Block blockIn) {

        Map<IBlockState, ModelResourceLocation> mappings = new HashMap<IBlockState, ModelResourceLocation>();
        replaceNormal = false;
        mappings.put(blockIn.getDefaultState(), this.getModelResourceLocation(blockIn.getDefaultState()));
        replaceNormal = true;

        for (String part : MultipartRegistry.defaultStates.keySet()) {
            IStateMapper mapper = MultipartRegistryClient.getSpecialPartStateMapper(part);
            if (mapper != null) {
                mappings.putAll(mapper.putStateModelLocations(blockIn));
            } else {
                BlockState state = MultipartRegistry.defaultStates.get(part);
                String modelPath = MultipartRegistry.stateLocations.get(state);
                for (IBlockState istate : state.getValidStates())
                    mappings.put(istate, new ModelResourceLocation(modelPath, this.getPropertyString(istate.getProperties())));
            }
        }
        return mappings;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public String getPropertyString(Map<IProperty, Comparable> p_178131_1_) {

        String str = super.getPropertyString(p_178131_1_);
        if (replaceNormal && str.equals("normal")) return "multipart";
        return str;
    }

}
