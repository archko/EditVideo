package com.archko.editvideo.tracks

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.TrackGroup
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.TrackSelectionParameters
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.TrackNameProvider
import com.thuypham.ptithcm.editvideo.R
import timber.log.Timber

/**
 * @author: archko 2023/8/19 :14:35
 */
@OptIn(UnstableApi::class)
class ExoTracksViewHolder(
    var mExoPlayer: ExoPlayer?,
    ctx: Context,
    trackType: Int,
    title: String,
    var trackNameProvider: TrackNameProvider,
    selectTrackListener: TracksAdapter.SelectTrackListener?
) : BaseTracksViewHolder(ctx, trackType, title, selectTrackListener) {

    override fun getAllTracks(): ArrayList<TrackInformation> {
        val alltrakcs = arrayListOf<TrackInformation>()
        val tracks = mExoPlayer!!.currentTracks
        val supportInformations: List<TrackInformation> =
            gatherSupportedTrackInfosOfType(tracks, trackType)
        if (supportInformations != null && supportInformations.isNotEmpty()) {
            val postfix = if (C.TRACK_TYPE_AUDIO == trackType) {
                getString(R.string.player_track_track_audio)
            } else {
                getString(R.string.player_track_track_subtitle)
            }
            /*if (C.TRACK_TYPE_TEXT == trackType) {
                var selectTrack =
                    TrackInformation(
                        null, 0, 0,
                        String.format(
                            "%s%s",
                            getString(R.string.player_track_track_select),
                            postfix
                        ),
                        TrackInformation.TYPE_SELECT_TRACK
                    )
                alltrakcs.add(selectTrack)
                selectTrack =
                    TrackInformation(
                        null, 0, 0,
                        getString(R.string.player_track_subtitle_download_title),
                        TrackInformation.TYPE_DOWNLOAD_TRACK
                    )
                alltrakcs.add(selectTrack)
            }*/
            val closeTrack =
                TrackInformation(
                    null, 0, 0,
                    String.format("%s%s", getString(R.string.player_track_track_close), postfix),
                    TrackInformation.TYPE_NO_TRACK
                )
            alltrakcs.add(closeTrack)
            alltrakcs.addAll(supportInformations)
        } else {
            if (C.TRACK_TYPE_TEXT == trackType) {
                /*var selectTrack = TrackInformation(
                    null,
                    0,
                    0,
                    getString(R.string.player_track_track_load_subtitle),
                    TrackInformation.TYPE_SELECT_TRACK
                )
                alltrakcs.add(selectTrack)
                selectTrack =
                    TrackInformation(
                        null, 0, 0,
                        getString(R.string.player_track_subtitle_download_title),
                        TrackInformation.TYPE_DOWNLOAD_TRACK
                    )
                alltrakcs.add(selectTrack)*/
            }
        }

        return alltrakcs
    }

    private fun gatherSupportedTrackInfosOfType(
        tracks: Tracks,
        tType: Int
    ): List<TrackInformation> {
        val trackInfos: MutableList<TrackInformation> = ArrayList()
        val trackGroups: List<Tracks.Group> = tracks.groups
        for (trackGroupIndex in trackGroups.indices) {
            val trackGroup = trackGroups[trackGroupIndex]
            if (trackGroup.type != tType) {
                continue
            }
            for (trackIndex in 0 until trackGroup.length) {
                if (!trackGroup.isTrackSupported(trackIndex)) {
                    continue
                }
                val trackFormat = trackGroup.getTrackFormat(trackIndex)
                if (trackFormat.selectionFlags and C.SELECTION_FLAG_FORCED != 0) {
                    continue
                }
                var trackName: String? = trackNameProvider.getTrackName(trackFormat)
                if (C.TRACK_TYPE_AUDIO == this.trackType) {
                    trackName += " " + trackFormat.sampleMimeType
                }
                val trackInformation = TrackInformation(
                    tracks,
                    trackGroupIndex,
                    trackIndex,
                    trackName,
                    TrackInformation.TYPE_TRACK
                )
                Timber.d(
                    String.format(
                        "add tracks.trackFormat:%s, %s",
                        trackFormat,
                        trackInformation
                    )
                )
                trackInfos.add(trackInformation)
            }
        }
        return trackInfos
    }

    fun initTracksIfNeeded(mExoPlayer: ExoPlayer?, trackType: Int) {
        this.mExoPlayer = mExoPlayer
        if (mExoPlayer == null || !mExoPlayer.isCommandAvailable(Player.COMMAND_GET_TRACKS)
            || !mExoPlayer.isCommandAvailable(Player.COMMAND_SET_TRACK_SELECTION_PARAMETERS)
            || tracksAdapter == null
        ) {
            return
        }
        if (tracksAdapter!!.itemCount == 0) {
            tracksAdapter?.trackInformations = getAllTracks()
        }
        tracksAdapter!!.notifyDataSetChanged()
    }

    override fun setTrack(
        trackType: Int,
        index: Int,
        trackInformations: List<TrackInformation>
    ) {
        setTrack(mExoPlayer, trackType, index, trackInformations)
    }

    companion object {
        fun setTrack(
            mExoPlayer: ExoPlayer?,
            trackType: Int,
            index: Int,
            trackInformations: List<TrackInformation>
        ) {
            val trackInformation: TrackInformation = trackInformations[index]
            if (trackInformation != null) {
                if (TrackInformation.TYPE_SELECT_TRACK == trackInformation.type) {
                    return
                }
                if (TrackInformation.TYPE_NO_TRACK == trackInformation.type) {
                    val trackSelectionParameters: TrackSelectionParameters =
                        mExoPlayer!!.trackSelectionParameters
                    Util.castNonNull<Player>(mExoPlayer).trackSelectionParameters =
                        trackSelectionParameters
                            .buildUpon()
                            .clearOverridesOfType(trackType)
                            .setTrackTypeDisabled(trackType, true)
                            .build()
                    updateSelection(index, trackInformations)
                    return
                }
                val mediaTrackGroup = trackInformation.trackGroup.mediaTrackGroup
                val trackSelectionParameters = mExoPlayer?.trackSelectionParameters
                printGroup(trackInformation, trackSelectionParameters)
                if (mediaTrackGroup != null && null != trackSelectionParameters) {
                    val indexs: MutableList<Int> = ArrayList()
                    indexs.add(trackInformation.trackIndex)
                    Timber.d(
                        String.format(
                            "isSelected getType:%s,trackIndex:%s",
                            trackInformation.trackGroup.type,
                            trackInformation.trackIndex
                        )
                    )
                    val newParams = trackSelectionParameters
                        .buildUpon()
                        .setOverrideForType(TrackSelectionOverride(mediaTrackGroup, indexs))
                        .setTrackTypeDisabled(
                            trackInformation.trackGroup.type,  /* disabled= */
                            false
                        )
                        .build()
                    mExoPlayer?.trackSelectionParameters = newParams
                    updateSelection(index, trackInformations)
                }
            }
        }

        private fun updateSelection(selectedIndex: Int, trackInformations: List<TrackInformation>) {
            for (index in trackInformations.indices) {
                trackInformations[index].isSelected = selectedIndex == index
            }
        }

        fun printGroup(
            track: TrackInformation?,
            trackSelectionParameters: TrackSelectionParameters?
        ) {
            if (null == trackSelectionParameters) {
                return
            }
            Timber.d(String.format("settracks:%s", track))
            val ov: Map<TrackGroup?, TrackSelectionOverride> = trackSelectionParameters.overrides
            if (ov.isNotEmpty()) {
                for ((_, value) in ov) {
                    val mediaTrackGroup = value.mediaTrackGroup
                    Timber.d(
                        String.format(
                            "settracks.id:%s,type:%s,format:%s",
                            mediaTrackGroup.id,
                            mediaTrackGroup.type,
                            mediaTrackGroup.getFormat(0)
                        )
                    )
                }
            }
        }

        fun printTracks(mExoPlayer: ExoPlayer) {
            val currentTracks = mExoPlayer.currentTracks
            val groups: List<Tracks.Group> = currentTracks.groups
            for (trackGroup in groups) {
                // Group level information.
                val trackType: @C.TrackType Int = trackGroup.type
                val trackInGroupIsSelected = trackGroup.isSelected
                val trackInGroupIsSupported = trackGroup.isSupported
                Timber.d(
                    String.format(
                        "trackType:%s,trackInGroupIsSelected:%s",
                        trackType,
                        trackInGroupIsSelected
                    )
                )
                /*for (i in 0 until trackGroup.length) {
                    // Individual track information.
                    val isSupported = trackGroup.isTrackSupported(i)
                    val isSelected = trackGroup.isTrackSelected(i)
                    val trackFormat = trackGroup.getTrackFormat(i)
                    if (isSelected) {
                        Timber.d(String.format("trackGroup.isSupported:%s, isSelected:%s, trackFormat:%s", isSupported, isSelected, trackFormat))
                    }
                }*/
            }
        }

        fun selectTrack(subtitleId: String?, mExoPlayer: ExoPlayer?, tracks: Tracks) {
            val trackGroups = tracks.groups
            for (groupIndex in trackGroups.indices) {
                val trackGroup = trackGroups[groupIndex]
                for (trackIndex in 0 until trackGroup.length) {
                    val format = trackGroup.getTrackFormat(trackIndex)
                    if (format.id == subtitleId) {
                        val indexs: MutableList<Int> = ArrayList()
                        indexs.add(0)
                        val trackSelectionParameters: TrackSelectionParameters =
                            mExoPlayer!!.trackSelectionParameters
                        Util.castNonNull<Player>(mExoPlayer).trackSelectionParameters =
                            trackSelectionParameters
                                .buildUpon()
                                .setOverrideForType(
                                    TrackSelectionOverride(
                                        trackGroup.mediaTrackGroup,
                                        indexs
                                    )
                                )
                                .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, false)
                                .build()
                        Timber.d("selectTrack:$subtitleId, $trackGroup")
                        break
                    }
                }
            }
        }
    }
}