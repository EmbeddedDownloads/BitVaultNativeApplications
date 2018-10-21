package net.sourceforge.opencamera;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.ActivityInfo;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import net.sourceforge.opencamera.Preview.Preview;

public class MyPreferenceFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {
	private static final String TAG = "MyPreferenceFragment";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		if( MyDebug.LOG )
			Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);



		//For PreferencesCategory Title
/*

		PreferenceCategory cameraEffects=(PreferenceCategory)this.findPreference("preference_category_camera_effects");
		cameraEffects.setTitle(Html.fromHtml("<font color='#876e3a'>"+getResources().getString(R.string.preference_category_camera_effects)+"</font>"));

		PreferenceCategory cameraControls=(PreferenceCategory)this.findPreference("preference_category_camera_controls");
		cameraControls.setTitle(Html.fromHtml("<font color='#876e3a'>"+getResources().getString(R.string.preference_category_camera_controls)+"</font>"));

		PreferenceCategory photoVideoSettings =(PreferenceCategory)this.findPreference("preference_category_camera_quality");
		photoVideoSettings.setTitle(Html.fromHtml("<font color='#876e3a'>"+getResources().getString(R.string.preference_category_camera_quality)+"</font>"));

		PreferenceCategory misc=(PreferenceCategory)this.findPreference("preference_category_online");
		misc.setTitle(Html.fromHtml("<font color='#876e3a'>"+getResources().getString(R.string.preference_category_online)+"</font>"));

		ListPreference lockPhotoVideoOrientation =(ListPreference)this.findPreference("preference_lock_orientation");
		lockPhotoVideoOrientation.setTitle(Html.fromHtml("<font color='#3c4143'>"+getResources().getString(R.string.preference_lock_orientation)+"</font>"));
		lockPhotoVideoOrientation.setSummary(Html.fromHtml("<font color='#676767'>"+getResources().getString(R.string.preference_lock_orientation_summary)+"</font>"));

		CheckBoxPreference faceDetection=(CheckBoxPreference)this.findPreference("preference_face_detection");
		faceDetection.setTitle(Html.fromHtml("<font color='#3c4143'>"+getResources().getString(R.string.preference_face_detection)+"</font>"));

		faceDetection.setSummary(Html.fromHtml("<font color='#676767'>"+getResources().getString(R.string.preference_face_detection_summary)+"</font>"));

		ListPreference timer =(ListPreference)this.findPreference("preference_timer");
		timer.setTitle(Html.fromHtml("<font color='#3c4143'>"+getResources().getString(R.string.preference_timer)+"</font>"));
		timer.setSummary(Html.fromHtml("<font color='#676767'>"+getResources().getString(R.string.preference_timer_summary)+"</font>"));

		ListPreference burstMode =(ListPreference)this.findPreference("preference_burst_mode");
		burstMode.setTitle(Html.fromHtml("<font color='#3c4143'>"+getResources().getString(R.string.preference_burst_mode)+"</font>"));
		burstMode.setSummary(Html.fromHtml("<font color='#676767'>"+getResources().getString(R.string.preference_burst_mode_summary)+"</font>"));


		ListPreference burstModeInterval =(ListPreference)this.findPreference("preference_burst_interval");
		burstModeInterval.setTitle(Html.fromHtml("<font color='#3c4143'>"+getResources().getString(R.string.preference_burst_interval)+"</font>"));
		burstModeInterval.setSummary(Html.fromHtml("<font color='#676767'>"+getResources().getString(R.string.preference_burst_interval_summary)+"</font>"));

		PreferenceScreen moreCameraControls=(PreferenceScreen)this.findPreference("preference_screen_camera_controls_more");
		moreCameraControls.setTitle(Html.fromHtml("<font color='#3c4143'>"+getResources().getString(R.string.preference_screen_camera_controls_more)+"</font>"));

		PreferenceScreen onScreenGui=(PreferenceScreen)this.findPreference("preference_screen_gui");
		onScreenGui.setTitle(Html.fromHtml("<font color='#3c4143'>"+getResources().getString(R.string.preference_screen_gui)+"</font>"));

		PreferenceScreen photoSettings=(PreferenceScreen)this.findPreference("preference_screen_photo_settings");
		photoSettings.setTitle(Html.fromHtml("<font color='#3c4143'>"+getResources().getString(R.string.preference_screen_photo_settings)+"</font>"));

		PreferenceScreen videoSettings=(PreferenceScreen)this.findPreference("preference_screen_video_settings");
		videoSettings.setTitle(Html.fromHtml("<font color='#3c4143'>"+getResources().getString(R.string.preference_screen_video_settings)+"</font>"));

		PreferenceScreen locationSettings=(PreferenceScreen)this.findPreference("preference_screen_location_settings");
		locationSettings.setTitle(Html.fromHtml("<font color='#3c4143'>"+getResources().getString(R.string.preference_screen_location_settings)+"</font>"));

		Preference about=(Preference)this.findPreference("preference_about");
		about.setTitle(Html.fromHtml("<font color='#3c4143'>"+getResources().getString(R.string.preference_about)+"</font>"));
		about.setSummary(Html.fromHtml("<font color='#676767'>"+getResources().getString(R.string.preference_about_summary)+"</font>"));

		Preference reset=(Preference)this.findPreference("preference_reset");
		reset.setTitle(Html.fromHtml("<font color='#3c4143'>"+getResources().getString(R.string.preference_reset)+"</font>"));
		reset.setSummary(Html.fromHtml("<font color='#676767'>"+getResources().getString(R.string.preference_reset_summary)+"</font>"));

		ListPreference touch_capture=(ListPreference)this.findPreference("preference_touch_capture");
		touch_capture.setTitle(Html.fromHtml("<font color='#3c4143'>"+getResources().getString(R.string.preference_touch_capture)+"</font>"));
		touch_capture.setSummary(Html.fromHtml("<font color='#676767'>"+getResources().getString(R.string.preference_touch_capture_summary)+"</font>"));

		CheckBoxPreference pause_preview=(CheckBoxPreference)this.findPreference("preference_pause_preview");
		pause_preview.setTitle(Html.fromHtml("<font color='#3c4143'>"+getResources().getString(R.string.preference_pause_preview)+"</font>"));
		pause_preview.setSummary(Html.fromHtml("<font color='#676767'>"+getResources().getString(R.string.preference_pause_preview_summary)+"</font>"));

		CheckBoxPreference shutter_sound=(CheckBoxPreference)this.findPreference("preference_shutter_sound");
		shutter_sound.setTitle(Html.fromHtml("<font color='#3c4143'>"+getResources().getString(R.string.preference_shutter_sound)+"</font>"));
		shutter_sound.setSummary(Html.fromHtml("<font color='#676767'>"+getResources().getString(R.string.preference_shutter_sound_summary)+"</font>"));

		CheckBoxPreference timer_beep=(CheckBoxPreference)this.findPreference("preference_timer_beep");
		timer_beep.setTitle(Html.fromHtml("<font color='#3c4143'>"+getResources().getString(R.string.preference_timer_beep)+"</font>"));
		timer_beep.setSummary(Html.fromHtml("<font color='#676767'>"+getResources().getString(R.string.preference_timer_beep_summary)+"</font>"));

		CheckBoxPreference timer_speak=(CheckBoxPreference)this.findPreference("preference_timer_speak");
		timer_speak.setTitle(Html.fromHtml("<font color='#3c4143'>"+getResources().getString(R.string.preference_timer_speak)+"</font>"));
		timer_speak.setSummary(Html.fromHtml("<font color='#676767'>"+getResources().getString(R.string.preference_timer_speak_summary)+"</font>"));

		ListPreference volume_keys=(ListPreference)this.findPreference("preference_volume_keys");
		volume_keys.setTitle(Html.fromHtml("<font color='#3c4143'>"+getResources().getString(R.string.preference_volume_keys)+"</font>"));
		volume_keys.setSummary(Html.fromHtml("<font color='#676767'>"+getResources().getString(R.string.preference_volume_keys_summary)+"</font>"));

		Preference save_location=(Preference)this.findPreference("preference_save_location");
		save_location.setTitle(Html.fromHtml("<font color='#3c4143'>"+getResources().getString(R.string.preference_save_location)+"</font>"));
		save_location.setSummary(Html.fromHtml("<font color='#676767'>"+getResources().getString(R.string.preference_save_location_summary)+"</font>"));

		CheckBoxPreference using_saf=(CheckBoxPreference)this.findPreference("preference_using_saf");
		using_saf.setTitle(Html.fromHtml("<font color='#3c4143'>"+getResources().getString(R.string.preference_using_saf)+"</font>"));
		using_saf.setSummary(Html.fromHtml("<font color='#676767'>"+getResources().getString(R.string.preference_using_saf_summary)+"</font>"));

		EditTextPreference save_photo_prefix=(EditTextPreference)this.findPreference("preference_save_photo_prefix");
		save_photo_prefix.setTitle(Html.fromHtml("<font color='#3c4143'>"+getResources().getString(R.string.preference_save_photo_prefix)+"</font>"));
		save_photo_prefix.setSummary(Html.fromHtml("<font color='#676767'>"+getResources().getString(R.string.preference_save_photo_prefix_summary)+"</font>"));
		*/
