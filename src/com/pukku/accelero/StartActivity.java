	package com.pukku.accelero;

import java.io.IOException;

import org.andengine.audio.music.Music;
import org.andengine.audio.music.MusicFactory;
import org.andengine.engine.camera.Camera;
import org.andengine.engine.handler.timer.ITimerCallback;
import org.andengine.engine.handler.timer.TimerHandler;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.sprite.AnimatedSprite;
import org.andengine.entity.sprite.Sprite;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.TiledTextureRegion;
import org.andengine.ui.activity.BaseGameActivity;
import org.andengine.util.color.Color;
import org.andengine.util.debug.Debug;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;

@SuppressWarnings("unused")
public class StartActivity extends BaseGameActivity {

	// ---------------------------------------------
	// CONSTANTS
	// ---------------------------------------------

	private static final int CAMERA_WIDTH = 720;
	private static final int CAMERA_HEIGHT = 480;
	private static int SPR_COLUMN = 2;
	private static int SPR_ROWS = 2;

	// ---------------------------------------------
	// VARIABLES
	// ---------------------------------------------

	private Camera mCamera;
	static protected Music mMusic;

	private SharedPreferences audioOptions;
	private SharedPreferences.Editor audioEditor;
	private SharedPreferences scoresOptions;
	private SharedPreferences.Editor scoresEditor;

	private Scene splashScene, scene;;
	private Handler mHandler;

	// ---------------------------------------------
	// TEXTURES & TEXTURE REGIONS
	// ---------------------------------------------

	private BitmapTextureAtlas textureAtlas;
	private BitmapTextureAtlas mBackgroundTexture;
	private BitmapTextureAtlas splashTA;

	private ITextureRegion playerTextureRegion;
	private ITextureRegion mBackgroundTextureRegion;
	private ITextureRegion splashTempTextureRegion;
	private TiledTextureRegion splashTR;

	// ---------------------------------------------
	// CLASS LOGIC
	// ---------------------------------------------

	@Override
	public EngineOptions onCreateEngineOptions() {
		// TODO Auto-generated method stub

		// requestWindowFeature(Window.FEATURE_NO_TITLE);
		mHandler = new Handler();
		mCamera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);// Camera(top,left,botton,right)

		scoresOptions = getSharedPreferences("scores", MODE_PRIVATE);
		scoresEditor = scoresOptions.edit();
		if (!scoresOptions.contains("scoring")) {
			scoresEditor.putInt("Level1", 0);
			scoresEditor.putInt("Level2", 0);
			scoresEditor.putInt("Level3", 0);

			scoresEditor.commit();
		}

		audioOptions = getSharedPreferences("audio", MODE_PRIVATE);
		audioEditor = audioOptions.edit();
		if (!audioOptions.contains("musicOn")) {
			audioEditor.putBoolean("musicOn", true);
			audioEditor.putBoolean("effectsOn", true);
			audioEditor.commit();
		}

		EngineOptions options = new EngineOptions(true,
				ScreenOrientation.LANDSCAPE_FIXED, new RatioResolutionPolicy(
						CAMERA_WIDTH, CAMERA_HEIGHT), mCamera);

		options.getAudioOptions().setNeedsMusic(true);
		options.getAudioOptions().setNeedsSound(true);

		return options;
	}

	@Override
	public void onCreateResources(
			OnCreateResourcesCallback pOnCreateResourcesCallback)
			throws Exception {

		// TODO Auto-generated method stub

		MusicFactory.setAssetBasePath("mfx/");
		try {
			StartActivity.mMusic = MusicFactory.createMusicFromAsset(
					this.mEngine.getMusicManager(), getApplicationContext(),
					"drops.ogg");
			StartActivity.mMusic.setLooping(true);
		} catch (final IOException e) {
			// TODO: handle exception
			Debug.e(e);
		}

		splashTA = new BitmapTextureAtlas(this.getTextureManager(), 1024, 512,
				TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		splashTempTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(splashTA, this.getAssets(), "gfx/resload.png",
						0, 0);
		splashTA.load();

		pOnCreateResourcesCallback.onCreateResourcesFinished();
	}

	@Override
	public void onCreateScene(OnCreateSceneCallback pOnCreateSceneCallback)
			throws Exception {
		// TODO Auto-generated method stub
		splashScene = new Scene();
		splashScene.setBackground(new Background(0, 0, 0));

		Sprite splash = new Sprite(0, 0, splashTempTextureRegion,
				this.getVertexBufferObjectManager());
		splashScene.attachChild(splash);

		// Starting the music
		mMusic.play();
		// Pause if set
		if (!audioOptions.getBoolean("musicOn", false)) {
			mMusic.pause();
		}

		pOnCreateSceneCallback.onCreateSceneFinished(splashScene);
	}

	@Override
	public void onPopulateScene(Scene pScene,
			OnPopulateSceneCallback pOnPopulateSceneCallback) throws Exception {
		// TODO Auto-generated method stub

		mHandler.postDelayed(mLaunchTask, 3000);
		pOnPopulateSceneCallback.onPopulateSceneFinished();
	}

	private Runnable mLaunchTask = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub

			Intent myIntent = new Intent(StartActivity.this, MenuActivity.class);
			startActivity(myIntent);
		}
	};
	
	@Override
	public synchronized void onPauseGame() {
		// TODO Auto-generated method stub
		super.onPauseGame();
		if (mMusic.isPlaying()) {
			StartActivity.mMusic.pause();
		}

	}

	@Override
	public synchronized void onResumeGame() {
		// TODO Auto-generated method stub

		super.onResumeGame();
		if (audioOptions.getBoolean("musicOn", true)) {
			StartActivity.mMusic.resume();
			mHandler.postDelayed(mLaunchTask, 3000);
		}
		
	}

}
