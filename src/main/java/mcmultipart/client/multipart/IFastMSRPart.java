package mcmultipart.client.multipart;

import mcmultipart.multipart.IMultipart;

/**
 * Interface that adds {@link FastMSR} support to {@link IMultipart}.<br/>
 */
public interface IFastMSRPart extends IMultipart {

    /**
     * Checks whether or not this part currently needs a {@link FastMSR} or it should just use a {@link MultipartSpecialRenderer}.
     */
    public boolean hasFastRenderer();

}