/*save_photo_prefix.getDialog().setTitle(Html.fromHtml("<font color='#3c4143'>"+getResources().getString(R.string.preference_save_photo_prefix)+"</font>"));*//*


		EditTextPreference save_video_prefix=(EditTextPreference)this.findPreference("preference_save_video_prefix");
		save_video_prefix.setTitle(Html.fromHtml("<font color='#3c4143'>"+getResources().getString(R.string.preference_save_video_prefix)+"</font>"));
		save_video_prefix.setSummary(Html.fromHtml("<font color='#676767'>"+getResources().getString(R.string.preference_save_video_prefix_summary)+"</font>"));
		*/
/*save_video_prefix.getDialog().setTitle(Html.fromHtml("<font color='#3c4143'>"+getResources().getString(R.string.preference_save_video_prefix)+"</font>"));*//*


		CheckBoxPreference show_when_locked=(CheckBoxPreference)this.findPreference("preference_show_when_locked");
		show_when_locked.setTitle(Html.fromHtml("<font color='#3c4143'>"+getResources().getString(R.string.preference_show_when_locked)+"</font>"));
		show_when_locked.setSummary(Html.fromHtml("<font color='#676767'>"+getResources().getString(R.string.preference_show_when_locked_summary)+"</font>"));

		CheckBoxPreference lock_video=(CheckBoxPreference)this.findPreference("preference_lock_video");
		lock_video.setTitle(Html.fromHtml("<font color='#3c4143'>"+getResources().getString(R.string.preference_lock_video)+"</font>"));
		lock_video.setSummary(Html.fromHtml("<font color='#676767'>"+getResources().getString(R.string.preference_lock_video_summary)+"</font>"));

		ListPreference rotate_preview=(ListPreference)this.findPreference("preference_rotate_preview");
		rotate_preview.setTitle(Html.fromHtml("<font color='#3c4143'>"+getResources().getString(R.string.preference_rotate_preview)+"</font>"));
		rotate_preview.setSummary(Html.fromHtml("<font color='#676767'>"+getResources().getString(R.string.preference_rotate_preview_summary)+"</font>"));

		ListPreference preview_size=(ListPreference)this.findPreference("preference_preview_size");
		preview_size.setTitle(Html.fromHtml("<font color='#3c4143'>"+getResources().getString(R.string.preference_preview_size)+"</font>"));
		preview_size.setSummary(Html.fromHtml("<font color='#676767'>"+getResources().getString(R.string.preference_preview_size_summary)+"</font>"));

		ListPreference ui_placement=(ListPreference)this.findPreference("preference_ui_placement");
		ui_placement.setTitle(Html.fromHtml("<font color='#3c4143'>"+getResources().getString(R.string.preference_ui_placement)+"</font>"));
		ui_placement.setSummary(Html.fromHtml("<font color='#676767'>"+getResources().getString(R.string.preference_ui_placement_summary)+"</font>"));

		ListPreference immersive_mode=(ListPreference)this.findPreference("preference_immersive_mode");
		immersive_mode.setTitle(Html.fromHtml("<font color='#3c4143'>"+getResources().getString(R.string.preference_immersive_mode)+"</font>"));
		immersive_mode.setSummary(Html.fromHtml("<font color='#676767'>"+getResources().getString(R.string.preference_immersive_mode_summary)+"</font>"));

		CheckBoxPreference show_zoom=(CheckBoxPreference)this.findPreference("preference_show_zoom");
		show_zoom.setTitle(Html.fromHtml("<font color='#3c4143'>"+getResources().getString(R.string.preference_show_zoom)+"</font>"));
		show_zoom.setSummary(Html.fromHtml("<font color='#676767'>"+getResources().getString(R.string.preference_show_zoom_summary)+"</font>"));

		CheckBoxPreference show_zoom_controls=(CheckBoxPreference)this.findPreference("preference_show_zoom_controls");
		show_zoom_controls.setTitle(Html.fromHtml("<font color='#3c4143'>"+getResources().getString(R.string.preference_show_zoom_controls)+"</font>"));
		show_zoom_controls.setSummary(Html.fromHtml("<font color='#676767'>"+getResources().getString(R.string.preference_show_zoom_controls_summary)+"</font>"));

		CheckBoxPreference show_iso=(CheckBoxPreference)this.findPreference("preference_show_iso");
		show_iso.setTitle(Html.fromHtml("<font color='#3c4143'>"+getResources().getString(R.string.preference_show_iso)+"</font>"));
		show_iso.setSummary(Html.fromHtml("<font color='#676767'>"+getResources().getString(R.string.preference_show_iso_summary)+"</font>"));

		CheckBoxPreference free_memory=(CheckBoxPreference)this.findPreference("preference_free_memory");
		free_memory.setTitle(Html.fromHtml("<font color='#3c4143'>"+getResources().getString(R.string.preference_free_memory)+"</font>"));
		free_memory.setSummary(Html.fromHtml("<font color='#676767'>"+getResources().getString(R.string.preference_free_memory_summary)+"</font>"));

		CheckBoxPreference show_angle=(CheckBoxPreference)this.findPreference("preference_show_angle");
		show_angle.setTitle(Html.fromHtml("<font color='#3c4143'>"+getResources().getString(R.string.preference_show_angle)+"</font>"));
		show_angle.setSummary(Html.fromHtml("<font color='#676767'>"+getResources().getString(R.string.preference_show_angle_summary)+"</font>"));

		CheckBoxPreference show_angle_line=(CheckBoxPreference)this.findPreference("preference_show_angle_line");
		show_angle_line.setTitle(Html.fromHtml("<font color='#3c4143'>"+getResources().getString(R.string.preference_show_angle_line)+"</font>"));
		show_angle_line.setSummary(Html.fromHtml("<font color='#676767'>"+getResources().getString(R.string.preference_show_angle_line_summary)+"</font>"));

		ListPreference angle_highlight_color=(ListPreference)this.findPreference("preference_angle_highlight_color");
		angle_highlight_color.setTitle(Html.fromHtml("<font color='#3c4143'>"+getResources().getString(R.string.preference_angle_highlight_color)+"</font>"));
		angle_highlight_color.setSummary(Html.fromHtml("<font color='#676767'>"+getResources().getString(R.string.preference_angle_highlight_color_summary)+"</font>"));

		CheckBoxPreference show_geo_direction=(CheckBoxPreference)this.findPreference("preference_show_geo_direction");
		show_geo_direction.setTitle(Html.fromHtml("<font color='#3c4143'>"+getResources().getString(R.string.preference_show_geo_direction)+"</font>"));
		show_geo_direction.setSummary(Html.fromHtml("<font color='#676767'>"+getResources().getString(R.string.preference_show_geo_direction_summary)+"</font>"));

		CheckBoxPreference show_time=(CheckBoxPreference)this.findPreference("preference_show_time");
		show_time.setTitle(Html.fromHtml("<font color='#3c4143'>"+getResources().getString(R.string.preference_show_time)+"</font>"));
		show_time.setSummary(Html.fromHtml("<font color='#676767'>"+getResources().getString(R.string.preference_show_time_summary)+"</font>"));

		CheckBoxPreference show_battery=(CheckBoxPreference)this.findPreference("preference_show_battery");
		show_battery.setTitle(Html.fromHtml("<font color='#3c4143'>"+getResources().getString(R.string.preference_show_battery)+"</font>"));
		show_battery.setSummary(Html.fromHtml("<font color='#676767'>"+getResources().getString(R.string.preference_show_battery_summary)+"</font>"));

		ListPreference grid=(ListPreference)this.findPreference("preference_grid");
		grid.setTitle(Html.fromHtml("<font color='#3c4143'>"+getResources().getString(R.string.preference_grid)+"</font>"));
		grid.setSummary(Html.fromHtml("<font color='#676767'>"+getResources().getString(R.string.preference_grid_summary)+"</font>"));

		ListPreference crop_guide=(ListPreference)this.findPreference("preference_crop_guide");
		crop_guide.setTitle(Html.fromHtml("<font color='#3c4143'>"+getResources().getString(R.string.preference_crop_guide)+"</font>"));
		crop_guide.setSummary(Html.fromHtml("<font color='#676767'>"+getResources().getString(R.string.preference_crop_guide_summary)+"</font>"));

		CheckBoxPreference show_toasts=(CheckBoxPreference)this.findPreference("preference_show_toasts");
		show_toasts.setTitle(Html.fromHtml("<font color='#3c4143'>"+getResources().getString(R.string.preference_show_toasts)+"</font>"));
		show_toasts.setSummary(Html.fromHtml("<font color='#676767'>"+getResources().getString(R.string.preference_show_toasts_summary)+"</font>"));

		CheckBoxPreference thumbnail_animation=(CheckBoxPreference)this.findPreference("preference_thumbnail_animation");
		thumbnail_animation.setTitle(Html.fromHtml("<font color='#3c4143'>"+getResources().getString(R.string.preference_thumbnail_animation)+"</font>"));
		thumbnail_animation.setSummary(Html.fromHtml("<font color='#676767'>"+getResources().getString(R.string.preference_thumbnail_animation_summary)+"</font>"));

		CheckBoxPreference keep_display_on=(CheckBoxPreference)this.findPreference("preference_keep_display_on");
		keep_display_on.setTitle(Html.fromHtml("<font color='#3c4143'>"+getResources().getString(R.string.preference_keep_display_on)+"</font>"));
		keep_display_on.setSummary(Html.fromHtml("<font color='#676767'>"+getResources().getString(R.string.preference_keep_display_on_summary)+"</font>"));

		CheckBoxPreference max_brightness=(CheckBoxPreference)this.findPreference("preference_max_brightness");
		max_brightness.setTitle(Html.fromHtml("<font color='#3c4143'>"+getResources().getString(R.string.preference_max_brightness)+"</font>"));
		max_brightness.setSummary(Html.fromHtml("<font color='#676767'>"+getResources().getString(R.string.preference_max_brightness_summary)+"</font>"));

		ListPreference resolution=(ListPreference)this.findPreference("preference_resolution");
		resolution.setTitle(Html.fromHtml("<font color='#3c4143'>"+getResources().getString(R.string.preference_resolution)+"</font>"));
		resolution.setSummary(Html.fromHtml("<font color='#676767'>"+getResources().getString(R.string.preference_resolution_summary)+"</font>"));

		ListPreference quality=(ListPreference)this.findPreference("preference_quality");
		quality.setTitle(Html.fromHtml("<font color='#3c4143'>"+getResources().getString(R.string.preference_quality)+"</font>"));
		quality.setSummary(Html.fromHtml("<font color='#676767'>"+getResources().getString(R.string.preference_quality_summary)+"</font>"));

		ListPreference stamp=(ListPreference)this.findPreference("preference_stamp");
		stamp.setTitle(Html.fromHtml("<font color='#3c4143'>"+getResources().getString(R.string.preference_stamp)+"</font>"));
		stamp.setSummary(Html.fromHtml("<font color='#676767'>"+getResources().getString(R.string.preference_stamp_summary)+"</font>"));

		ListPreference stamp_dateformat=(ListPreference)this.findPreference("preference_stamp_dateformat");
		stamp_dateformat.setTitle(Html.fromHtml("<font color='#3c4143'>"+getResources().getString(R.string.preference_stamp_dateformat)+"</font>"));
		stamp_dateformat.setSummary(Html.fromHtml("<font color='#676767'>"+getResources().getString(R.string.preference_stamp_dateformat_summary)+"</font>"));

		ListPreference stamp_timeformat=(ListPreference)this.findPreference("preference_stamp_timeformat");
		stamp_timeformat.setTitle(Html.fromHtml("<font color='#3c4143'>"+getResources().getString(R.string.preference_stamp_timeformat)+"</font>"));
		stamp_timeformat.setSummary(Html.fromHtml("<font color='#676767'>"+getResources().getString(R.string.preference_stamp_timeformat_summary)+"</font>"));

		ListPreference stamp_gpsformat=(ListPreference)this.findPreference("preference_stamp_gpsformat");
		stamp_gpsformat.setTitle(Html.fromHtml("<font color='#3c4143'>"+getResources().getString(R.string.preference_stamp_gpsformat)+"</font>"));
		stamp_gpsformat.setSummary(Html.fromHtml("<font color='#676767'>"+getResources().getString(R.string.preference_stamp_gpsformat_summary)+"</font>"));

		EditTextPreference textstamp=(EditTextPreference)this.findPreference("preference_textstamp");
		textstamp.setTitle(Html.fromHtml("<font color='#3c4143'>"+getResources().getString(R.string.preference_textstamp)+"</font>"));
		textstamp.setSummary(Html.fromHtml("<font color='#676767'>"+getResources().getString(R.string.preference_textstamp_summary)+"</font>"));

		ListPreference stamp_fontsize=(ListPreference)this.findPreference("preference_stamp_fontsize");
		stamp_fontsize.setTitle(Html.fromHtml("<font color='#3c4143'>"+getResources().getString(R.string.preference_stamp_fontsize)+"</font>"));
		stamp_fontsize.setSummary(Html.fromHtml("<font color='#676767'>"+getResources().getString(R.string.preference_stamp_fontsize_summary)+"</font>"));

		ListPreference stamp_font_color=(ListPreference)this.findPreference("preference_stamp_font_color");
		stamp_font_color.setTitle(Html.fromHtml("<font color='#3c4143'>"+getResources().getString(R.string.preference_stamp_font_color)+"</font>"));
		stamp_font_color.setSummary(Html.fromHtml("<font color='#676767'>"+getResources().getString(R.string.preference_stamp_font_color_summary)+"</font>"));

		ListPreference stamp_style=(ListPreference)this.findPreference("preference_stamp_style");
		stamp_gpsformat.setTitle(Html.fromHtml("<font color='#3c4143'>"+getResources().getString(R.string.preference_stamp_style)+"</font>"));
		stamp_gpsformat.setSummary(Html.fromHtml("<font color='#676767'>"+getResources().getString(R.string.preference_stamp_style_summary)+"</font>"));

		ListPreference video_quality1=(ListPreference)this.findPreference("preference_video_quality");
		video_quality1.setTitle(Html.fromHtml("<font color='#3c4143'>"+getResources().getString(R.string.video_quality)+"</font>"));
		video_quality1.setSummary(Html.fromHtml("<font color='#676767'>"+getResources().getString(R.string.video_quality_summary)+"</font>"));

		CheckBoxPreference force_video_4k=(CheckBoxPreference)this.findPreference("preference_force_video_4k");
		force_video_4k.setTitle(Html.fromHtml("<font color='#3c4143'>"+getResources().getString(R.string.preference_force_video_4k)+"</font>"));
		force_video_4k.setSummary(Html.fromHtml("<font color='#676767'>"+getResources().getString(R.string.preference_force_video_4k_summary)+"</font>"));

		CheckBoxPreference video_stabilization=(CheckBoxPreference)this.findPreference("preference_video_stabilization");
		video_stabilization.setTitle(Html.fromHtml("<font color='#3c4143'>"+getResources().getString(R.string.preference_video_stabilization)+"</font>"));
		video_stabilization.setSummary(Html.fromHtml("<font color='#676767'>"+getResources().getString(R.string.preference_video_stabilization_summary)+"</font>"));

		ListPreference video_bitrate=(ListPreference)this.findPreference("preference_video_bitrate");
		video_bitrate.setTitle(Html.fromHtml("<font color='#3c4143'>"+getResources().getString(R.string.preference_video_bitrate)+"</font>"));
		video_bitrate.setSummary(Html.fromHtml("<font color='#676767'>"+getResources().getString(R.string.preference_video_bitrate_summary)+"</font>"));

		ListPreference video_fps=(ListPreference)this.findPreference("preference_video_fps");
		video_fps.setTitle(Html.fromHtml("<font color='#3c4143'>"+getResources().getString(R.string.preference_video_fps)+"</font>"));
		video_fps.setSummary(Html.fromHtml("<font color='#676767'>"+getResources().getString(R.string.preference_video_fps_summary)+"</font>"));

		ListPreference video_max_duration=(ListPreference)this.findPreference("preference_video_max_duration");
		video_max_duration.setTitle(Html.fromHtml("<font color='#3c4143'>"+getResources().getString(R.string.preference_video_max_duration)+"</font>"));
		video_max_duration.setSummary(Html.fromHtml("<font color='#676767'>"+getResources().getString(R.string.preference_video_max_duration_summary)+"</font>"));

		ListPreference video_restart=(ListPreference)this.findPreference("preference_video_restart");
		video_restart.setTitle(Html.fromHtml("<font color='#3c4143'>"+getResources().getString(R.string.preference_video_restart)+"</font>"));
		video_restart.setSummary(Html.fromHtml("<font color='#676767'>"+getResources().getString(R.string.preference_video_restart_summary)+"</font>"));

		CheckBoxPreference record_audio=(CheckBoxPreference)this.findPreference("preference_record_audio");
		record_audio.setTitle(Html.fromHtml("<font color='#3c4143'>"+getResources().getString(R.string.preference_record_audio)+"</font>"));
		record_audio.setSummary(Html.fromHtml("<font color='#676767'>"+getResources().getString(R.string.preference_record_audio_summary)+"</font>"));

		ListPreference record_audio_src=(ListPreference)this.findPreference("preference_record_audio_src");
		record_audio_src.setTitle(Html.fromHtml("<font color='#3c4143'>"+getResources().getString(R.string.preference_record_audio_src)+"</font>"));
		record_audio_src.setSummary(Html.fromHtml("<font color='#676767'>"+getResources().getString(R.string.preference_record_audio_src_summary)+"</font>"));

		ListPreference record_audio_channels=(ListPreference)this.findPreference("preference_record_audio_channels");
		record_audio_channels.setTitle(Html.fromHtml("<font color='#3c4143'>"+getResources().getString(R.string.preference_record_audio_channels)+"</font>"));
		record_audio_channels.setSummary(Html.fromHtml("<font color='#676767'>"+getResources().getString(R.string.preference_record_audio_channels_summary)+"</font>"));

		CheckBoxPreference video_flash=(CheckBoxPreference)this.findPreference("preference_video_flash");
		video_flash.setTitle(Html.fromHtml("<font color='#3c4143'>"+getResources().getString(R.string.preference_video_flash)+"</font>"));
		video_flash.setSummary(Html.fromHtml("<font color='#676767'>"+getResources().getString(R.string.preference_video_flash_summary)+"</font>"));

		CheckBoxPreference location=(CheckBoxPreference)this.findPreference("preference_location");
		location.setTitle(Html.fromHtml("<font color='#3c4143'>"+getResources().getString(R.string.preference_location)+"</font>"));
		location.setSummary(Html.fromHtml("<font color='#676767'>"+getResources().getString(R.string.preference_location_summary)+"</font>"));

		CheckBoxPreference gps_direction=(CheckBoxPreference)this.findPreference("preference_gps_direction");
		gps_direction.setTitle(Html.fromHtml("<font color='#3c4143'>"+getResources().getString(R.string.preference_gps_direction)+"</font>"));
		gps_direction.setSummary(Html.fromHtml("<font color='#676767'>"+getResources().getString(R.string.preference_gps_direction_summary)+"</font>"));

		CheckBoxPreference require_location=(CheckBoxPreference)this.findPreference("preference_require_location");
		require_location.setTitle(Html.fromHtml("<font color='#3c4143'>"+getResources().getString(R.string.preference_require_location)+"</font>"));
		require_location.setSummary(Html.fromHtml("<font color='#676767'>"+getResources().getString(R.string.preference_require_location_summary)+"</font>"));

		CheckBoxPreference use_camera2=(CheckBoxPreference)this.findPreference("preference_use_camera2");
		use_camera2.setTitle(Html.fromHtml("<font color='#3c4143'>"+getResources().getString(R.string.preference_use_camera2)+"</font>"));
		use_camera2.setSummary(Html.fromHtml("<font color='#676767'>"+getResources().getString(R.string.preference_use_camera2_summary)+"</font>"));


*/

		final Bundle bundle = getArguments();
		final int cameraId = bundle.getInt("cameraId");
		if( MyDebug.LOG )
			Log.d(TAG, "cameraId: " + cameraId);
		
		final String camera_api = bundle.getString("camera_api");
		
		final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.getActivity());

		final boolean supports_auto_stabilise = bundle.getBoolean("supports_auto_stabilise");
		if( MyDebug.LOG )
			Log.d(TAG, "supports_auto_stabilise: " + supports_auto_stabilise);

		/*if( !supports_auto_stabilise ) {
			Preference pref = findPreference("preference_auto_stabilise");
			PreferenceGroup pg = (PreferenceGroup)this.findPreference("preference_category_camera_effects");
        	pg.removePreference(pref);
		}*/

		//readFromBundle(bundle, "color_effects", Preview.getColorEffectPreferenceKey(), Camera.Parameters.EFFECT_NONE, "preference_category_camera_effects");
		//readFromBundle(bundle, "scene_modes", Preview.getSceneModePreferenceKey(), Camera.Parameters.SCENE_MODE_AUTO, "preference_category_camera_effects");
		//readFromBundle(bundle, "white_balances", Preview.getWhiteBalancePreferenceKey(), Camera.Parameters.WHITE_BALANCE_AUTO, "preference_category_camera_effects");
		//readFromBundle(bundle, "isos", Preview.getISOPreferenceKey(), "auto", "preference_category_camera_effects");
		//readFromBundle(bundle, "exposures", "preference_exposure", "0", "preference_category_camera_effects");

		final boolean supports_face_detection = bundle.getBoolean("supports_face_detection");
		if( MyDebug.LOG )
			Log.d(TAG, "supports_face_detection: " + supports_face_detection);

		if( !supports_face_detection ) {
			Preference pref = findPreference("preference_face_detection");
			PreferenceGroup pg = (PreferenceGroup)this.findPreference("preference_category_camera_effects");

        	pg.removePreference(pref);
		}

		final int preview_width = bundle.getInt("preview_width");
		final int preview_height = bundle.getInt("preview_height");
		final int [] preview_widths = bundle.getIntArray("preview_widths");
		final int [] preview_heights = bundle.getIntArray("preview_heights");
		final int [] video_widths = bundle.getIntArray("video_widths");
		final int [] video_heights = bundle.getIntArray("video_heights");

		final int resolution_width = bundle.getInt("resolution_width");
		final int resolution_height = bundle.getInt("resolution_height");
		final int [] widths = bundle.getIntArray("resolution_widths");
		final int [] heights = bundle.getIntArray("resolution_heights");
		if( widths != null && heights != null ) {
			CharSequence [] entries = new CharSequence[widths.length];
			CharSequence [] values = new CharSequence[widths.length];
			for(int i=0;i<widths.length;i++) {
				entries[i] = widths[i] + " x " + heights[i] + " " + Preview.getAspectRatioMPString(widths[i], heights[i]);
				values[i] = widths[i] + " " + heights[i];
			}
			ListPreference lp = (ListPreference)findPreference("preference_resolution");
			lp.setEntries(entries);
			lp.setEntryValues(values);
			String resolution_preference_key = PreferenceKeys.getResolutionPreferenceKey(cameraId);
			String resolution_value = sharedPreferences.getString(resolution_preference_key, "");
			if( MyDebug.LOG )
				Log.d(TAG, "resolution_value: " + resolution_value);
			lp.setValue(resolution_value);
			// now set the key, so we save for the correct cameraId
			lp.setKey(resolution_preference_key);
		}
		else {
			Preference pref = findPreference("preference_resolution");
			PreferenceGroup pg = (PreferenceGroup)this.findPreference("preference_screen_photo_settings");
        	pg.removePreference(pref);
		}

		{
			final int n_quality = 100;
			CharSequence [] entries = new CharSequence[n_quality];
			CharSequence [] values = new CharSequence[n_quality];
			for(int i=0;i<n_quality;i++) {
				entries[i] = "" + (i+1) + "%";
				values[i] = "" + (i+1);
			}
			ListPreference lp = (ListPreference)findPreference("preference_quality");
			lp.setEntries(entries);
			lp.setEntryValues(values);
		}

		final String [] video_quality = bundle.getStringArray("video_quality");
		final String [] video_quality_string = bundle.getStringArray("video_quality_string");
		if( video_quality != null && video_quality_string != null ) {
			CharSequence [] entries = new CharSequence[video_quality.length];
			CharSequence [] values = new CharSequence[video_quality.length];
			for(int i=0;i<video_quality.length;i++) {
				entries[i] = video_quality_string[i];
				values[i] = video_quality[i];
			}
			ListPreference lp = (ListPreference)findPreference("preference_video_quality");
			lp.setEntries(entries);
			lp.setEntryValues(values);
			String video_quality_preference_key = PreferenceKeys.getVideoQualityPreferenceKey(cameraId);
			String video_quality_value = sharedPreferences.getString(video_quality_preference_key, "");
			if( MyDebug.LOG )
				Log.d(TAG, "video_quality_value: " + video_quality_value);
			lp.setValue(video_quality_value);
			// now set the key, so we save for the correct cameraId
			lp.setKey(video_quality_preference_key);
		}
		else {
			Preference pref = findPreference("preference_video_quality");
			PreferenceGroup pg = (PreferenceGroup)this.findPreference("preference_screen_video_settings");
        	pg.removePreference(pref);
		}
		final String current_video_quality = bundle.getString("current_video_quality");
		final int video_frame_width = bundle.getInt("video_frame_width");
		final int video_frame_height = bundle.getInt("video_frame_height");
		final int video_bit_rate = bundle.getInt("video_bit_rate");
		final int video_frame_rate = bundle.getInt("video_frame_rate");

		final boolean supports_force_video_4k = bundle.getBoolean("supports_force_video_4k");
		if( MyDebug.LOG )
			Log.d(TAG, "supports_force_video_4k: " + supports_force_video_4k);
		if( !supports_force_video_4k || video_quality == null || video_quality_string == null ) {
			Preference pref = findPreference("preference_force_video_4k");
			PreferenceGroup pg = (PreferenceGroup)this.findPreference("preference_screen_video_settings");
        	pg.removePreference(pref);
		}
		
		final boolean supports_video_stabilization = bundle.getBoolean("supports_video_stabilization");
		if( MyDebug.LOG )
			Log.d(TAG, "supports_video_stabilization: " + supports_video_stabilization);
		if( !supports_video_stabilization ) {
			Preference pref = findPreference("preference_video_stabilization");
			PreferenceGroup pg = (PreferenceGroup)this.findPreference("preference_screen_video_settings");
        	pg.removePreference(pref);
		}

		final boolean can_disable_shutter_sound = bundle.getBoolean("can_disable_shutter_sound");
		if( MyDebug.LOG )
			Log.d(TAG, "can_disable_shutter_sound: " + can_disable_shutter_sound);
        if( Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1 || !can_disable_shutter_sound ) {
        	// Camera.enableShutterSound requires JELLY_BEAN_MR1 or greater
        	Preference pref = findPreference("preference_shutter_sound");
        	PreferenceGroup pg = (PreferenceGroup)this.findPreference("preference_screen_camera_controls_more");
        	pg.removePreference(pref);
        }

