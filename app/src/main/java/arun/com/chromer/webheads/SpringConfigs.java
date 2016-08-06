package arun.com.chromer.webheads;

import com.facebook.rebound.SpringConfig;

/**
 * Created by Arun on 06/08/2016.
 */

public class SpringConfigs {
    private SpringConfigs() {
        throw new AssertionError("no instances");
    }

    public static final SpringConfig FLING = SpringConfig.fromOrigamiTensionAndFriction(50, 5);
    public static final SpringConfig DRAG = SpringConfig.fromOrigamiTensionAndFriction(0, 1.8);
    public static final SpringConfig SNAP = SpringConfig.fromOrigamiTensionAndFriction(100, 7);
    public static final SpringConfig ATTACHMENT = SpringConfig.fromOrigamiTensionAndFriction(50, 10);
}
