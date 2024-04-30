package com.archko.editvideo.ui.activity;

import android.content.Context;
import android.os.Handler;

import java.util.ArrayList;

import androidx.media3.common.util.UnstableApi;
import androidx.media3.decoder.ffmpeg.FfmpegAudioRenderer;
import androidx.media3.exoplayer.DefaultRenderersFactory;
import androidx.media3.exoplayer.Renderer;
import androidx.media3.exoplayer.audio.AudioRendererEventListener;
import androidx.media3.exoplayer.audio.AudioSink;
import androidx.media3.exoplayer.mediacodec.MediaCodecSelector;

/**
 * @author: archko 2023/6/21 :14:13
 */
@UnstableApi
public class FfmpegRenderersFactory extends DefaultRenderersFactory {

    public FfmpegRenderersFactory(Context context) {
        super(context);
        setExtensionRendererMode(EXTENSION_RENDERER_MODE_PREFER);
    }

    @Override
    protected void buildAudioRenderers(Context context,
                                       int extensionRendererMode,
                                       MediaCodecSelector mediaCodecSelector,
                                       boolean enableDecoderFallback,
                                       AudioSink audioSink, Handler eventHandler,
                                       AudioRendererEventListener eventListener,
                                       ArrayList<Renderer> out) {
        out.add(new FfmpegAudioRenderer());
        super.buildAudioRenderers(context, extensionRendererMode, mediaCodecSelector, enableDecoderFallback, audioSink, eventHandler, eventListener, out);

    }
}