//        if( Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT ) {
//        	// Some immersive modes require KITKAT - simpler to require Kitkat for any of the menu options
//        	Preference pref = findPreference("preference_immersive_mode");
//        	PreferenceGroup pg = (PreferenceGroup)this.findPreference("preference_screen_gui");
//        	pg.removePreference(pref);
//        }
//
//		final boolean using_android_l = bundle.getBoolean("using_android_l");
//        if( !using_android_l ) {
//        	Preference pref = findPreference("preference_show_iso");
//        	PreferenceGroup pg = (PreferenceGroup)this.findPreference("preference_screen_gui");
//        	pg.removePreference(pref);
//        }

//		final boolean supports_camera2 = bundle.getBoolean("supports_camera2");
//		if( MyDebug.LOG )
//			Log.d(TAG, "supports_camera2: " + supports_camera2);
//        if( supports_camera2 ) {
//        	final Preference pref = findPreference("preference_use_camera2");
//            pref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
//                @Override
//                public boolean onPreferenceClick(Preference arg0) {
//                	if( pref.getKey().equals("preference_use_camera2") ) {
//                		if( MyDebug.LOG )
//                			Log.d(TAG, "user clicked camera2 API - need to restart");
//                		// see http://stackoverflow.com/questions/2470870/force-application-to-restart-on-first-activity
//                		Intent i = getActivity().getBaseContext().getPackageManager().getLaunchIntentForPackage( getActivity().getBaseContext().getPackageName() );
//	                	i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//	                	startActivity(i);
//	                	return false;
//                	}
//                	return false;
//                }
//            });
//        }
//        else {
//        	Preference pref = findPreference("preference_use_camera2");
//        	PreferenceGroup pg = (PreferenceGroup)this.findPreference("preference_category_online");
//        	pg.removePreference(pref);
//        }
        
       /* {
            final Preference pref = findPreference("preference_online_help");
            pref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference arg0) {
                	if( pref.getKey().equals("preference_online_help") ) {
                		if( MyDebug.LOG )
                			Log.d(TAG, "user clicked online help");
            	        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://opencamera.sourceforge.net/"));
            	        startActivity(browserIntent);
                		return false;
                	}
                	return false;
                }
            });
        }*/

        /*{
        	EditTextPreference edit = (EditTextPreference)findPreference("preference_save_location");
        	InputFilter filter = new InputFilter() { 
        		// whilst Android seems to allow any characters on internal memory, SD cards are typically formatted with FAT32
        		String disallowed = "|\\?*<\":>";
                public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) { 
                    for(int i=start;i<end;i++) { 
                    	if( disallowed.indexOf( source.charAt(i) ) != -1 ) {
                            return ""; 
                    	}
                    } 
                    return null; 
                }
        	}; 
        	edit.getEditText().setFilters(new InputFilter[]{filter});         	
        }*/
        {
//        	Preference pref = findPreference("preference_save_location");
//        	pref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
//        		@Override
//                public boolean onPreferenceClick(Preference arg0) {
//            		if( MyDebug.LOG )
//            			Log.d(TAG, "clicked save location");
//            		MainActivity main_activity = (MainActivity)MyPreferenceFragment.this.getActivity();
//            		if( main_activity.getStorageUtils().isUsingSAF() ) {
//                		main_activity.openFolderChooserDialogSAF();
//            			return true;
//                    }
//            		else {
//                		FolderChooserDialog fragment = new FolderChooserDialog();
//                		fragment.show(getFragmentManager(), "FOLDER_FRAGMENT");
//                    	return true;
//            		}
//                }
//            });
        }

