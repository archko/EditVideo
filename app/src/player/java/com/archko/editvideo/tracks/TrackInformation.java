package com.archko.editvideo.tracks;

import androidx.media3.common.Tracks;

/**
 * 渲染器实体类,trackIndex:音频=1,视频=2,字幕=3,等等,可以从exoplayer里面查到
 * trackGroupIndex是真实的具体渲染器里面的哪一个轨道.
 */
public class TrackInformation {
    public static int TYPE_DOWNLOAD_TRACK = -3;

    //轨道类型,如果-2:选择轨道,-1:关闭轨道
    public static int TYPE_SELECT_TRACK = -2;
    public static int TYPE_NO_TRACK = -1;
    public static int TYPE_TRACK = -0;

    public int type = TYPE_TRACK;
    /**
     * 这个是渲染器索引,音频,字幕都是渲染器,当前不用它
     */
    public int trackIndex;
    public String trackName;
    /**
     * 这个是音频的索引,外部传来的值trackId应该是对应这个值的
     */
    public int trackGroupIndex;
    public Tracks.Group trackGroup;

    public boolean isSelected;
    //vlc trackid
    public String id;

    //这是exo用的
    public TrackInformation(Tracks tracks, int trackGroupIndex, int trackIndex, String trackName, int type) {
        this.trackIndex = trackIndex;
        this.trackName = trackName;
        this.trackGroupIndex = trackGroupIndex;
        this.type = type;
        if (null != tracks) {
            trackGroup = tracks.getGroups().get(trackGroupIndex);
            isSelected = trackGroup.isSelected();   //初始化时需要设置一下.
        }
    }

    //这是vlc用的
    public TrackInformation(String id, int type, int trackIndex, String trackName, boolean isSelected) {
        this.id = id;
        this.type = type;
        this.trackIndex = trackIndex;
        this.trackName = trackName;
        this.isSelected = isSelected;
    }

    @Override
    public String toString() {
        return "Track{" +
                "id=" + id +
                "type=" + type +
                "trackGroupIndex=" + trackGroupIndex +
                ", trackName='" + trackName + '\'' +
                ", trackIndex=" + trackIndex +
                ", trackGroup=" + (trackGroup) +
                ", isSelected=" + (isSelected) +
                '}';
    }
}