package mcmultipart.client.multipart;

import java.util.HashMap;
import java.util.Map;

import mcmultipart.multipart.MultipartRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.statemap.DefaultStateMapper;
import net.minecraft.client.renderer.block.statemap.IStateMapper;
import net.minecraft.client.resources.model.ModelResourceLocation;

public class MultipartStateMapper extends DefaultStateMapper {

    @Override
    public Map<IBlockState, ModelResourceLocation> putStateModelLocations(Block blockIn) {

        Map<IBlockState, ModelResourceLocation> mappings = new HashMap<IBlockState, ModelResourceLocation>();
        mappings.put(blockIn.getDefaultState(), this.getModelResourceLocation(blockIn.getDefaultState()));

        for (String s : MultipartRegistry.defaultStates.keySet()) {
            IStateMapper mapper = MultipartRegistryClient.getSpecialMapper(s);
            if (mapper != null) {
                mappings.putAll(mapper.putStateModelLocations(blockIn));
            } else {
                BlockState state = MultipartRegistry.defaultStates.get(s);
                String modelPath = MultipartRegistry.stateLocations.get(state);
                for (IBlockState istate : state.getValidStates())
                    mappings.put(istate, new ModelResourceLocation(modelPath, this.getPropertyString(istate.getProperties())));
            }
        }
        return mappings;
    }
}