//        if( Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP ) {
//        	Preference pref = findPreference("preference_using_saf");
//        	PreferenceGroup pg = (PreferenceGroup)this.findPreference("preference_screen_camera_controls_more");
//        	pg.removePreference(pref);
//        }
//        else {
//            final Preference pref = findPreference("preference_using_saf");
//            pref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
//                @Override
//                public boolean onPreferenceClick(Preference arg0) {
//                	if( pref.getKey().equals("preference_using_saf") ) {
//                		if( MyDebug.LOG )
//                			Log.d(TAG, "user clicked saf");
//            			if( sharedPreferences.getBoolean(PreferenceKeys.getUsingSAFPreferenceKey(), false) ) {
//                    		if( MyDebug.LOG )
//                    			Log.d(TAG, "saf is now enabled");
//                    		// seems better to alway re-show the dialog when the user selects, to make it clear where files will be saved (as the SAF location in general will be different to the non-SAF one)
//                    		//String uri = sharedPreferences.getString(PreferenceKeys.getSaveLocationSAFPreferenceKey(), "");
//                    		//if( uri.length() == 0 )
//                    		{
//                        		MainActivity main_activity = (MainActivity)MyPreferenceFragment.this.getActivity();
//                    			Toast.makeText(main_activity, R.string.saf_select_save_location, Toast.LENGTH_SHORT).show();
//                        		main_activity.openFolderChooserDialogSAF();
//                    		}
//            			}
//            			else {
//                    		if( MyDebug.LOG )
//                    			Log.d(TAG, "saf is now disabled");
//            			}
//                	}
//                	return false;
//                }
//            });
//        }

        /*{
            final Preference pref = findPreference("preference_donate");
            pref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference arg0) {
                	if( pref.getKey().equals("preference_donate") ) {
                		if( MyDebug.LOG )
                			Log.d(TAG, "user clicked to donate");
            	        *//*Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(MainActivity.getDonateMarketLink()));
            	        try {
            	        	startActivity(browserIntent);
            	        }
            			catch(ActivityNotFoundException e) {
            				// needed in case market:// not supported
            				if( MyDebug.LOG )
            					Log.d(TAG, "can't launch market:// intent");
                	        browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(MainActivity.getDonateLink()));
            	        	startActivity(browserIntent);
            			}*//*
            	        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(MainActivity.getDonateLink()));
        	        	startActivity(browserIntent);
                		return false;
                	}
                	return false;
                }
            });
        }*/

        {
//            final Preference pref = findPreference("preference_about");
//            pref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
//                @Override
//                public boolean onPreferenceClick(Preference arg0) {
//                	if( pref.getKey().equals("preference_about") ) {
//                		if( MyDebug.LOG )
//                			Log.d(TAG, "user clicked about");
//            	        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MyPreferenceFragment.this.getActivity());
//                        alertDialog.setTitle("About");
//                        final StringBuilder about_string = new StringBuilder();
//                        String version = "UNKNOWN_VERSION";
//                        int version_code = -1;
//						try {
//	                        PackageInfo pInfo = MyPreferenceFragment.this.getActivity().getPackageManager().getPackageInfo(MyPreferenceFragment.this.getActivity().getPackageName(), 0);
//	                        version = pInfo.versionName;
//	                        version_code = pInfo.versionCode;
//	                    }
//						catch(NameNotFoundException e) {
//	                		if( MyDebug.LOG )
//	                			Log.d(TAG, "NameNotFoundException exception trying to get version number");
//							e.printStackTrace();
//						}
//                        about_string.append("Bit Vault Camera v");
//                        about_string.append(version);
//                        about_string.append("\nVersion Code: ");
//                        about_string.append(version_code);
//                        about_string.append("\nReleased under the GPL v3 or later");
//                        about_string.append("\nAndroid API version: ");
//                        about_string.append(Build.VERSION.SDK_INT);
//                        about_string.append("\nDevice manufacturer: ");
//                        about_string.append(Build.MANUFACTURER);
//                        about_string.append("\nDevice model: ");
//                        about_string.append(Build.MODEL);
//                        about_string.append("\nDevice code-name: ");
//                        about_string.append(Build.HARDWARE);
//                        about_string.append("\nDevice variant: ");
//                        about_string.append(Build.DEVICE);
//                        {
//                    		ActivityManager activityManager = (ActivityManager) getActivity().getSystemService(Activity.ACTIVITY_SERVICE);
//                            about_string.append("\nStandard max heap? (MB): ");
//                            about_string.append(activityManager.getMemoryClass());
//                            about_string.append("\nLarge max heap? (MB): ");
//                            about_string.append(activityManager.getLargeMemoryClass());
//                        }
//                        {
//                            Point display_size = new Point();
//                            Display display = MyPreferenceFragment.this.getActivity().getWindowManager().getDefaultDisplay();
//                            display.getSize(display_size);
//                            about_string.append("\nDisplay size: ");
//                            about_string.append(display_size.x);
//                            about_string.append("x");
//                            about_string.append(display_size.y);
//                        }
//                        about_string.append("\nCurrent camera ID: ");
//                        about_string.append(cameraId);
//                        about_string.append("\nCamera API: ");
//                        about_string.append(camera_api);
//                        {
//                        	String last_video_error = sharedPreferences.getString("last_video_error", "");
//                        	if( last_video_error != null && last_video_error.length() > 0 ) {
//                                about_string.append("\nLast video error: ");
//                                about_string.append(last_video_error);
//                        	}
//                        }
//                        if( preview_widths != null && preview_heights != null ) {
//                            about_string.append("\nPreview resolutions: ");
//                			for(int i=0;i<preview_widths.length;i++) {
//                				if( i > 0 ) {
//                    				about_string.append(", ");
//                				}
//                				about_string.append(preview_widths[i]);
//                				about_string.append("x");
//                				about_string.append(preview_heights[i]);
//                			}
//                        }
//                        about_string.append("\nPreview resolution: " + preview_width + "x" + preview_height);
//                        if( widths != null && heights != null ) {
//                            about_string.append("\nPhoto resolutions: ");
//                			for(int i=0;i<widths.length;i++) {
//                				if( i > 0 ) {
//                    				about_string.append(", ");
//                				}
//                				about_string.append(widths[i]);
//                				about_string.append("x");
//                				about_string.append(heights[i]);
//                			}
//                        }
//                        about_string.append("\nPhoto resolution: " + resolution_width + "x" + resolution_height);
//                        if( video_quality != null ) {
//                            about_string.append("\nVideo qualities: ");
//                			for(int i=0;i<video_quality.length;i++) {
//                				if( i > 0 ) {
//                    				about_string.append(", ");
//                				}
//                				about_string.append(video_quality[i]);
//                			}
//                        }
//                        if( video_widths != null && video_heights != null ) {
//                            about_string.append("\nVideo resolutions: ");
//                			for(int i=0;i<video_widths.length;i++) {
//                				if( i > 0 ) {
//                    				about_string.append(", ");
//                				}
//                				about_string.append(video_widths[i]);
//                				about_string.append("x");
//                				about_string.append(video_heights[i]);
//                			}
//                        }
//        				about_string.append("\nVideo quality: " + current_video_quality);
//        				about_string.append("\nVideo frame width: " + video_frame_width);
//        				about_string.append("\nVideo frame height: " + video_frame_height);
//        				about_string.append("\nVideo bit rate: " + video_bit_rate);
//        				about_string.append("\nVideo frame rate: " + video_frame_rate);
//                        about_string.append("\nAuto-stabilise?: ");
//                        about_string.append(getString(supports_auto_stabilise ? R.string.about_available : R.string.about_not_available));
//                        about_string.append("\nAuto-stabilise enabled?: " + sharedPreferences.getBoolean(PreferenceKeys.getAutoStabilisePreferenceKey(), false));
//                        about_string.append("\nFace detection?: ");
//                        about_string.append(getString(supports_face_detection ? R.string.about_available : R.string.about_not_available));
//                        about_string.append("\nVideo stabilization?: ");
//                        about_string.append(getString(supports_video_stabilization ? R.string.about_available : R.string.about_not_available));
//                        about_string.append("\nFlash modes: ");
//                		String [] flash_values = bundle.getStringArray("flash_values");
//                		if( flash_values != null && flash_values.length > 0 ) {
//                			for(int i=0;i<flash_values.length;i++) {
//                				if( i > 0 ) {
//                    				about_string.append(", ");
//                				}
//                				about_string.append(flash_values[i]);
//                			}
//                		}
//                		else {
//                            about_string.append("None");
//                		}
//                        about_string.append("\nFocus modes: ");
//                		String [] focus_values = bundle.getStringArray("focus_values");
//                		if( focus_values != null && focus_values.length > 0 ) {
//                			for(int i=0;i<focus_values.length;i++) {
//                				if( i > 0 ) {
//                    				about_string.append(", ");
//                				}
//                				about_string.append(focus_values[i]);
//                			}
//                		}
//                		else {
//                            about_string.append("None");
//                		}
//                        about_string.append("\nColor effects: ");
//                		String [] color_effects_values = bundle.getStringArray("color_effects");
//                		if( color_effects_values != null && color_effects_values.length > 0 ) {
//                			for(int i=0;i<color_effects_values.length;i++) {
//                				if( i > 0 ) {
//                    				about_string.append(", ");
//                				}
//                				about_string.append(color_effects_values[i]);
//                			}
//                		}
//                		else {
//                            about_string.append("None");
//                		}
//                        about_string.append("\nScene modes: ");
//                		String [] scene_modes_values = bundle.getStringArray("scene_modes");
//                		if( scene_modes_values != null && scene_modes_values.length > 0 ) {
//                			for(int i=0;i<scene_modes_values.length;i++) {
//                				if( i > 0 ) {
//                    				about_string.append(", ");
//                				}
//                				about_string.append(scene_modes_values[i]);
//                			}
//                		}
//                		else {
//                            about_string.append("None");
//                		}
//                        about_string.append("\nWhite balances: ");
//                		String [] white_balances_values = bundle.getStringArray("white_balances");
//                		if( white_balances_values != null && white_balances_values.length > 0 ) {
//                			for(int i=0;i<white_balances_values.length;i++) {
//                				if( i > 0 ) {
//                    				about_string.append(", ");
//                				}
//                				about_string.append(white_balances_values[i]);
//                			}
//                		}
//                		else {
//                            about_string.append("None");
//                		}
//                        about_string.append("\nISOs: ");
//                		String [] isos = bundle.getStringArray("isos");
//                		if( isos != null && isos.length > 0 ) {
//                			for(int i=0;i<isos.length;i++) {
//                				if( i > 0 ) {
//                    				about_string.append(", ");
//                				}
//                				about_string.append(isos[i]);
//                			}
//                		}
//                		else {
//                            about_string.append("None");
//                		}
//                		String iso_key = bundle.getString("iso_key");
//                		if( iso_key != null ) {
//                			about_string.append("\nISO key: " + iso_key);
//                		}
//
//                		about_string.append("\nUsing SAF?: " + sharedPreferences.getBoolean(PreferenceKeys.getUsingSAFPreferenceKey(), false));
//                		String save_location = sharedPreferences.getString(PreferenceKeys.getSaveLocationPreferenceKey(), "OpenCamera");
//                		about_string.append("\nSave Location: " + save_location);
//                		String save_location_saf = sharedPreferences.getString(PreferenceKeys.getSaveLocationSAFPreferenceKey(), "");
//                		about_string.append("\nSave Location SAF: " + save_location_saf);
//
//                		about_string.append("\nParameters: ");
//                		String parameters_string = bundle.getString("parameters_string");
//                		if( parameters_string != null ) {
//                			about_string.append(parameters_string);
//                		}
//                		else {
//                            about_string.append("None");
//                		}
//
//                        alertDialog.setMessage(about_string);
//                        alertDialog.setPositiveButton(R.string.about_ok, null);
//                        alertDialog.setNegativeButton(R.string.about_copy_to_clipboard, new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int id) {
//                        		if( MyDebug.LOG )
//                        			Log.d(TAG, "user clicked copy to clipboard");
//							 	ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Activity.CLIPBOARD_SERVICE);
//							 	ClipData clip = ClipData.newPlainText("OpenCamera About", about_string);
//							 	clipboard.setPrimaryClip(clip);
//                            }
//                        });
//                        alertDialog.show();
//                		return false;
//                	}
//                	return false;
//                }
//            });
        }

        {
            final Preference pref = findPreference("preference_reset");
            pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference arg0) {
                	if( pref.getKey().equals("preference_reset") ) {
                		if( MyDebug.LOG )Log.d(TAG, "user clicked reset");
						AlertDialog.Builder confirmDelete = new AlertDialog.Builder(getActivity());
						LayoutInflater inflater = LayoutInflater.from(getActivity());
						View view = inflater.inflate(R.layout.reset_dialogue, null);
						confirmDelete.setTitle(R.string.preference_reset);
						confirmDelete.setMessage(R.string.preference_reset_question);
						confirmDelete.setCancelable(true);
						confirmDelete.setView(view);
						final AlertDialog alert = confirmDelete.create();
						alert.show();
						final Button btn_yes = (Button) view.findViewById(R.id.btn_yes);
						final Button btn_no = (Button) view.findViewById(R.id.btn_no);
						btn_yes.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View view) {
								try {
									SharedPreferences.Editor editor = sharedPreferences.edit();
									editor.clear();
									editor.putBoolean(PreferenceKeys.getFirstTimePreferenceKey(), true);
									editor.apply();
									if( MyDebug.LOG )
										Log.d(TAG, "user clicked reset - need to restart");
									// see http://stackoverflow.com/questions/2470870/force-application-to-restart-on-first-activity
									Intent i = getActivity().getBaseContext().getPackageManager().getLaunchIntentForPackage( getActivity().getBaseContext().getPackageName() );
									i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
									startActivity(i);


								} catch (Exception e) {
								}

								alert.dismiss();

							}
						});
						btn_no.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View view) {

								alert.dismiss();
							}
						});

					}
    				  /*  new AlertDialog.Builder(MyPreferenceFragment.this.getActivity())
			        	.setIcon(android.R.drawable.ic_dialog_alert)
			        	.setTitle(R.string.preference_reset)
			        	.setMessage(R.string.preference_reset_question)
			        	.setPositiveButton(R.string.answer_yes, new DialogInterface.OnClickListener() {
			        		@Override
					        public void onClick(DialogInterface dialog, int which) {
		                		if( MyDebug.LOG )
		                			Log.d(TAG, "user confirmed reset");
		                		SharedPreferences.Editor editor = sharedPreferences.edit();
		                		editor.clear();
		                		editor.putBoolean(PreferenceKeys.getFirstTimePreferenceKey(), true);
		                		editor.apply();
		                		if( MyDebug.LOG )
		                			Log.d(TAG, "user clicked reset - need to restart");
		                		// see http://stackoverflow.com/questions/2470870/force-application-to-restart-on-first-activity
		                		Intent i = getActivity().getBaseContext().getPackageManager().getLaunchIntentForPackage( getActivity().getBaseContext().getPackageName() );
			                	i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			                	startActivity(i);
					        }
			        	})
			        	.setNegativeButton(R.string.answer_no, null)
			        	.show();
                	}*/
                	return false;
                }
            });
        }
	}
	
	/*private void readFromBundle(Bundle bundle, String intent_key, String preference_key, String default_value, String preference_category_key) {
		if( MyDebug.LOG ) {
			Log.d(TAG, "readFromBundle: " + intent_key);
		}
		String [] values = bundle.getStringArray(intent_key);
		if( values != null && values.length > 0 ) {
			if( MyDebug.LOG ) {
				Log.d(TAG, intent_key + " values:");
				for(int i=0;i<values.length;i++) {
					Log.d(TAG, values[i]);
				}
			}
			ListPreference lp = (ListPreference)findPreference(preference_key);
			lp.setEntries(values);
			lp.setEntryValues(values);
			SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.getActivity());
			String value = sharedPreferences.getString(preference_key, default_value);
			if( MyDebug.LOG )
				Log.d(TAG, "    value: " + values);
			lp.setValue(value);
		}
		else {
			if( MyDebug.LOG )
				Log.d(TAG, "remove preference " + preference_key + " from category " + preference_category_key);
			Preference pref = findPreference(preference_key);
        	PreferenceGroup pg = (PreferenceGroup)this.findPreference(preference_category_key);
        	pg.removePreference(pref);
		}
	}*/
	
	public void onResume() {
		super.onResume();
		getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);

		// prevent fragment being transparent
		// note, setting color here only seems to affect the "main" preference fragment screen, and not sub-screens
		// note, on Galaxy Nexus Android 4.3 this sets to black rather than the dark grey that the background theme should be (and what the sub-screens use); works okay on Nexus 7 Android 5
		// we used to use a light theme for the PreferenceFragment, but mixing themes in same activity seems to cause problems (e.g., for EditTextPreference colors)
		TypedArray array = getActivity().getTheme().obtainStyledAttributes(new int[] {  
			    android.R.attr.colorBackground
		});
		int backgroundColor = Color.parseColor("#323232");
		/*if( MyDebug.LOG ) {
			int r = (backgroundColor >> 16) & 0xFF;
			int g = (backgroundColor >> 8) & 0xFF;
			int b = (backgroundColor >> 0) & 0xFF;
			Log.d(TAG, "backgroundColor: " + r + " , " + g + " , " + b);
		}*/
		getView().setBackgroundColor(Color.WHITE);
		array.recycle();

		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.getActivity());
		sharedPreferences.registerOnSharedPreferenceChangeListener(this);
	}

	public void onPause() {
		super.onPause();
		getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	}

	/* So that manual changes to the checkbox preferences, while the preferences are showing, show up;
	 * in particular, needed for preference_using_saf, when the user cancels the SAF dialog (see
	 * MainActivity.onActivityResult).
	 */
	public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
		if( MyDebug.LOG )
			Log.d(TAG, "onSharedPreferenceChanged");
	    Preference pref = findPreference(key);
	    if( pref instanceof CheckBoxPreference ){
	        CheckBoxPreference checkBoxPref = (CheckBoxPreference)pref;
	        checkBoxPref.setChecked(prefs.getBoolean(key, true));
	    }
	}
}
