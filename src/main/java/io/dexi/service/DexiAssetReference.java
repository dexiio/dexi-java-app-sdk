package io.dexi.service;

/**
 * Defines a reference to a dexi asset such as a robot, dataset etc.
 *
 * You can define assets as part of your configuration for an app which will give you
 * access to interact with those assets as part of the integration - using the Dexi API.
 */
public class DexiAssetReference {

    /**
     * The id of the asset itself - e.g. the robot id
     */
    private String id;

    /**
     * The type of the asset.
     */
    private String type;

    /**
     * The absolute path in the users project view to the asset
     */
    private String pathName;

    /**
     * The name of the asset
     */
    private String name;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPathName() {
        return pathName;
    }

    public void setPathName(String pathName) {
        this.pathName = pathName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
