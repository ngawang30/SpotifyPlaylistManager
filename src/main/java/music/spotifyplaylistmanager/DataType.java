package music.spotifyplaylistmanager;

public enum DataType {
    NUMBER(false, true,true),
    COVER(false, true,false),
    TRACK(true, true,true),
    ARTIST(true, true,true),
    RELEASE_DATE(false, false,true),
    DURATION(false, true,true),
    POPULARITY(false, false,true),
    EXPLICIT(true, false,true),
    ARTIST_TYPE(true, false,true),
    ARTIST_COUNTRY(true, false,true),
    ARTIST_GENDER(true, false,true),
    SUB_AREA(true, false,true),
    LANGUAGE(true, false,true),
    DEAD(true, false,true);

    private final boolean supportsFilter;
    private final boolean supportsSorting;
    private boolean visible;

    private DataType(boolean supportsFilter, boolean visible, boolean supportsSorting){
        this.supportsFilter = supportsFilter;
        this.supportsSorting = supportsSorting;
        this.visible = visible;
    }

    public boolean isSupportsFilter() {
        return supportsFilter;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isSupportsSorting() {
        return supportsSorting;
    }
}
