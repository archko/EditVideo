package com.thuypham.ptithcm.editvideo.model

class FFprobeStream {
    var index: Int = 0
    var codec_name: String = ""
    var codec_long_name: String = ""
    var profile: Int = 0//": "1",
    var codec_type: String = ""//": "video",
    var codec_tag_string: String = ""//": "hev1",
    var codec_tag: String = ""//": "0x31766568",
    var width: Int = 0//": 540,
    var height: Int = 0//": 960,
    var coded_width: Int = 0//": 544,
    var coded_height: Int = 0//": 960,
    var closed_captions: Int = 0//": 0,
    var has_b_frames: Int = 0//": 2,
    var sample_aspect_ratio: String = ""//": "1:1",
    var display_aspect_ratio: String = ""//": "9:16",
    var pix_fmt: String = ""//": "yuv420p",
    var level: Int = 0//": 90,
    var color_range: String = ""//": "tv",
    var color_space: String = ""//": "bt709",
    var color_transfer: String = ""//": "bt709",
    var color_primaries: String = ""//": "bt709",
    var chroma_location: String = ""// ": "left",
    var field_order: String = ""//": "progressive",
    var refs: Int = 0//": 1,
    var id: String = ""//": "0x1",
    var r_frame_rate: String = ""//": "30/1",
    var avg_frame_rate: String = ""//": "30/1",
    var time_base: String = ""//": "1/30000",
    var start_pts: Int = 0//": 0,
    var start_time: String = ""//": "0.000000",
    var duration_ts: Int = 0//": 1773000,
    var duration: String = ""//": "59.100000",
    var bit_rate: String = ""//": "989473",
    var nb_frames: String = ""//": "1892",
    override fun toString(): String {
        return "FFprobeStream(width=$width, height=$height, r_frame_rate='$r_frame_rate', duration_ts=$duration_ts, duration='$duration', bit_rate='$bit_rate', nb_frames='$nb_frames')"
    }

}